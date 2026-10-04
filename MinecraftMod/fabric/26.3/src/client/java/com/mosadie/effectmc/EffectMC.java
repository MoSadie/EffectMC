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
import net.fabricmc.fabric.api.client.command.v2.ClientCommands;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.client.gui.components.toasts.SystemToast;
import net.minecraft.client.gui.screens.*;
import net.minecraft.client.gui.screens.inventory.BookViewScreen;
import net.minecraft.client.gui.screens.multiplayer.JoinMultiplayerScreen;
import net.minecraft.client.gui.screens.worldselection.CreateWorldScreen;
import net.minecraft.client.gui.screens.worldselection.SelectWorldScreen;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.multiplayer.resolver.ServerAddress;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.server.IntegratedServer;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.RegistryOps;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.ChatVisiblity;
import net.minecraft.world.entity.player.PlayerModelPart;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.WrittenBookContent;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.net.Authenticator;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class EffectMC implements ModInitializer, ClientModInitializer, EffectExecutor {

	public static String MODID = "effectmc";

	private EffectMCCore core;

	public static Logger LOGGER = LogManager.getLogger();

	private static Narrator narrator = Narrator.getNarrator();
	private static RandomSource random = RandomSource.create();
	private static ServerData serverData = new ServerData("", "", ServerData.Type.OTHER); // Used to hold data during Open Screen

	private HttpClient httpClient;

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

		httpClient = HttpClient.newHttpClient();
	}

	private void registerClientCommand(CommandDispatcher<FabricClientCommandSource> dispatcher, CommandBuildContext contextUnused) {
		LOGGER.info("Registering Client Command");
		dispatcher.register(ClientCommands.literal("effectmc")
				.then(ClientCommands.literal("trust").executes((context -> {
					Minecraft.getInstance().execute(core::setTrustFlag);
					receiveChatMessage("[EffectMC] Now prompting to trust the next request sent.");
					return 0;
				})))
				.then(ClientCommands.literal("exportbook").executes((context -> {
					if (Minecraft.getInstance().player == null) {
						return 0;
					}

					ItemStack mainHand = Minecraft.getInstance().player.getMainHandItem();
					ItemStack offHand = Minecraft.getInstance().player.getOffhandItem();

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

					if (!bookStack.getComponents().has(DataComponents.WRITTEN_BOOK_CONTENT)) {
						receiveChatMessage("[EffectMC] Failed to export book: Missing component.");
						return 0;
					}

					DataResult<JsonElement> dataResult = WrittenBookContent.CODEC.encodeStart(JsonOps.INSTANCE, bookStack.get(DataComponents.WRITTEN_BOOK_CONTENT));

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
				}))).then(ClientCommands.literal("exportitem").executes((context -> {
					if (Minecraft.getInstance().player == null) {
						LOGGER.info("Null player running exportitem, this shouldn't happen!");
						return 0;
					}

					if (Minecraft.getInstance().level == null) {
						LOGGER.info("Null world running exportitem, this shouldn't happen!");
						return 0;
					}

					DataResult<JsonElement> dataResult = ItemStack.CODEC.encodeStart(RegistryOps.create(JsonOps.INSTANCE, Minecraft.getInstance().level.registryAccess()), Minecraft.getInstance().player.getMainHandItem());

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
					showItemToast(json, "Exported", Minecraft.getInstance().player.getMainHandItem().getDisplayName().getString());
					receiveChatMessage("[EffectMC] Exported held item data to log file!");
					return 0;
				}))).then(ClientCommands.literal("exporteffect").executes((context -> {
					core.setExportFlag();
					receiveChatMessage("[EffectMC] Will export the next triggered effect as JSON to the current log file.");
					return 0;
				}))).then(ClientCommands.literal("trigger").then(ClientCommands.argument("json", StringArgumentType.greedyString()).executes((context -> {
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
		Minecraft.getInstance().execute(() -> {
			leaveIfNeeded();

			if (!ServerAddress.isValidAddress(serverIp)) {
				LOGGER.warn("Invalid server address: " + serverIp);
				return;
			}

			ServerAddress address = ServerAddress.parseString(serverIp);
			ServerData info = new ServerData("EffectMC", serverIp, ServerData.Type.OTHER);


			LOGGER.info("Connecting to " + serverIp);

			// Connect to server

			ConnectScreen.startConnecting(new TitleScreen(), Minecraft.getInstance(), address, info, false, null);
		});
		return true;
	}

	@Override
	public boolean setSkinLayer(SkinLayerEffect.SKIN_SECTION section, boolean visibility) {
		Options options = Minecraft.getInstance().options;

		switch (section) {
			case ALL:
				options.setModelPart(PlayerModelPart.CAPE, visibility);
				// Fall to ALL_BODY
			case ALL_BODY:
				options.setModelPart(PlayerModelPart.HAT, visibility);
				options.setModelPart(PlayerModelPart.JACKET, visibility);
				options.setModelPart(PlayerModelPart.LEFT_SLEEVE, visibility);
				options.setModelPart(PlayerModelPart.LEFT_PANTS_LEG, visibility);
				options.setModelPart(PlayerModelPart.RIGHT_SLEEVE, visibility);
				options.setModelPart(PlayerModelPart.RIGHT_PANTS_LEG, visibility);
				break;
			case CAPE:
				options.setModelPart(PlayerModelPart.CAPE, visibility);
				break;
			case JACKET:
				options.setModelPart(PlayerModelPart.JACKET, visibility);
				break;
			case LEFT_SLEEVE:
				options.setModelPart(PlayerModelPart.LEFT_SLEEVE, visibility);
				break;
			case RIGHT_SLEEVE:
				options.setModelPart(PlayerModelPart.RIGHT_SLEEVE, visibility);
				break;
			case LEFT_PANTS_LEG:
				options.setModelPart(PlayerModelPart.LEFT_PANTS_LEG, visibility);
				break;
			case RIGHT_PANTS_LEG:
				options.setModelPart(PlayerModelPart.RIGHT_PANTS_LEG, visibility);
				break;
			case HAT:
				options.setModelPart(PlayerModelPart.HAT, visibility);
				break;
		}

		options.save();

		return true;
	}

	@Override
	public boolean toggleSkinLayer(SkinLayerEffect.SKIN_SECTION section) {
		Options options = Minecraft.getInstance().options;

		switch (section) {
			case ALL:
				togglePlayerModelPart(options, PlayerModelPart.CAPE);
				// Fall to ALL_BODY
			case ALL_BODY:
				togglePlayerModelPart(options, PlayerModelPart.HAT);
				togglePlayerModelPart(options, PlayerModelPart.JACKET);
				togglePlayerModelPart(options, PlayerModelPart.LEFT_SLEEVE);
				togglePlayerModelPart(options, PlayerModelPart.LEFT_PANTS_LEG);
				togglePlayerModelPart(options, PlayerModelPart.RIGHT_SLEEVE);
				togglePlayerModelPart(options, PlayerModelPart.RIGHT_PANTS_LEG);
				break;
			case CAPE:
				togglePlayerModelPart(options, PlayerModelPart.CAPE);
				break;
			case JACKET:
				togglePlayerModelPart(options, PlayerModelPart.JACKET);
				break;
			case LEFT_SLEEVE:
				togglePlayerModelPart(options, PlayerModelPart.LEFT_SLEEVE);
				break;
			case RIGHT_SLEEVE:
				togglePlayerModelPart(options, PlayerModelPart.RIGHT_SLEEVE);
				break;
			case LEFT_PANTS_LEG:
				togglePlayerModelPart(options, PlayerModelPart.LEFT_PANTS_LEG);
				break;
			case RIGHT_PANTS_LEG:
				togglePlayerModelPart(options, PlayerModelPart.RIGHT_PANTS_LEG);
				break;
			case HAT:
				togglePlayerModelPart(options, PlayerModelPart.HAT);
				break;
		}

		options.save();

		return true;
	}

	private void togglePlayerModelPart(Options options, PlayerModelPart part) {
		options.setModelPart(part, !options.isModelPartEnabled(part));
	}

	@Override
	public boolean sendChatMessage(String message) {
		if (Minecraft.getInstance().player != null) {
			if (message.startsWith("/")) {
				LOGGER.info("Sending command message: " + message);
				Minecraft.getInstance().player.connection.sendCommand(message.substring(1));
			} else {
				LOGGER.info("Sending chat message: " + message);
				Minecraft.getInstance().player.connection.sendChat(message);
			}

			return true;
		}

		return false;
	}

	@Override
	public boolean receiveChatMessage(String message) {
		if (Minecraft.getInstance().player != null) {
			LOGGER.info("Showing chat message: " + message);
			Minecraft.getInstance().player.sendSystemMessage(Component.literal(message));

			return true;
		}

		return false;
	}

	@Override
	public boolean showTitle(String title, String subtitle) {
		LOGGER.info("Showing Title: " + title + " Subtitle: " + subtitle);
		Minecraft.getInstance().gui.hud.resetTitleTimes();
		Minecraft.getInstance().gui.hud.setSubtitle(Component.literal(subtitle));
		Minecraft.getInstance().gui.hud.setTitle(Component.literal(title));
		return true;
	}

	@Override
	public boolean showActionMessage(String message) {
		LOGGER.info("Showing ActionBar message: " + message);
		Minecraft.getInstance().gui.hud.setOverlayMessage(Component.literal(message), false);
		return true;
	}

	@Override
	public void showTrustPrompt(Device device) {
		Minecraft.getInstance().execute(() -> {
			ConfirmScreen screen = new ConfirmScreen(new EffectMCCore.TrustBooleanConsumer(device, core), Component.literal("EffectMC - Trust Prompt"), Component.literal("Do you want to trust this device?\n(Type: " + device.getType() + (device.getType() == DeviceType.OTHER ? " Device Id:" + device.getId() : "") + ")"));
			Minecraft.getInstance().setScreenAndShow(screen);
		});
	}

	@Override
	public boolean triggerDisconnect(DisconnectEffect.NEXT_SCREEN nextScreenType, String title, String message) {
		Minecraft.getInstance().execute(() -> {
			leaveIfNeeded();

			Screen nextScreen;

			switch (nextScreenType) {
				default:
				case MAIN_MENU:
					nextScreen = new TitleScreen();
					break;

				case SERVER_SELECT:
					nextScreen = new JoinMultiplayerScreen(new TitleScreen());
					break;

				case WORLD_SELECT:
					nextScreen = new SelectWorldScreen(new TitleScreen());
					break;
			}

			DisconnectedScreen screen = new DisconnectedScreen(nextScreen, Component.literal(title), Component.literal(message));
			Minecraft.getInstance().setScreenAndShow(screen);
		});
		return true;
	}

	@Override
	public boolean playSound(String soundID, String categoryName, float volume, float pitch, boolean repeat, int repeatDelay, String attenuationType, double x, double y, double z, boolean relative, boolean global) {
		Minecraft.getInstance().execute(() -> {
			Identifier sound = Identifier.tryParse(soundID);

			if (sound == null) {
				LOGGER.info("Invalid sound Identifier");
			}

			SoundSource category;
			try {
				category = SoundSource.valueOf(categoryName.toUpperCase());
			} catch (IllegalArgumentException e) {
				category = SoundSource.MASTER;
			}

			SoundInstance.Attenuation attenuation;
			try {
				attenuation = SoundInstance.Attenuation.valueOf(attenuationType.toUpperCase());
			} catch (IllegalArgumentException e) {
				attenuation = SoundInstance.Attenuation.NONE;
			}

			double trueX = x;
			double trueY = y;
			double trueZ = z;

			if (relative && Minecraft.getInstance().level != null && Minecraft.getInstance().player != null) {
				trueX += Minecraft.getInstance().player.getX();
				trueY += Minecraft.getInstance().player.getY();
				trueZ += Minecraft.getInstance().player.getZ();
			}

			Minecraft.getInstance().getSoundManager().play(new SimpleSoundInstance(sound, category, volume, pitch, random, repeat, repeatDelay, attenuation, trueX, trueY, trueZ, global));
		});

		return true;
	}

	@Override
	public void resetScreen() {
		Minecraft.getInstance().execute(() -> Minecraft.getInstance().setScreenAndShow(null));
	}

	@Override
	public boolean stopSound(String sound, String categoryName) {
		Minecraft.getInstance().execute(() -> {
			Identifier location = sound == null ? null : Identifier.tryParse(sound);
			SoundSource category = null;

			try {
				category = SoundSource.valueOf(categoryName);
			} catch (IllegalArgumentException | NullPointerException e) {
				// Do nothing, if soundId is non-null Minecraft will auto-search, otherwise Minecraft stops all sounds.
			}

			Minecraft.getInstance().getSoundManager().stop(location, category);
		});

		return true;
	}

	@Override
	public boolean showToast(String title, String subtitle) {
		Minecraft.getInstance().execute(() -> Minecraft.getInstance().gui.toastManager().addToast(new SystemToast(SystemToast.SystemToastId.NARRATOR_TOGGLE, Component.literal(title), Component.literal(subtitle))));

		return true;
	}

	@Override
	public boolean showItemToast(String itemData, String title, String subtitle) {
		Minecraft.getInstance().execute(() -> Minecraft.getInstance().gui.toastManager().addToast(new ItemToast(itemData, Component.literal(title), Component.literal(subtitle), core)));

		return true;
	}

	@Override
	public boolean openBook(JsonObject bookJSON) {
		Minecraft.getInstance().execute(() -> {
			DataResult<Pair<WrittenBookContent, JsonElement>> dataResult = WrittenBookContent.CODEC.decode(JsonOps.INSTANCE, bookJSON);

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
			bookStack.set(DataComponents.WRITTEN_BOOK_CONTENT, dataResult.result().get().getFirst());

			BookViewScreen.BookAccess bookContents = BookViewScreen.BookAccess.fromItem(bookStack);

			BookViewScreen screen = new BookViewScreen(bookContents);

			Minecraft.getInstance().setScreenAndShow(screen);
		});

		return true;
	}

	@Override
	public boolean narrate(String message, boolean interrupt) {
		if (narrator.active()) {
			Minecraft.getInstance().execute(() -> narrator.say(message, interrupt, 1.0f));
			return true;
		}

		LOGGER.error("Narrator is unavailable!");

		return false;
	}

	@Override
	public boolean loadWorld(String worldName) {
		Minecraft.getInstance().execute(() -> {
			if (Minecraft.getInstance().getLevelSource().levelExists(worldName)) {
				leaveIfNeeded();

				LOGGER.info("Loading world...");
				Minecraft.getInstance().createWorldOpenFlows().openWorld(worldName, () -> {
					LOGGER.info("World load cancelled!");
					Minecraft.getInstance().setScreenAndShow(new TitleScreen());
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

			LOGGER.info("Payload: " + core.toJson(payload));

			HttpRequest request = HttpRequest.newBuilder(URI.create("https://api.minecraftservices.com/minecraft/profile/skins"))
					.header("Authorization", "Bearer " + Minecraft.getInstance().getUser().getAccessToken())
					.POST(HttpRequest.BodyPublishers.ofString(core.toJson(payload), java.nio.charset.StandardCharsets.UTF_8))
					.header("Content-Type", "application/json")
					.build();

			HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

			if (response.statusCode() == 200 && response.body() != null && response.body().length() > 0) {
				JsonObject responseJSON = core.fromJson(response.body());
				if (responseJSON.has("errorMessage")) {
					LOGGER.warn("Failed to update the skin! " + responseJSON);
					return false;
				}

				LOGGER.debug("Skin Update Response: " + responseJSON);
			} else {
				LOGGER.info("Skin update unsuccessful! HTTP Status: " + response.statusCode());
				if (response.body() != null) LOGGER.info("Response Body: " + response.body());
				return false;
			}

			LOGGER.info("Skin updated!");
			return true;
		} catch (IOException e) {
			LOGGER.warn("Failed to update skin!", e);
			return false;
		} catch (InterruptedException e) {
			LOGGER.warn("Skin update interrupted!", e);
			return false;
		}
	}

	public void leaveIfNeeded() {
		if (Minecraft.getInstance().level != null) {
			LOGGER.info("Disconnecting from world...");

			Minecraft.getInstance().level.disconnect(Component.literal("Loading new world..."));
			Minecraft.getInstance().disconnect(new BlankScreen(), false);
		}
	}

	@Override
	public boolean openScreen(OpenScreenEffect.SCREEN screen) {
		Minecraft.getInstance().execute(() -> {
			leaveIfNeeded();

			switch (screen) {
				case MAIN_MENU:
					Minecraft.getInstance().setScreenAndShow(new TitleScreen());
					break;
				case SERVER_SELECT:
					Minecraft.getInstance().setScreenAndShow(new JoinMultiplayerScreen(new TitleScreen()));
					break;
				case SERVER_DIRECT_CONNECT:
					Minecraft.getInstance().setScreenAndShow(new DirectJoinServerScreen(new JoinMultiplayerScreen(new TitleScreen()), this::connectIfTrue, serverData));
					break;
				case WORLD_SELECT:
					Minecraft.getInstance().setScreenAndShow(new SelectWorldScreen(new TitleScreen()));
					break;
				case WORLD_CREATE:
					CreateWorldScreen.openFresh(Minecraft.getInstance(), () -> {
							Minecraft.getInstance().setScreenAndShow(new SelectWorldScreen(new TitleScreen()));
					});
					break;
				default:
					LOGGER.error("Unknown screen.");
			}
		});
		return true;
	}

	@Override
	public boolean setFOV(int fov) {
		Minecraft.getInstance().execute(() -> Minecraft.getInstance().options.fov().set(fov));
		return true;
	}

	@Override
	public boolean setPOV(SetPovEffect.POV pov) {
		CameraType mcPov;

		switch (pov) {
			default:
			case FIRST_PERSON:
				mcPov = CameraType.FIRST_PERSON;
				break;

			case THIRD_PERSON_BACK:
				mcPov = CameraType.THIRD_PERSON_BACK;
				break;

			case THIRD_PERSON_FRONT:
				mcPov = CameraType.THIRD_PERSON_FRONT;
				break;
		}

		Minecraft.getInstance().execute(() -> Minecraft.getInstance().options.setCameraType(mcPov));
		return true;
	}

	@Override
	public boolean setGuiScale(int scale) {
		if (Minecraft.getInstance().options.guiScale().get().equals(scale)) {
			return true;
		}

		Minecraft.getInstance().execute(() -> {
			Minecraft.getInstance().options.guiScale().set(scale);
			Minecraft.getInstance().options.save();
			Minecraft.getInstance().resizeGui();
		});
		return true;
	}

	@Override
	public boolean setGamma(double gamma) {
		Minecraft.getInstance().execute(() -> {
			Minecraft.getInstance().options.gamma().set(gamma);
			Minecraft.getInstance().options.save();
		});
		return true;
	}

	@Override
	public boolean setChatVisibility(ChatVisibilityEffect.VISIBILITY visibility) {
		ChatVisiblity result;
		switch (visibility) {
			case SHOW:
				result = ChatVisiblity.FULL;
				break;

			case COMMANDS_ONLY:
				result = ChatVisiblity.SYSTEM;
				break;

			case HIDE:
				result = ChatVisiblity.HIDDEN;
				break;

			default:
				return false;
		}

		Minecraft.getInstance().execute(() -> {
			Minecraft.getInstance().options.chatVisibility().set(result);
			Minecraft.getInstance().options.save();
		});
		return true;
	}

	@Override
	public boolean setRenderDistance(int chunks) {
		Minecraft.getInstance().execute(() -> {
			Minecraft.getInstance().options.renderDistance().set(chunks);
			Minecraft.getInstance().options.save();
		});
		return true;
	}

	@Override
	public WorldState getWorldState() {
		if (Minecraft.getInstance().level == null) {
			return WorldState.OTHER;
		}

		return Minecraft.getInstance().isLocalServer() ? WorldState.SINGLEPLAYER : WorldState.MULTIPLAYER;
	}

	@Override
	public String getSPWorldName() {
		if (getWorldState() != WorldState.SINGLEPLAYER) {
			return null;
		}

		IntegratedServer server = Minecraft.getInstance().getSingleplayerServer();

		if (server != null) {
			return server.getWorldData().getLevelName();
		}

		LOGGER.info("Attempted to get SP World Name, but no integrated server was found!");
		return null;
	}

	@Override
	public String getServerIP() {
		if (getWorldState() != WorldState.MULTIPLAYER) {
			return null;
		}

		if (Minecraft.getInstance().getCurrentServer() != null) {
			return Minecraft.getInstance().getCurrentServer().ip;
		}

		LOGGER.info("Failed to get Server IP!");
		return null;
	}

	@Override
	public void setVolume(SetVolumeEffect.VOLUME_CATEGORIES category, int volume) {
		Minecraft.getInstance().execute(() -> {
			SoundSource mcCategory;

			switch (category) {
				case MASTER:
					mcCategory = SoundSource.MASTER;
					break;

				case MUSIC:
					mcCategory = SoundSource.MUSIC;
					break;

				case RECORDS:
					mcCategory = SoundSource.RECORDS;
					break;

				case WEATHER:
					mcCategory = SoundSource.WEATHER;
					break;

				case BLOCKS:
					mcCategory = SoundSource.BLOCKS;
					break;

				case HOSTILE:
					mcCategory = SoundSource.HOSTILE;
					break;

				case NEUTRAL:
					mcCategory = SoundSource.NEUTRAL;
					break;

				case PLAYERS:
					mcCategory = SoundSource.PLAYERS;
					break;

				case AMBIENT:
					mcCategory = SoundSource.AMBIENT;
					break;

				case VOICE:
					mcCategory = SoundSource.VOICE;
					break;

				default:
					LOGGER.error("Unknown volume category!");
					return;
			}

			Minecraft.getInstance().options.getSoundSourceOptionInstance(mcCategory).set(volume / 100.0d);
			Minecraft.getInstance().options.save();
		});
	}

	private void connectIfTrue(boolean connect) {
		if (connect) {
			joinServer(serverData.ip);
		} else {
			Minecraft.getInstance().setScreenAndShow(new JoinMultiplayerScreen(new TitleScreen()));
		}
	}

	public class GameMessageHandler implements ClientReceiveMessageEvents.AllowGame {

		@Override
		public boolean allowReceiveGameMessage(Component message, boolean overlay) {
			if (message.getContents() instanceof TranslatableContents text) {
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
