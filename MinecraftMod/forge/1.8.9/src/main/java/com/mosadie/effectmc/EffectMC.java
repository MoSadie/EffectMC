package com.mosadie.effectmc;

import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.mosadie.effectmc.core.EffectExecutor;
import com.mosadie.effectmc.core.EffectMCCore;
import com.mosadie.effectmc.core.handler.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.ISound;
import net.minecraft.client.audio.ISound.AttenuationType;
import net.minecraft.client.audio.SoundCategory;
import net.minecraft.client.gui.GuiCreateWorld;
import net.minecraft.client.gui.GuiDisconnected;
import net.minecraft.client.gui.GuiMainMenu;
import net.minecraft.client.gui.GuiMultiplayer;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiScreenBook;
import net.minecraft.client.gui.GuiScreenServerList;
import net.minecraft.client.gui.GuiSelectWorld;
import net.minecraft.client.gui.GuiYesNo;
import net.minecraft.client.gui.GuiYesNoCallback;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.client.settings.GameSettings.Options;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommand;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer.EnumChatVisibility;
import net.minecraft.entity.player.EnumPlayerModelParts;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemWritableBook;
import net.minecraft.nbt.JsonToNBT;
import net.minecraft.nbt.NBTException;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.stats.Achievement;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.storage.WorldInfo;
import net.minecraftforge.client.ClientCommandHandler;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
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
import java.util.List;

@Mod(modid = EffectMC.MODID, version = EffectMC.VERSION)
public class EffectMC implements EffectExecutor {
	public static final String MODID = "effectmc";
	public static final String VERSION = "2.2";

	public static EffectMCCore core;

	public static Logger LOGGER = LogManager.getLogger();

	private static ServerData serverData = new ServerData("", "", false); // Used to hold data during Open Screen

	private HttpClient authedClient;

	@EventHandler
	public void preInit(FMLPreInitializationEvent event) throws IOException {
		File configDir = event.getModConfigurationDirectory().toPath().resolve("./" + MODID + "/").toFile();
		if (!configDir.exists()) {
			if (!configDir.mkdirs()) {
				LOGGER.error("Something went wrong creating the config directory!");
				throw new IOException("Failed to create config directory!");
			}
		}
		File trustFile = configDir.toPath().resolve("trust.json").toFile();
		File configFile = configDir.toPath().resolve("config.json").toFile();

		LOGGER.info("Starting Core");
		core = new EffectMCCore(configFile, trustFile, this);
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

		// MinecraftForge.EVENT_BUS.addListener(this::onChat);
		ClientCommandHandler.instance.registerCommand(new EffectMCCommand());

		Header authHeader = new BasicHeader("Authorization",
				"Bearer " + Minecraft.getMinecraft().getSession().getToken());
		List<Header> headers = new ArrayList<Header>();
		headers.add(authHeader);
		authedClient = HttpClientBuilder.create().setDefaultHeaders(headers).build();
	}

	public static class EffectMCCommand implements ICommand {

		@Override
		public int compareTo(ICommand other) {
			return this.getCommandName().compareTo(other.getCommandName());
		}

		@Override
		public String getCommandName() {
			return "effectmc";
		}

		@Override
		public String getCommandUsage(ICommandSender sender) {
			return "/effectmc <trust/exportbook>";
		}

		@Override
		public List<String> getCommandAliases() {
			List<String> list = new ArrayList<String>();
			list.add("emc");
			return list;
		}

		@Override
		public void processCommand(ICommandSender sender, String[] args) throws CommandException {
			if (args.length != 1) {
				sender.addChatMessage(new ChatComponentText(getCommandUsage(sender)));
				return;
			}

			// args[0] is the argument

			switch (args[0].toLowerCase()) {
			case "trust":
				Minecraft.getMinecraft().addScheduledTask(core::setTrustNextRequest);
				sender.addChatMessage(
						new ChatComponentText("[EffectMC] Now prompting to trust the next request sent."));
				return;

			case "exportbook":
				Minecraft.getMinecraft().addScheduledTask(() -> {
					if (Minecraft.getMinecraft().thePlayer == null) {
						return;
					}

					ItemStack mainHand = Minecraft.getMinecraft().thePlayer.getHeldItem();

					ItemStack bookStack = null;
					if (mainHand.getItem().equals(Items.written_book)) {
						bookStack = mainHand;
					}

					if (bookStack == null) {
						sender.addChatMessage(
								new ChatComponentText("[EffectMC] Failed to export book: Not holding a book!"));
						return;
					}

					if (bookStack.getTagCompound() == null) {
						sender.addChatMessage(new ChatComponentText("[EffectMC] Failed to export book: Missing tag."));
						return;
					}

					LOGGER.info("Exported Book JSON: " + bookStack.getTagCompound().toString());
					sender.addChatMessage(
							new ChatComponentText("[EffectMC] Exported the held book to the current log file."));
					return;
				});

			default:
				sender.addChatMessage(new ChatComponentText(getCommandUsage(sender)));
				return;
			}
		}

		@Override
		public boolean canCommandSenderUseCommand(ICommandSender sender) {
			return true;
		}

		@Override
		public List<String> addTabCompletionOptions(ICommandSender sender, String[] args, BlockPos pos) {
			return new ArrayList<String>();
		}

		@Override
		public boolean isUsernameIndex(String[] args, int index) {
			return false;
		}

	}

	@Override
	public void log(String message) {
		LOGGER.info(message);
	}

	@Override
	public boolean joinServer(String serverIp) {
		Minecraft.getMinecraft().addScheduledTask(() -> {
			leaveIfNeeded();

			// Create ServerData
			ServerData server = new ServerData("EffectMC", serverIp, false);

			LOGGER.info("Connecting to " + server.serverIP);
			// Connect to server

			net.minecraftforge.fml.client.FMLClientHandler.instance()
					.connectToServer(new GuiMultiplayer(new GuiMainMenu()), server);
			// please work
		});

		return true;
	}

	@Override
	public boolean setSkinLayer(SkinLayerHandler.SKIN_SECTION section, boolean visibility) {
		GameSettings gameSettings = Minecraft.getMinecraft().gameSettings;

		switch (section) {

		case ALL:
			gameSettings.setModelPartEnabled(EnumPlayerModelParts.CAPE, visibility);
			// Fall to ALL_BODY
		case ALL_BODY:
			gameSettings.setModelPartEnabled(EnumPlayerModelParts.HAT, visibility);
			gameSettings.setModelPartEnabled(EnumPlayerModelParts.JACKET, visibility);
			gameSettings.setModelPartEnabled(EnumPlayerModelParts.LEFT_SLEEVE, visibility);
			gameSettings.setModelPartEnabled(EnumPlayerModelParts.LEFT_PANTS_LEG, visibility);
			gameSettings.setModelPartEnabled(EnumPlayerModelParts.RIGHT_SLEEVE, visibility);
			gameSettings.setModelPartEnabled(EnumPlayerModelParts.RIGHT_PANTS_LEG, visibility);
			break;
		case CAPE:
			gameSettings.setModelPartEnabled(EnumPlayerModelParts.CAPE, visibility);
			break;
		case JACKET:
			gameSettings.setModelPartEnabled(EnumPlayerModelParts.JACKET, visibility);
			break;
		case LEFT_SLEEVE:
			gameSettings.setModelPartEnabled(EnumPlayerModelParts.LEFT_SLEEVE, visibility);
			break;
		case RIGHT_SLEEVE:
			gameSettings.setModelPartEnabled(EnumPlayerModelParts.RIGHT_SLEEVE, visibility);
			break;
		case LEFT_PANTS_LEG:
			gameSettings.setModelPartEnabled(EnumPlayerModelParts.LEFT_PANTS_LEG, visibility);
			break;
		case RIGHT_PANTS_LEG:
			gameSettings.setModelPartEnabled(EnumPlayerModelParts.RIGHT_PANTS_LEG, visibility);
			break;
		case HAT:
			gameSettings.setModelPartEnabled(EnumPlayerModelParts.HAT, visibility);
			break;
		}

		return true;
	}

	@Override
	public boolean toggleSkinLayer(SkinLayerHandler.SKIN_SECTION section) {
		GameSettings gameSettings = Minecraft.getMinecraft().gameSettings;
		switch (section) {

		case ALL:
			gameSettings.switchModelPartEnabled(EnumPlayerModelParts.CAPE);
			// Fall to ALL_BODY
		case ALL_BODY:
			gameSettings.switchModelPartEnabled(EnumPlayerModelParts.HAT);
			gameSettings.switchModelPartEnabled(EnumPlayerModelParts.JACKET);
			gameSettings.switchModelPartEnabled(EnumPlayerModelParts.LEFT_SLEEVE);
			gameSettings.switchModelPartEnabled(EnumPlayerModelParts.LEFT_PANTS_LEG);
			gameSettings.switchModelPartEnabled(EnumPlayerModelParts.RIGHT_SLEEVE);
			gameSettings.switchModelPartEnabled(EnumPlayerModelParts.RIGHT_PANTS_LEG);
			break;
		case CAPE:
			gameSettings.switchModelPartEnabled(EnumPlayerModelParts.CAPE);
			break;
		case JACKET:
			gameSettings.switchModelPartEnabled(EnumPlayerModelParts.JACKET);
			break;
		case LEFT_SLEEVE:
			gameSettings.switchModelPartEnabled(EnumPlayerModelParts.LEFT_SLEEVE);
			break;
		case RIGHT_SLEEVE:
			gameSettings.switchModelPartEnabled(EnumPlayerModelParts.RIGHT_SLEEVE);
			break;
		case LEFT_PANTS_LEG:
			gameSettings.switchModelPartEnabled(EnumPlayerModelParts.LEFT_PANTS_LEG);
			break;
		case RIGHT_PANTS_LEG:
			gameSettings.switchModelPartEnabled(EnumPlayerModelParts.RIGHT_PANTS_LEG);
			break;
		case HAT:
			gameSettings.switchModelPartEnabled(EnumPlayerModelParts.HAT);
			break;
		}

		return true;
	}

	@Override
	public boolean sendChatMessage(String message) {
		if (Minecraft.getMinecraft().thePlayer != null) {
			LOGGER.info("Sending chat message: " + message);
			Minecraft.getMinecraft().thePlayer.sendChatMessage(message);
			return true;
		}

		return false;
	}

	@Override
	public boolean receiveChatMessage(String message) {
		if (Minecraft.getMinecraft().thePlayer != null) {
			LOGGER.info("Showing chat message: " + message);
			Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText(message));
			return true;
		}
		return false;
	}

	@Override
	public boolean showTitle(String title, String subtitle) {
		LOGGER.info("Showing Title: " + title + " Subtitle: " + subtitle);
		Minecraft.getMinecraft().ingameGUI.displayTitle(null, null, -1, -1, -1);
		Minecraft.getMinecraft().ingameGUI.displayTitle(null, subtitle, -1, -1, -1);
		Minecraft.getMinecraft().ingameGUI.displayTitle(title, null, -1, -1, -1);
		return true;
	}

	@Override
	public boolean showActionMessage(String message) {
		LOGGER.info("Showing ActionBar message: " + message);
		Minecraft.getMinecraft().ingameGUI.setRecordPlayingMessage(message); // Closest thing in this version.
		return true;
	}

	@Override
	public boolean triggerDisconnect(DisconnectHandler.NEXT_SCREEN nextScreenType, String title, String message) {
		Minecraft.getMinecraft().addScheduledTask(() -> {
			leaveIfNeeded();

			GuiScreen nextScreen;

			switch (nextScreenType) {
			default:
			case MAIN_MENU:
				nextScreen = new GuiMainMenu();
				break;

			case SERVER_SELECT:
				nextScreen = new GuiMultiplayer(new GuiMainMenu());
				break;

			case WORLD_SELECT:
				nextScreen = new GuiSelectWorld(new GuiMainMenu());
				break;
			}

			GuiDisconnected screen = new GuiDisconnected(nextScreen, title, new ChatComponentText(message)); // Note: if
																												// title
																												// matches
																												// a
																												// translation
																												// it'll
																												// show
																												// translation
																												// instead.
			Minecraft.getMinecraft().displayGuiScreen(screen);
		});
		return true;
	}

	@Override
	public boolean playSound(String soundID, String categoryName, float volume, float pitch, boolean repeat,
			int repeatDelay, String attenuationType, double x, double y, double z, boolean relative, boolean global) {
		Minecraft.getMinecraft().addScheduledTask(() -> {
			ResourceLocation sound = new ResourceLocation(soundID);

			SoundCategory category;
			try {
				category = SoundCategory.valueOf(categoryName.toUpperCase());
			} catch (IllegalArgumentException e) {
				category = SoundCategory.MASTER;
			}

			final ISound.AttenuationType attenuation = parseAttenuation(attenuationType);

			final double trueX;
			final double trueY;
			final double trueZ;

			if (relative && Minecraft.getMinecraft().theWorld != null && Minecraft.getMinecraft().thePlayer != null) {
				trueX = x + Minecraft.getMinecraft().thePlayer.posX;
				trueY = y + Minecraft.getMinecraft().thePlayer.posY;
				trueZ = z + Minecraft.getMinecraft().thePlayer.posX;
			} else {
				trueX = x;
				trueY = y;
				trueZ = z;
			}

			ISound soundObj = new ISound() {

				@Override
				public ResourceLocation getSoundLocation() {
					return sound;
				}

				@Override
				public boolean canRepeat() {
					return repeat;
				}

				@Override
				public int getRepeatDelay() {
					return repeatDelay;
				}

				@Override
				public float getVolume() {
					return volume;
				}

				@Override
				public float getPitch() {
					return pitch;
				}

				@Override
				public float getXPosF() {
					return (float) trueX;
				}

				@Override
				public float getYPosF() {
					return (float) trueY;
				}

				@Override
				public float getZPosF() {
					return (float) trueZ;
				}

				@Override
				public AttenuationType getAttenuationType() {
					// TODO Auto-generated method stub
					return attenuation;
				}
			};

			Minecraft.getMinecraft().getSoundHandler().playSound(soundObj);
		});
		return true;
	}

	private AttenuationType parseAttenuation(String attenuationType) {
		try {
			return ISound.AttenuationType.valueOf(attenuationType.toUpperCase());
		} catch (IllegalArgumentException e) {
			LOGGER.info("Failed to parse attenuation type!", e);
			return AttenuationType.NONE;
		}
	}

	@Override
	public void showTrustPrompt(String device) {
		Minecraft.getMinecraft().addScheduledTask(() -> {
//        	EffectMCCore.TrustBooleanConsumer consumer = new EffectMCCore.TrustBooleanConsumer(device, core);
			GuiYesNoCallback callback = new GuiYesNoCallback() {

				GuiScreen currentScreen = Minecraft.getMinecraft().currentScreen;

				@Override
				public void confirmClicked(boolean result, int id) {
					EffectMCCore.TrustBooleanConsumer consumer = new EffectMCCore.TrustBooleanConsumer(device, core);
					consumer.accept(result);
					Minecraft.getMinecraft().displayGuiScreen(currentScreen);
				}

			};
			GuiYesNo screen = new GuiYesNo(callback, "EffectMC - Trust Prompt",
					"Do you want to trust this device? (" + device + ")", 0);
			Minecraft.getMinecraft().displayGuiScreen(screen);
		});
	}

	@Override
	public void resetScreen() {
		Minecraft.getMinecraft().addScheduledTask(() -> Minecraft.getMinecraft().displayGuiScreen(null));
	}

	@Override
	public boolean stopSound(String sound, String categoryName) {
		if (sound != null || categoryName != null) {
			LOGGER.warn("Can't stop specific sounds in this version, sorry!");
			return false;
		}
		Minecraft.getMinecraft().addScheduledTask(() -> {
			Minecraft.getMinecraft().getSoundHandler().stopSounds();
		});
		return true;
	}

	@Override
	public boolean showToast(String title, String subtitle) {
		Achievement ach = new ToastAchievement(title, subtitle);
		Minecraft.getMinecraft()
				.addScheduledTask(() -> Minecraft.getMinecraft().guiAchievement.displayUnformattedAchievement(ach));
		return true;
	}

	@Override
	public boolean openBook(JsonObject bookJSON) {
		if (Minecraft.getMinecraft().thePlayer == null) {
			return false;
		}

		NBTTagCompound nbt;
		try {
			nbt = JsonToNBT.getTagFromJson(bookJSON.toString());
		} catch (NBTException e) {
			LOGGER.error("Invalid JSON");
			return false;
		}

		if (!ItemWritableBook.isNBTValid(nbt)) {
			LOGGER.error("Invalid Book JSON");
			return false;
		}

		ItemStack bookStack = new ItemStack(Items.written_book);
		bookStack.setTagCompound(nbt);

		GuiScreenBook screen = new GuiScreenBook(Minecraft.getMinecraft().thePlayer, bookStack, false);

		Minecraft.getMinecraft().addScheduledTask(() -> Minecraft.getMinecraft().displayGuiScreen(screen));
		return true;
	}

	@Override
	public boolean narrate(String message, boolean interrupt) {
		LOGGER.info("Narrator unsupported on this version :(");
		return false;
	}

	@Override
	public boolean loadWorld(String worldName) {
		if (Minecraft.getMinecraft().getSaveLoader().canLoadWorld(worldName)) {
			Minecraft.getMinecraft().addScheduledTask(() -> {
				leaveIfNeeded();

				LOGGER.info("Loading world...");
				WorldInfo worldInfo = Minecraft.getMinecraft().getSaveLoader().getWorldInfo(worldName);
				net.minecraftforge.fml.client.FMLClientHandler.instance().tryLoadExistingWorld(
						new GuiSelectWorld(new GuiMainMenu()), worldName, worldInfo.getWorldName());
			});

			return true;
		}

		LOGGER.info("Cannot load world!");

		return false;
	}

	@Override
	public boolean setSkin(URL skinUrl, SetSkinHandler.SKIN_TYPE skinType) {
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
				JsonObject responseJSON = core
						.fromJson(IOUtils.toString(response.getEntity().getContent(), StandardCharsets.UTF_8));
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
		if (Minecraft.getMinecraft().theWorld != null) {
			LOGGER.info("Disconnecting from world...");

			Minecraft.getMinecraft().theWorld.sendQuittingDisconnectingPacket();
			Minecraft.getMinecraft().loadWorld(null); // best guess
		}
	}

	@Override
	public boolean openScreen(OpenScreenHandler.SCREEN screen) {
		Minecraft.getMinecraft().addScheduledTask(() -> {
			leaveIfNeeded();

			switch (screen) {
			case MAIN_MENU:
				Minecraft.getMinecraft().displayGuiScreen(new GuiMainMenu());
				break;
			case SERVER_SELECT:
				Minecraft.getMinecraft().displayGuiScreen(new GuiMultiplayer(new GuiMainMenu()));
				break;
			case SERVER_DIRECT_CONNECT:
				Minecraft.getMinecraft()
						.displayGuiScreen(new GuiScreenServerList(new GuiMultiplayer(new GuiMainMenu()), serverData));
				break;
			case WORLD_SELECT:
				Minecraft.getMinecraft().displayGuiScreen(new GuiSelectWorld(new GuiMainMenu()));
				break;
			case WORLD_CREATE:
				Minecraft.getMinecraft().displayGuiScreen(new GuiCreateWorld(new GuiSelectWorld(new GuiMainMenu())));
				break;
			default:
				LOGGER.error("Unknown screen.");
			}
		});
		return true;
	}

	@Override
	public boolean setFOV(int fov) {
		Minecraft.getMinecraft().addScheduledTask(() -> Minecraft.getMinecraft().gameSettings.fovSetting = fov);
		return true;
	}

	@Override
	public boolean setPOV(SetPovHandler.POV pov) {
		int mcPov;

		switch (pov) {
		default:
		case FIRST_PERSON:
			mcPov = 0;
			break;

		case THIRD_PERSON_BACK:
			mcPov = 1;
			break;

		case THIRD_PERSON_FRONT:
			mcPov = 2;
			break;
		}

		Minecraft.getMinecraft().addScheduledTask(() -> Minecraft.getMinecraft().gameSettings.thirdPersonView = mcPov);
		return true;
	}

	@Override
	public boolean setGuiScale(int scale) {
		if (Minecraft.getMinecraft().gameSettings.guiScale == scale) {
			return true;
		}

		Minecraft.getMinecraft().addScheduledTask(() -> {
			Minecraft.getMinecraft().gameSettings.guiScale = scale;
			Minecraft.getMinecraft().gameSettings.saveOptions();
//            Minecraft.getMinecraft().updateDisplay();
		});
		return true;
	}

	@Override
	public boolean setGamma(double gamma) {
		Minecraft.getMinecraft().gameSettings.setOptionFloatValue(Options.GAMMA, (float) gamma);
		return true;
	}

	@Override
	public boolean setChatVisibility(ChatVisibilityHandler.VISIBILITY visibility) {
		EnumChatVisibility result;
		switch (visibility) {
		case SHOW:
			result = EnumChatVisibility.FULL;
			break;

		case COMMANDS_ONLY:
			result = EnumChatVisibility.SYSTEM;
			break;

		case HIDE:
			result = EnumChatVisibility.HIDDEN;
			break;

		default:
			return false;
		}

		Minecraft.getMinecraft().addScheduledTask(() -> {
			Minecraft.getMinecraft().gameSettings.chatVisibility = result;
			Minecraft.getMinecraft().gameSettings.saveOptions();
		});
		return true;
	}

	@Override
	public boolean setRenderDistance(int chunks) {
		Minecraft.getMinecraft().gameSettings.setOptionValue(Options.RENDER_DISTANCE, chunks);
//    	Minecraft.getMinecraft().gameSettings.renderDistanceChunks = chunks;
//    	Minecraft.getMinecraft().gameSettings.saveOptions();
		return true;
	}
}
