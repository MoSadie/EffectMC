package com.mosadie.effectmc;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import com.mojang.text2speech.Narrator;
import com.mosadie.effectmc.core.EffectExecutor;
import com.mosadie.effectmc.core.EffectMCCore;
import com.mosadie.effectmc.core.WorldState;
import com.mosadie.effectmc.core.effect.*;
import com.mosadie.effectmc.core.effect.internal.Effect;
import com.mosadie.effectmc.core.effect.internal.EffectRequest;
import com.mosadie.effectmc.core.handler.*;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ConfirmScreen;
import net.minecraft.client.gui.screen.DisconnectedScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.gui.screen.ingame.BookScreen;
import net.minecraft.client.gui.screen.multiplayer.ConnectScreen;
import net.minecraft.client.gui.screen.multiplayer.DirectConnectScreen;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerScreen;
import net.minecraft.client.gui.screen.world.CreateWorldScreen;
import net.minecraft.client.gui.screen.world.SelectWorldScreen;
import net.minecraft.client.network.ServerAddress;
import net.minecraft.client.network.ServerInfo;
import net.minecraft.client.option.GameOptions;
import net.minecraft.client.option.Perspective;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.client.sound.SoundInstance;
import net.minecraft.client.toast.SystemToast;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.component.Component;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.WrittenBookContentComponent;
import net.minecraft.entity.player.PlayerModelPart;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.message.ChatVisibility;
import net.minecraft.registry.RegistryOps;
import net.minecraft.server.integrated.IntegratedServer;
import net.minecraft.sound.SoundCategory;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableTextContent;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.random.Random;
import org.apache.commons.io.IOUtils;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicHeader;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class EffectMC implements ModInitializer, ClientModInitializer, EffectExecutor {

	public static String MODID = "effectmc";

	private EffectMCCore core;

	public static Logger LOGGER = LogManager.getLogger();

	private static Narrator narrator = Narrator.getNarrator();
	private static Random random = Random.create();
	private static ServerInfo serverInfo = new ServerInfo("", "", ServerInfo.ServerType.OTHER); // Used to hold data during Open Screen

	private HttpClient authedClient;

	@Override
	public void onInitialize() {
		System.out.println("Hello Fabric world!");
	}

	@Override
	public void onInitializeClient() {
		File configDir = FabricLoader.getInstance().getConfigDir().resolve("../" + MODID + "/").toFile();
		if (!configDir.exists()) {
			if (!configDir.mkdirs()) {
				LOGGER.error("Something went wrong creating the config directory! The mod will not work until this is fixed!");
				return;
			}
		}
		File trustFile = configDir.toPath().resolve("trust.json").toFile();
		File configFile = configDir.toPath().resolve("config.json").toFile();



		LOGGER.info("Starting Core");
		core = new EffectMCCore(configFile, trustFile,this);
		LOGGER.info("Core Started");

		LOGGER.info("Starting Server");
		boolean result;
		try {
			result = core.initServer();
		} catch (URISyntaxException e) {
			LOGGER.error("Failed to initialize server due to internal error, please report this!", e);
			result = false;
		}
		LOGGER.info("Server start result: " + result);

		// Register game message handler
		GameMessageHandler gameMessageHandler = new GameMessageHandler();
		ClientReceiveMessageEvents.ALLOW_GAME.register(gameMessageHandler);

		// Register command
		ClientCommandRegistrationCallback.EVENT.register(this::registerClientCommand);

		Header authHeader = new BasicHeader("Authorization", "Bearer " + MinecraftClient.getInstance().getSession().getAccessToken());
		List<Header> headers = new ArrayList<>();
		headers.add(authHeader);
		authedClient = HttpClientBuilder.create().setDefaultHeaders(headers).build();
	}

	private void registerClientCommand(CommandDispatcher<FabricClientCommandSource> dispatcher, CommandRegistryAccess registryAccess) {
		LOGGER.info("Registering Client Command");
		dispatcher.register(ClientCommandManager.literal("effectmc")
				.then(ClientCommandManager.literal("trust").executes((context -> {
					MinecraftClient.getInstance().send(core::setTrustFlag);
					receiveChatMessage("[EffectMC] Now prompting to trust the next request sent.");
					return 0;
				})))
				.then(ClientCommandManager.literal("exportbook").executes((context -> {
					if (MinecraftClient.getInstance().player == null) {
						return 0;
					}

					ItemStack mainHand = MinecraftClient.getInstance().player.getMainHandStack();
					ItemStack offHand = MinecraftClient.getInstance().player.getOffHandStack();

					ItemStack bookStack = null;
					if (mainHand.getItem().equals(Items.WRITTEN_BOOK)) {
						bookStack = mainHand;
					} else if (offHand.getItem().equals(Items.WRITTEN_BOOK)) {
						bookStack = offHand;
					}

					if (bookStack == null) {
						receiveChatMessage("[EffectMC] Failed to export book: Not holding a book!");
						return 0;
					}

					if (!bookStack.getComponents().contains(DataComponentTypes.WRITTEN_BOOK_CONTENT)) {
						receiveChatMessage("[EffectMC] Failed to export book: Missing component.");
						return 0;
					}

					DataResult<JsonElement> dataResult = WrittenBookContentComponent.CODEC.encodeStart(JsonOps.INSTANCE, bookStack.get(DataComponentTypes.WRITTEN_BOOK_CONTENT));

					if (dataResult.isError()) {
						receiveChatMessage("[EffectMC] Failed to export book: Error encoding JSON.");
						if (dataResult.error().isPresent()) {
							LOGGER.warn("Error encoding JSON: " + dataResult.error().get().message());
						} else {
							LOGGER.warn("Error encoding JSON: Unknown error.");
						}
						return 0;
					}

					if (dataResult.result().isEmpty()) {
						receiveChatMessage("[EffectMC] Failed to export book: No JSON result.");
						return 0;
					}

					String json = dataResult.result().get().toString();

					LOGGER.info("Exported Book JSON: " + json);
					receiveChatMessage("[EffectMC] Exported the held book to the current log file.");
					return 0;
				}))).then(ClientCommandManager.literal("exportitem").executes((context -> {
					if (MinecraftClient.getInstance().player == null) {
						LOGGER.info("Null player running exportitem, this shouldn't happen!");
						return 0;
					}

					if (MinecraftClient.getInstance().world == null) {
						LOGGER.info("Null world running exportitem, this shouldn't happen!");
						return 0;
					}

					DataResult<JsonElement> dataResult = ItemStack.CODEC.encodeStart(RegistryOps.of(JsonOps.INSTANCE, MinecraftClient.getInstance().world.getRegistryManager()), MinecraftClient.getInstance().player.getMainHandStack());

					if (dataResult.isError()) {
						receiveChatMessage("[EffectMC] Failed to export held item data: Error encoding JSON.");
						if (dataResult.error().isPresent()) {
							LOGGER.warn("Error encoding JSON: " + dataResult.error().get().message());
						} else {
							LOGGER.warn("Error encoding JSON: Unknown error.");
						}
						return 0;
					}

					if (dataResult.result().isEmpty()) {
						receiveChatMessage("[EffectMC] Failed to export held item data: No JSON result.");
						return 0;
					}

					String json = dataResult.result().get().toString();

					LOGGER.info("Held Item JSON: " + json);
					showItemToast(json, "Exported", MinecraftClient.getInstance().player.getMainHandStack().getName().getString());
					receiveChatMessage("[EffectMC] Exported held item data to log file!");
					return 0;
				}))).then(ClientCommandManager.literal("exporteffect").executes((context -> {
					core.setExportFlag();
					receiveChatMessage("[EffectMC] Will export the next triggered effect as JSON to the current log file.");
					return 0;
				}))).then(ClientCommandManager.literal("trigger").then(ClientCommandManager.argument("json", StringArgumentType.greedyString()).executes((context -> {
					String json = StringArgumentType.getString(context, "json");
					EffectRequest request = core.requestFromJson(json);

					if (request == null) {
						receiveChatMessage("[EffectMC] Invalid JSON for effect request!");
						return 0;
					}

					String worldId = getWorldState() == WorldState.SINGLEPLAYER ? getSPWorldName() : getServerIP();

					Device device = new Device(worldId, getWorldState() == WorldState.SINGLEPLAYER ? DeviceType.WORLD : DeviceType.SERVER);

					Effect.EffectResult result = core.triggerEffect(device, request);
					switch (result.result) {
						case SUCCESS -> receiveChatMessage("[EffectMC] Effect \"" + request.getEffectId() + "\" triggered successfully: " + result.message);
						case ERROR -> receiveChatMessage("[EffectMC] Error triggering effect: " + result.message);
						case UNAUTHORIZED -> receiveChatMessage("[EffectMC] World/Server not trusted. Use /effectmc trust to trust the current world/server.");
						case UNKNOWN -> receiveChatMessage("[EffectMC] Unknown effect.");
						case SKIPPED -> receiveChatMessage("[EffectMC] Effect skipped: " + result.message);
						case UNSUPPORTED -> receiveChatMessage("[EffectMC] Effect unsupported: " + result.message);
					}

					return 0;
				})))).executes((context -> {
					receiveChatMessage("[EffectMC] Available subcommands: exportbook, exportitem, exporteffect, trigger, trust");
					return 0;
				})));
	}

	@Override
	public void log(String message) {
		LOGGER.info(message);
	}

	@Override
	public boolean joinServer(String serverIp) {
		MinecraftClient.getInstance().send(() -> {
			leaveIfNeeded();

			if (!ServerAddress.isValid(serverIp)) {
				LOGGER.warn("Invalid server address: " + serverIp);
				return;
			}

			ServerAddress address = ServerAddress.parse(serverIp);
			ServerInfo info = new ServerInfo("EffectMC", serverIp, ServerInfo.ServerType.OTHER);


			LOGGER.info("Connecting to " + serverIp);

			// Connect to server

			ConnectScreen.connect(new TitleScreen(), MinecraftClient.getInstance(), address, info, false, null);
		});
		return true;
	}

	@Override
	public boolean setSkinLayer(SkinLayerEffect.SKIN_SECTION section, boolean visibility) {
		GameOptions options = MinecraftClient.getInstance().options;

		switch (section) {
			case ALL:
				options.togglePlayerModelPart(PlayerModelPart.CAPE, visibility);
				// Fall to ALL_BODY
			case ALL_BODY:
				options.togglePlayerModelPart(PlayerModelPart.HAT, visibility);
				options.togglePlayerModelPart(PlayerModelPart.JACKET, visibility);
				options.togglePlayerModelPart(PlayerModelPart.LEFT_SLEEVE, visibility);
				options.togglePlayerModelPart(PlayerModelPart.LEFT_PANTS_LEG, visibility);
				options.togglePlayerModelPart(PlayerModelPart.RIGHT_SLEEVE, visibility);
				options.togglePlayerModelPart(PlayerModelPart.RIGHT_PANTS_LEG, visibility);
				break;
			case CAPE:
				options.togglePlayerModelPart(PlayerModelPart.CAPE, visibility);
				break;
			case JACKET:
				options.togglePlayerModelPart(PlayerModelPart.JACKET, visibility);
				break;
			case LEFT_SLEEVE:
				options.togglePlayerModelPart(PlayerModelPart.LEFT_SLEEVE, visibility);
				break;
			case RIGHT_SLEEVE:
				options.togglePlayerModelPart(PlayerModelPart.RIGHT_SLEEVE, visibility);
				break;
			case LEFT_PANTS_LEG:
				options.togglePlayerModelPart(PlayerModelPart.LEFT_PANTS_LEG, visibility);
				break;
			case RIGHT_PANTS_LEG:
				options.togglePlayerModelPart(PlayerModelPart.RIGHT_PANTS_LEG, visibility);
				break;
			case HAT:
				options.togglePlayerModelPart(PlayerModelPart.HAT, visibility);
				break;
		}

		return true;
	}

	@Override
	public boolean toggleSkinLayer(SkinLayerEffect.SKIN_SECTION section) {
		GameOptions options = MinecraftClient.getInstance().options;

		switch (section) {
			case ALL:
				trueTogglePlayerModelPart(options, PlayerModelPart.CAPE);
				// Fall to ALL_BODY
			case ALL_BODY:
				trueTogglePlayerModelPart(options, PlayerModelPart.HAT);
				trueTogglePlayerModelPart(options, PlayerModelPart.JACKET);
				trueTogglePlayerModelPart(options, PlayerModelPart.LEFT_SLEEVE);
				trueTogglePlayerModelPart(options, PlayerModelPart.LEFT_PANTS_LEG);
				trueTogglePlayerModelPart(options, PlayerModelPart.RIGHT_SLEEVE);
				trueTogglePlayerModelPart(options, PlayerModelPart.RIGHT_PANTS_LEG);
				break;
			case CAPE:
				trueTogglePlayerModelPart(options, PlayerModelPart.CAPE);
				break;
			case JACKET:
				trueTogglePlayerModelPart(options, PlayerModelPart.JACKET);
				break;
			case LEFT_SLEEVE:
				trueTogglePlayerModelPart(options, PlayerModelPart.LEFT_SLEEVE);
				break;
			case RIGHT_SLEEVE:
				trueTogglePlayerModelPart(options, PlayerModelPart.RIGHT_SLEEVE);
				break;
			case LEFT_PANTS_LEG:
				trueTogglePlayerModelPart(options, PlayerModelPart.LEFT_PANTS_LEG);
				break;
			case RIGHT_PANTS_LEG:
				trueTogglePlayerModelPart(options, PlayerModelPart.RIGHT_PANTS_LEG);
				break;
			case HAT:
				trueTogglePlayerModelPart(options, PlayerModelPart.HAT);
				break;
		}

		return true;
	}

	private void trueTogglePlayerModelPart(GameOptions options, PlayerModelPart part) {
		options.togglePlayerModelPart(part, !options.isPlayerModelPartEnabled(part));
	}

	@Override
	public boolean sendChatMessage(String message) {
		if (MinecraftClient.getInstance().player != null) {
			if (message.startsWith("/")) {
				LOGGER.info("Sending command message: " + message);
				MinecraftClient.getInstance().player.networkHandler.sendCommand(message.substring(1));
			} else {
				LOGGER.info("Sending chat message: " + message);
				MinecraftClient.getInstance().player.networkHandler.sendChatMessage(message);
			}

			return true;
		}

		return false;
	}

	@Override
	public boolean receiveChatMessage(String message) {
		if (MinecraftClient.getInstance().player != null) {
			LOGGER.info("Showing chat message: " + message);
			MinecraftClient.getInstance().player.sendMessage(Text.of(message), false);

			return true;
		}

		return false;
	}

	@Override
	public boolean showTitle(String title, String subtitle) {
		LOGGER.info("Showing Title: " + title + " Subtitle: " + subtitle);
		MinecraftClient.getInstance().inGameHud.setDefaultTitleFade();
		MinecraftClient.getInstance().inGameHud.setSubtitle(Text.of(subtitle));
		MinecraftClient.getInstance().inGameHud.setTitle(Text.of(title));
		return true;
	}

	@Override
	public boolean showActionMessage(String message) {
		LOGGER.info("Showing ActionBar message: " + message);
		MinecraftClient.getInstance().inGameHud.setOverlayMessage(Text.of(message), false);
		return true;
	}

	@Override
	public void showTrustPrompt(Device device) {
		MinecraftClient.getInstance().send(() -> {
			ConfirmScreen screen = new ConfirmScreen(new EffectMCCore.TrustBooleanConsumer(device, core), Text.of("EffectMC - Trust Prompt"), Text.of("Do you want to trust this device?\n(Type: " + device.getType() + (device.getType() == DeviceType.OTHER ? " Device Id:" + device.getId() : "") + ")"));
			MinecraftClient.getInstance().setScreen(screen);
		});
	}

	@Override
	public boolean triggerDisconnect(DisconnectEffect.NEXT_SCREEN nextScreenType, String title, String message) {
		MinecraftClient.getInstance().send(() -> {
			leaveIfNeeded();

			Screen nextScreen;

			switch (nextScreenType) {
				default:
				case MAIN_MENU:
					nextScreen = new TitleScreen();
					break;

				case SERVER_SELECT:
					nextScreen = new MultiplayerScreen(new TitleScreen());
					break;

				case WORLD_SELECT:
					nextScreen = new SelectWorldScreen(new TitleScreen());
					break;
			}

			DisconnectedScreen screen = new DisconnectedScreen(nextScreen, Text.of(title), Text.of(message));
			MinecraftClient.getInstance().setScreen(screen);
		});
		return true;
	}

	@Override
	public boolean playSound(String soundID, String categoryName, float volume, float pitch, boolean repeat, int repeatDelay, String attenuationType, double x, double y, double z, boolean relative, boolean global) {
		MinecraftClient.getInstance().send(() -> {
			Identifier sound = Identifier.tryParse(soundID);

			if (sound == null) {
				LOGGER.info("Invalid sound Identifier");
			}

			SoundCategory category;
			try {
				category = SoundCategory.valueOf(categoryName.toUpperCase());
			} catch (IllegalArgumentException e) {
				category = SoundCategory.MASTER;
			}

			SoundInstance.AttenuationType attenuation;
			try {
				attenuation = SoundInstance.AttenuationType.valueOf(attenuationType.toUpperCase());
			} catch (IllegalArgumentException e) {
				attenuation = SoundInstance.AttenuationType.NONE;
			}

			double trueX = x;
			double trueY = y;
			double trueZ = z;

			if (relative && MinecraftClient.getInstance().world != null && MinecraftClient.getInstance().player != null) {
				trueX += MinecraftClient.getInstance().player.getX();
				trueY += MinecraftClient.getInstance().player.getY();
				trueZ += MinecraftClient.getInstance().player.getZ();
			}

			MinecraftClient.getInstance().getSoundManager().play(new PositionedSoundInstance(sound, category, volume, pitch, random, repeat, repeatDelay, attenuation, trueX, trueY, trueZ, global));
		});

		return true;
	}

	@Override
	public void resetScreen() {
		MinecraftClient.getInstance().send(() -> MinecraftClient.getInstance().setScreen(null));
	}

	@Override
	public boolean stopSound(String sound, String categoryName) {
		MinecraftClient.getInstance().send(() -> {
			Identifier location = sound == null ? null : Identifier.tryParse(sound);
			SoundCategory category = null;

			try {
				category = SoundCategory.valueOf(categoryName);
			} catch (IllegalArgumentException | NullPointerException e) {
				// Do nothing, if soundId is non-null Minecraft will auto-search, otherwise Minecraft stops all sounds.
			}

			MinecraftClient.getInstance().getSoundManager().stopSounds(location, category);
		});

		return true;
	}

	@Override
	public boolean showToast(String title, String subtitle) {
		MinecraftClient.getInstance().send(() -> MinecraftClient.getInstance().getToastManager().add(new SystemToast(SystemToast.Type.NARRATOR_TOGGLE, Text.of(title), Text.of(subtitle))));

		return true;
	}

	@Override
	public boolean showItemToast(String itemData, String title, String subtitle) {
		MinecraftClient.getInstance().send(() -> MinecraftClient.getInstance().getToastManager().add(new ItemToast(itemData, Text.of(title), Text.of(subtitle), core)));

		return true;
	}

	@Override
	public boolean openBook(JsonObject bookJSON) {
		MinecraftClient.getInstance().send(() -> {
			DataResult<Pair<WrittenBookContentComponent, JsonElement>> dataResult = WrittenBookContentComponent.CODEC.decode(JsonOps.INSTANCE, bookJSON);

			if (dataResult.isError()) {
				if (dataResult.error().isPresent()) {
					LOGGER.error("Error decoding book JSON: " + dataResult.error().get().message());
				} else {
					LOGGER.error("Error decoding book JSON: Unknown error.");
				}
				return;
			}

			if (dataResult.result().isEmpty()) {
				LOGGER.error("No result from decoding book JSON.");
				return;
			}

			ItemStack bookStack = new ItemStack(Items.WRITTEN_BOOK);
			bookStack.set(DataComponentTypes.WRITTEN_BOOK_CONTENT, dataResult.result().get().getFirst());

			BookScreen.Contents bookContents = BookScreen.Contents.create(bookStack);

			BookScreen screen = new BookScreen(bookContents);

			MinecraftClient.getInstance().setScreen(screen);
		});

		return true;
	}

	@Override
	public boolean narrate(String message, boolean interrupt) {
		if (narrator.active()) {
			MinecraftClient.getInstance().send(() -> narrator.say(message, interrupt));
			return true;
		}

		LOGGER.error("Narrator is unavailable!");

		return false;
	}

	@Override
	public boolean loadWorld(String worldName) {
		MinecraftClient.getInstance().send(() -> {
			if (MinecraftClient.getInstance().getLevelStorage().levelExists(worldName)) {
				if (MinecraftClient.getInstance().world != null) {
					LOGGER.info("Disconnecting from world...");

					MinecraftClient.getInstance().world.disconnect();
					MinecraftClient.getInstance().disconnect();
				}

				LOGGER.info("Loading world...");
				MinecraftClient.getInstance().createIntegratedServerLoader().start(worldName, () -> {
					LOGGER.info("World load cancelled!");
					MinecraftClient.getInstance().setScreen(new TitleScreen());
				});
			} else {
				LOGGER.warn("World " + worldName + " does not exist!");
			}
		});

		return true;
	}

	@Override
	public boolean setSkin(URL skinUrl, SetSkinEffect.SKIN_TYPE skinType) {
		if (skinUrl == null) {
			LOGGER.warn("Skin URL is null!");
			return false;
		}

		try {
			JsonObject payload = new JsonObject();

			payload.add("variant", new JsonPrimitive(skinType.getValue()));
			payload.add("url", new JsonPrimitive(skinUrl.toString()));

			HttpPost request = new HttpPost("https://api.minecraftservices.com/minecraft/profile/skins");
			request.setEntity(new StringEntity(payload.toString(), ContentType.APPLICATION_JSON));

			HttpResponse response = authedClient.execute(request);

			if (response.getEntity() != null && response.getEntity().getContentLength() > 0) {
				JsonObject responseJSON = core.fromJson(IOUtils.toString(response.getEntity().getContent(), StandardCharsets.UTF_8));
				if (responseJSON.has("errorMessage")) {
					LOGGER.warn("Failed to update skin! " + responseJSON);
					return false;
				}

				LOGGER.debug("Skin Update Response: " + responseJSON);
			}

			LOGGER.info("Skin updated!");
			return true;
		} catch (IOException e) {
			LOGGER.warn("Failed to update skin!", e);
			return false;
		}
	}

	public void leaveIfNeeded() {
		if (MinecraftClient.getInstance().world != null) {
			LOGGER.info("Disconnecting from world...");

			MinecraftClient.getInstance().world.disconnect();
			MinecraftClient.getInstance().disconnect();
		}
	}

	@Override
	public boolean openScreen(OpenScreenEffect.SCREEN screen) {
		MinecraftClient.getInstance().execute(() -> {
			leaveIfNeeded();

			switch (screen) {
				case MAIN_MENU:
					MinecraftClient.getInstance().setScreen(new TitleScreen());
					break;
				case SERVER_SELECT:
					MinecraftClient.getInstance().setScreen(new MultiplayerScreen(new TitleScreen()));
					break;
				case SERVER_DIRECT_CONNECT:
					MinecraftClient.getInstance().setScreen(new DirectConnectScreen(new MultiplayerScreen(new TitleScreen()), this::connectIfTrue, serverInfo));
					break;
				case WORLD_SELECT:
					MinecraftClient.getInstance().setScreen(new SelectWorldScreen(new TitleScreen()));
					break;
				case WORLD_CREATE:
					CreateWorldScreen.create(MinecraftClient.getInstance(), new SelectWorldScreen(new TitleScreen()));
					break;
				default:
					LOGGER.error("Unknown screen.");
			}
		});
		return true;
	}

	@Override
	public boolean setFOV(int fov) {
		MinecraftClient.getInstance().execute(() -> MinecraftClient.getInstance().options.getFov().setValue(fov));
		return true;
	}

	@Override
	public boolean setPOV(SetPovEffect.POV pov) {
		Perspective mcPov;

		switch (pov) {
			default:
			case FIRST_PERSON:
				mcPov = Perspective.FIRST_PERSON;
				break;

			case THIRD_PERSON_BACK:
				mcPov = Perspective.THIRD_PERSON_BACK;
				break;

			case THIRD_PERSON_FRONT:
				mcPov = Perspective.THIRD_PERSON_FRONT;
				break;
		}

		MinecraftClient.getInstance().execute(() -> MinecraftClient.getInstance().options.setPerspective(mcPov));
		return true;
	}

	@Override
	public boolean setGuiScale(int scale) {
		if (MinecraftClient.getInstance().options.getGuiScale().getValue().equals(scale)) {
			return true;
		}

		MinecraftClient.getInstance().execute(() -> {
			MinecraftClient.getInstance().options.getGuiScale().setValue(scale);
			MinecraftClient.getInstance().options.write();
			MinecraftClient.getInstance().onResolutionChanged();
		});
		return true;
	}

	@Override
	public boolean setGamma(double gamma) {
		MinecraftClient.getInstance().execute(() -> {
			MinecraftClient.getInstance().options.getGamma().setValue(gamma);
			MinecraftClient.getInstance().options.write();
		});
		return true;
	}

	@Override
	public boolean setChatVisibility(ChatVisibilityEffect.VISIBILITY visibility) {
		ChatVisibility result;
		switch (visibility) {
			case SHOW:
				result = ChatVisibility.FULL;
				break;

			case COMMANDS_ONLY:
				result = ChatVisibility.SYSTEM;
				break;

			case HIDE:
				result = ChatVisibility.HIDDEN;
				break;

			default:
				return false;
		}

		MinecraftClient.getInstance().execute(() -> {
			MinecraftClient.getInstance().options.getChatVisibility().setValue(result);
			MinecraftClient.getInstance().options.write();
		});
		return true;
	}

	@Override
	public boolean setRenderDistance(int chunks) {
		MinecraftClient.getInstance().execute(() -> {
			MinecraftClient.getInstance().options.getViewDistance().setValue(chunks);
			MinecraftClient.getInstance().options.write();
		});
		return true;
	}

	@Override
	public WorldState getWorldState() {
		if (MinecraftClient.getInstance().world == null) {
			return WorldState.OTHER;
		}

		return MinecraftClient.getInstance().isConnectedToLocalServer() ? WorldState.SINGLEPLAYER : WorldState.MULTIPLAYER;
	}

	@Override
	public String getSPWorldName() {
		if (getWorldState() != WorldState.SINGLEPLAYER) {
			return null;
		}

		IntegratedServer server = MinecraftClient.getInstance().getServer();

		if (server != null) {
			return server.getSaveProperties().getLevelName();
		}

		LOGGER.info("Attempted to get SP World Name, but no integrated server was found!");
		return null;
	}

	@Override
	public String getServerIP() {
		if (getWorldState() != WorldState.MULTIPLAYER) {
			return null;
		}

		if (MinecraftClient.getInstance().getCurrentServerEntry() != null) {
			return MinecraftClient.getInstance().getCurrentServerEntry().address;
		}

		LOGGER.info("Failed to get Server IP!");
		return null;
	}

	@Override
	public void setVolume(SetVolumeEffect.VOLUME_CATEGORIES category, int volume) {
		MinecraftClient.getInstance().execute(() -> {
			SoundCategory mcCategory;

			switch (category) {
				case MASTER:
					mcCategory = SoundCategory.MASTER;
					break;

				case MUSIC:
					mcCategory = SoundCategory.MUSIC;
					break;

				case RECORDS:
					mcCategory = SoundCategory.RECORDS;
					break;

				case WEATHER:
					mcCategory = SoundCategory.WEATHER;
					break;

				case BLOCKS:
					mcCategory = SoundCategory.BLOCKS;
					break;

				case HOSTILE:
					mcCategory = SoundCategory.HOSTILE;
					break;

				case NEUTRAL:
					mcCategory = SoundCategory.NEUTRAL;
					break;

				case PLAYERS:
					mcCategory = SoundCategory.PLAYERS;
					break;

				case AMBIENT:
					mcCategory = SoundCategory.AMBIENT;
					break;

				case VOICE:
					mcCategory = SoundCategory.VOICE;
					break;

				default:
					LOGGER.error("Unknown volume category!");
					return;
			}

			MinecraftClient.getInstance().options.getSoundVolumeOption(mcCategory).setValue(volume / 100.0d);
			MinecraftClient.getInstance().options.write();
		});
	}

	private void connectIfTrue(boolean connect) {
		if (connect) {
			joinServer(serverInfo.address);
		} else {
			MinecraftClient.getInstance().setScreen(new MultiplayerScreen(new TitleScreen()));
		}
	}

	public class GameMessageHandler implements ClientReceiveMessageEvents.AllowGame {

		@Override
		public boolean allowReceiveGameMessage(Text message, boolean overlay) {
			if (message.getContent() instanceof TranslatableTextContent text) {
				if (!text.getKey().equals(EffectMCCore.TRANSLATION_TRIGGER_KEY)) {
					//LOGGER.error("Received non-trigger message!");
					return true;
				}

				if (text.getArgs().length == 0) {
					LOGGER.error("Received trigger message with no arguments!");
					return false;
				}

				EffectRequest request = core.requestFromJson(String.valueOf(text.getArgs()[0]));

				if (request == null) {
					LOGGER.error("Failed to parse request!");
					return false;
				}

				String worldId = getWorldState() == WorldState.SINGLEPLAYER ? getSPWorldName() : getServerIP();

				Device device = new Device(worldId, getWorldState() == WorldState.SINGLEPLAYER ? DeviceType.WORLD : DeviceType.SERVER);

				core.triggerEffect(device, request);
				return false;
			}

			return true;
		}
	}
}
