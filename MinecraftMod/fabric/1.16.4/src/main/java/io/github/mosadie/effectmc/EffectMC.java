package io.github.mosadie.effectmc;

import io.github.mosadie.effectmc.core.EffectExecutor;
import io.github.mosadie.effectmc.core.EffectMCCore;
import io.github.mosadie.effectmc.core.handler.DisconnectHandler;
import io.github.mosadie.effectmc.core.handler.SkinLayerHandler;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.command.v1.ClientCommandManager;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.*;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerScreen;
import net.minecraft.client.gui.screen.world.SelectWorldScreen;
import net.minecraft.client.network.ServerInfo;
import net.minecraft.client.options.GameOptions;
import net.minecraft.client.render.entity.PlayerModelPart;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.client.sound.SoundInstance;
import net.minecraft.client.toast.SystemToast;
import net.minecraft.resource.Resource;
import net.minecraft.sound.SoundCategory;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;

public class EffectMC implements ModInitializer, ClientModInitializer, EffectExecutor {

    public static String MODID = "effectmc";

    private EffectMCCore core;

    public static Logger LOGGER = LogManager.getLogger();

    @Override
    public void onInitialize() {
        LOGGER.info("hello!"); //TODO Remove me!
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
        boolean result = core.initServer();
        LOGGER.info("Server start result: " + result);

        // Register command
        ClientCommandManager.DISPATCHER.register(ClientCommandManager.literal("effectmctrust").executes((context -> {
            MinecraftClient.getInstance().send(core::setTrustNextRequest);
            receiveChatMessage("[EffectMC] Now prompting to trust the next request sent.");
            return 0;
        })));
    }

    @Override
    public void log(String message) {
        LOGGER.info(message);
    }

    @Override
    public void joinServer(String serverIp) {
        MinecraftClient.getInstance().send(() -> {
            if (MinecraftClient.getInstance().world != null) {
                LOGGER.info("Disconnecting from world...");

                MinecraftClient.getInstance().world.disconnect();
                MinecraftClient.getInstance().disconnect();
            }

            // Create ServerInfo
            ServerInfo server = new ServerInfo("EffectMC", serverIp, false);
            
            
            LOGGER.info("Connecting to " + server.address);
            
            // Connect to server

            ConnectScreen connectScreen = new ConnectScreen(new TitleScreen(), MinecraftClient.getInstance(), server);
            MinecraftClient.getInstance().openScreen(connectScreen);
        });
    }

    @Override
    public void setSkinLayer(SkinLayerHandler.SKIN_SECTION section, boolean visibility) {
        GameOptions options = MinecraftClient.getInstance().options;
        
        switch (section) {
            case ALL:
                options.setPlayerModelPart(PlayerModelPart.CAPE, visibility);
                // Fall to ALL_BODY
            case ALL_BODY:
                options.setPlayerModelPart(PlayerModelPart.HAT, visibility);
                options.setPlayerModelPart(PlayerModelPart.JACKET, visibility);
                options.setPlayerModelPart(PlayerModelPart.LEFT_SLEEVE, visibility);
                options.setPlayerModelPart(PlayerModelPart.LEFT_PANTS_LEG, visibility);
                options.setPlayerModelPart(PlayerModelPart.RIGHT_SLEEVE, visibility);
                options.setPlayerModelPart(PlayerModelPart.RIGHT_PANTS_LEG, visibility);
                break;
            case CAPE:
                options.setPlayerModelPart(PlayerModelPart.CAPE, visibility);
                break;
            case JACKET:
                options.setPlayerModelPart(PlayerModelPart.JACKET, visibility);
                break;
            case LEFT_SLEEVE:
                options.setPlayerModelPart(PlayerModelPart.LEFT_SLEEVE, visibility);
                break;
            case RIGHT_SLEEVE:
                options.setPlayerModelPart(PlayerModelPart.RIGHT_SLEEVE, visibility);
                break;
            case LEFT_PANTS_LEG:
                options.setPlayerModelPart(PlayerModelPart.LEFT_PANTS_LEG, visibility);
                break;
            case RIGHT_PANTS_LEG:
                options.setPlayerModelPart(PlayerModelPart.RIGHT_PANTS_LEG, visibility);
                break;
            case HAT:
                options.setPlayerModelPart(PlayerModelPart.HAT, visibility);
                break;
        }
    }

    @Override
    public void toggleSkinLayer(SkinLayerHandler.SKIN_SECTION section) {
        GameOptions options = MinecraftClient.getInstance().options;
        
        switch (section) {
            case ALL:
                options.togglePlayerModelPart(PlayerModelPart.CAPE);
                // Fall to ALL_BODY
            case ALL_BODY:
                options.togglePlayerModelPart(PlayerModelPart.HAT);
                options.togglePlayerModelPart(PlayerModelPart.JACKET);
                options.togglePlayerModelPart(PlayerModelPart.LEFT_SLEEVE);
                options.togglePlayerModelPart(PlayerModelPart.LEFT_PANTS_LEG);
                options.togglePlayerModelPart(PlayerModelPart.RIGHT_SLEEVE);
                options.togglePlayerModelPart(PlayerModelPart.RIGHT_PANTS_LEG);
                break;
            case CAPE:
                options.togglePlayerModelPart(PlayerModelPart.CAPE);
                break;
            case JACKET:
                options.togglePlayerModelPart(PlayerModelPart.JACKET);
                break;
            case LEFT_SLEEVE:
                options.togglePlayerModelPart(PlayerModelPart.LEFT_SLEEVE);
                break;
            case RIGHT_SLEEVE:
                options.togglePlayerModelPart(PlayerModelPart.RIGHT_SLEEVE);
                break;
            case LEFT_PANTS_LEG:
                options.togglePlayerModelPart(PlayerModelPart.LEFT_PANTS_LEG);
                break;
            case RIGHT_PANTS_LEG:
                options.togglePlayerModelPart(PlayerModelPart.RIGHT_PANTS_LEG);
                break;
            case HAT:
                options.togglePlayerModelPart(PlayerModelPart.HAT);
                break;
        }
    }

    @Override
    public void sendChatMessage(String message) {
        if (MinecraftClient.getInstance().player != null) {
            LOGGER.info("Sending chat message: " + message);
            MinecraftClient.getInstance().player.sendChatMessage(message);
        }
    }

    @Override
    public void receiveChatMessage(String message) {
        if (MinecraftClient.getInstance().player != null) {
            LOGGER.info("Showing chat message: " + message);
            MinecraftClient.getInstance().player.sendSystemMessage(Text.of(message), MinecraftClient.getInstance().player.getUuid());
        }
    }

    @Override
    public void showTitle(String title, String subtitle) {
        LOGGER.info("Showing Title: " + title + " Subtitle: " + subtitle);
        MinecraftClient.getInstance().inGameHud.setDefaultTitleFade();
        MinecraftClient.getInstance().inGameHud.setTitles(null, Text.of(subtitle), -1, -1, -1);
        MinecraftClient.getInstance().inGameHud.setTitles(Text.of(title), null, -1, -1, -1);
    }

    @Override
    public void showActionMessage(String message) {
        LOGGER.info("Showing ActionBar message: " + message);
        MinecraftClient.getInstance().inGameHud.setOverlayMessage(Text.of(message), false);
    }

    @Override
    public void showTrustPrompt(String device) {
        MinecraftClient.getInstance().send(() -> {
            ConfirmScreen screen = new ConfirmScreen(new EffectMCCore.TrustBooleanConsumer(device, core), Text.of("EffectMC - Trust Prompt"), Text.of("Do you want to trust this device? (" + device + ")"));
            MinecraftClient.getInstance().openScreen(screen);
        });
    }

    @Override
    public void triggerDisconnect(DisconnectHandler.NEXT_SCREEN nextScreenType, String title, String message) {
        MinecraftClient.getInstance().send(() -> {
            if (MinecraftClient.getInstance().world != null) {
                LOGGER.info("Disconnecting from world...");

                MinecraftClient.getInstance().world.disconnect();
                MinecraftClient.getInstance().disconnect();
            }

            Screen nextScreen;

            switch (nextScreenType) {
                case MAIN_MENU:
                    nextScreen = new TitleScreen();
                    break;

                case SERVER_SELECT:
                    nextScreen = new MultiplayerScreen(new TitleScreen());
                    break;

                case WORLD_SELECT:
                    nextScreen = new SelectWorldScreen(new TitleScreen());
                    break;

                default:
                    nextScreen = new TitleScreen();
                    break;
            }

            DisconnectedScreen screen = new DisconnectedScreen(nextScreen, Text.of(title), Text.of(message));
            MinecraftClient.getInstance().openScreen(screen);
        });
    }

    @Override
    public void playSound(String soundID, String categoryName, float volume, float pitch, boolean repeat, int repeatDelay, String attenuationType, double x, double y, double z, boolean relative, boolean global) {
        MinecraftClient.getInstance().send(() -> {
            Identifier sound = new Identifier(soundID);

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

            if (relative && MinecraftClient.getInstance().world != null) {
                trueX += MinecraftClient.getInstance().player.getX();
                trueY += MinecraftClient.getInstance().player.getY();
                trueZ += MinecraftClient.getInstance().player.getZ();
            }

            MinecraftClient.getInstance().getSoundManager().play(new PositionedSoundInstance(sound, category, volume, pitch, repeat, repeatDelay, attenuation, trueX, trueY, trueZ, global));
        });
    }

    @Override
    public void resetScreen() {
        MinecraftClient.getInstance().send(() -> MinecraftClient.getInstance().openScreen(null));
    }

    @Override
    public void stopSound(String sound, String categoryName) {
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
    }

    @Override
    public void showToast(String title, String subtitle) {
        MinecraftClient.getInstance().send(() -> {
            MinecraftClient.getInstance().getToastManager().add(new SystemToast(null, Text.of(title), Text.of(subtitle)));
        });
    }
}
