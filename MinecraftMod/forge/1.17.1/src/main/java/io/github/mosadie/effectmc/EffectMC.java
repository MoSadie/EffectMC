package io.github.mosadie.effectmc;

import com.google.gson.JsonObject;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.text2speech.Narrator;
import io.github.mosadie.effectmc.core.EffectExecutor;
import io.github.mosadie.effectmc.core.EffectMCCore;
import io.github.mosadie.effectmc.core.handler.DisconnectHandler;
import io.github.mosadie.effectmc.core.handler.SkinLayerHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.client.gui.components.toasts.SystemToast;
import net.minecraft.client.gui.screens.*;
import net.minecraft.client.gui.screens.inventory.BookViewScreen;
import net.minecraft.client.gui.screens.multiplayer.JoinMultiplayerScreen;
import net.minecraft.client.gui.screens.worldselection.SelectWorldScreen;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.multiplayer.resolver.ServerAddress;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.TagParser;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.PlayerModelPart;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.WrittenBookItem;
import net.minecraftforge.client.event.ClientChatEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;

@Mod(EffectMC.MODID)
public class EffectMC implements EffectExecutor {
    public final static String MODID = "effectmc";

    private final EffectMCCore core;

    public static Logger LOGGER = LogManager.getLogger();

    private static Narrator narrator = Narrator.getNarrator();

    public EffectMC() throws IOException {
        File configDir = ModList.get().getModFileById(MODID).getFile().getFilePath().resolve("../" + MODID + "/").toFile();
        if (!configDir.exists()) {
            if (!configDir.mkdirs()) {
                LOGGER.error("Something went wrong creating the config directory!");
                throw new IOException("Failed to create config directory!");
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

        MinecraftForge.EVENT_BUS.addListener(this::onChat);
    }

    @SubscribeEvent
    public void onChat(ClientChatEvent event) {
        if (event.getMessage().equalsIgnoreCase("/effectmctrust")) {
            Minecraft.getInstance().execute(core::setTrustNextRequest);
            receiveChatMessage("[EffectMC] Now prompting to trust the next request sent.");
            event.setCanceled(true);
        } else if (event.getMessage().equalsIgnoreCase("/effectmcexportbook")) {
            event.setCanceled(true);
            Minecraft.getInstance().execute(() -> {
                if (Minecraft.getInstance().player == null) {
                    return;
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
                    return;
                }

                LOGGER.info("Exported Book JSON: " + bookStack.getTag().toString());
                receiveChatMessage("[EffectMC] Exported the held book to the current log file.");
            });
        }
    }

    @Override
    public void log(String message) {
        LOGGER.info(message);
    }

    @Override
    public void joinServer(String serverIp) {
        Minecraft.getInstance().execute(() -> {
            if (Minecraft.getInstance().level != null) {
                LOGGER.info("Disconnecting from world...");

                Minecraft.getInstance().level.disconnect();
                Minecraft.getInstance().clearLevel();
            }

            // Create ServerAddress
            if (!ServerAddress.isValidAddress(serverIp)) {
                LOGGER.error("Invalid server IP!");

                DisconnectedScreen errorScreen = new DisconnectedScreen(new JoinMultiplayerScreen(new TitleScreen()), new TextComponent("Failed to join server!"), new TextComponent("Invalid server IP!"));
                Minecraft.getInstance().setScreen(errorScreen);

                return;
            }

            ServerAddress serverAddress = ServerAddress.parseString(serverIp);
            ServerData serverData = new ServerData("EffectMC", serverIp, false);


            LOGGER.info("Connecting to " + serverAddress.getHost());
            // Connect to server

            ConnectScreen.startConnecting(new TitleScreen(), Minecraft.getInstance(), serverAddress, serverData);
        });
    }

    @Override
    public void setSkinLayer(SkinLayerHandler.SKIN_SECTION section, boolean visibility) {
        Options options = Minecraft.getInstance().options;

        switch (section) {

            case ALL:
                options.toggleModelPart(PlayerModelPart.CAPE, visibility);
                // Fall to ALL_BODY
            case ALL_BODY:
                options.toggleModelPart(PlayerModelPart.HAT, visibility);
                options.toggleModelPart(PlayerModelPart.JACKET, visibility);
                options.toggleModelPart(PlayerModelPart.LEFT_SLEEVE, visibility);
                options.toggleModelPart(PlayerModelPart.LEFT_PANTS_LEG, visibility);
                options.toggleModelPart(PlayerModelPart.RIGHT_SLEEVE, visibility);
                options.toggleModelPart(PlayerModelPart.RIGHT_PANTS_LEG, visibility);
                break;
            case CAPE:
                options.toggleModelPart(PlayerModelPart.CAPE, visibility);
                break;
            case JACKET:
                options.toggleModelPart(PlayerModelPart.JACKET, visibility);
                break;
            case LEFT_SLEEVE:
                options.toggleModelPart(PlayerModelPart.LEFT_SLEEVE, visibility);
                break;
            case RIGHT_SLEEVE:
                options.toggleModelPart(PlayerModelPart.RIGHT_SLEEVE, visibility);
                break;
            case LEFT_PANTS_LEG:
                options.toggleModelPart(PlayerModelPart.LEFT_PANTS_LEG, visibility);
                break;
            case RIGHT_PANTS_LEG:
                options.toggleModelPart(PlayerModelPart.RIGHT_PANTS_LEG, visibility);
                break;
            case HAT:
                options.toggleModelPart(PlayerModelPart.HAT, visibility);
                break;
        }
    }

    @Override
    public void toggleSkinLayer(SkinLayerHandler.SKIN_SECTION section) {
        Options options = Minecraft.getInstance().options;
        switch (section) {

            case ALL:
                options.toggleModelPart(PlayerModelPart.CAPE, !options.isModelPartEnabled(PlayerModelPart.CAPE));
                // Fall to ALL_BODY
            case ALL_BODY:
                options.toggleModelPart(PlayerModelPart.HAT, !options.isModelPartEnabled(PlayerModelPart.HAT));
                options.toggleModelPart(PlayerModelPart.JACKET, !options.isModelPartEnabled(PlayerModelPart.JACKET));
                options.toggleModelPart(PlayerModelPart.LEFT_SLEEVE, !options.isModelPartEnabled(PlayerModelPart.LEFT_SLEEVE));
                options.toggleModelPart(PlayerModelPart.LEFT_PANTS_LEG, !options.isModelPartEnabled(PlayerModelPart.LEFT_PANTS_LEG));
                options.toggleModelPart(PlayerModelPart.RIGHT_SLEEVE, !options.isModelPartEnabled(PlayerModelPart.RIGHT_SLEEVE));
                options.toggleModelPart(PlayerModelPart.RIGHT_PANTS_LEG, !options.isModelPartEnabled(PlayerModelPart.RIGHT_PANTS_LEG));
                break;
            case CAPE:
                options.toggleModelPart(PlayerModelPart.CAPE, !options.isModelPartEnabled(PlayerModelPart.CAPE));
                break;
            case JACKET:
                options.toggleModelPart(PlayerModelPart.JACKET, !options.isModelPartEnabled(PlayerModelPart.JACKET));
                break;
            case LEFT_SLEEVE:
                options.toggleModelPart(PlayerModelPart.LEFT_SLEEVE, !options.isModelPartEnabled(PlayerModelPart.LEFT_SLEEVE));
                break;
            case RIGHT_SLEEVE:
                options.toggleModelPart(PlayerModelPart.RIGHT_SLEEVE, !options.isModelPartEnabled(PlayerModelPart.RIGHT_SLEEVE));
                break;
            case LEFT_PANTS_LEG:
                options.toggleModelPart(PlayerModelPart.LEFT_PANTS_LEG, !options.isModelPartEnabled(PlayerModelPart.LEFT_PANTS_LEG));
                break;
            case RIGHT_PANTS_LEG:
                options.toggleModelPart(PlayerModelPart.RIGHT_PANTS_LEG, !options.isModelPartEnabled(PlayerModelPart.RIGHT_PANTS_LEG));
                break;
            case HAT:
                options.toggleModelPart(PlayerModelPart.HAT, !options.isModelPartEnabled(PlayerModelPart.HAT));
                break;
        }
    }

    @Override
    public void sendChatMessage(String message) {
        if (Minecraft.getInstance().player != null) {
            LOGGER.info("Sending chat message: " + message);
            Minecraft.getInstance().player.chat(message);
        }
    }

    @Override
    public void receiveChatMessage(String message) {
        if (Minecraft.getInstance().player != null) {
            LOGGER.info("Showing chat message: " + message);
            Minecraft.getInstance().player.sendMessage(new TextComponent(message), Minecraft.getInstance().player.getUUID());
        }
    }

    @Override
    public void showTitle(String title, String subtitle) {
        LOGGER.info("Showing Title: " + title + " Subtitle: " + subtitle);
        Minecraft.getInstance().gui.resetTitleTimes();
        Minecraft.getInstance().gui.setSubtitle(new TextComponent(subtitle));
        Minecraft.getInstance().gui.setTitle(new TextComponent(title));
    }

    @Override
    public void showActionMessage(String message) {
        LOGGER.info("Showing ActionBar message: " + message);
        Minecraft.getInstance().gui.setOverlayMessage(new TextComponent(message), false);
    }

    @Override
    public void triggerDisconnect(DisconnectHandler.NEXT_SCREEN nextScreenType, String title, String message) {
        Minecraft.getInstance().execute(() -> {
            if (Minecraft.getInstance().level != null) {
                LOGGER.info("Disconnecting from world...");

                Minecraft.getInstance().level.disconnect();
                Minecraft.getInstance().clearLevel();
            }

            Screen nextScreen;

            switch (nextScreenType) {
                case MAIN_MENU:
                    nextScreen = new TitleScreen();
                    break;

                case SERVER_SELECT:
                    nextScreen = new JoinMultiplayerScreen(new TitleScreen());
                    break;

                case WORLD_SELECT:
                    nextScreen = new SelectWorldScreen(new TitleScreen());
                    break;

                default:
                    nextScreen = new TitleScreen();
            }

            DisconnectedScreen screen = new DisconnectedScreen(nextScreen, new TextComponent(title), new TextComponent(message));
            Minecraft.getInstance().setScreen(screen);
        });
    }

    @Override
    public void playSound(String soundID, String categoryName, float volume, float pitch, boolean repeat, int repeatDelay, String attenuationType, double x, double y, double z, boolean relative, boolean global) {
        Minecraft.getInstance().execute(() -> {
            ResourceLocation sound = new ResourceLocation(soundID);

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

            if (relative && Minecraft.getInstance().level != null) {
                trueX += Minecraft.getInstance().player.getX();
                trueY += Minecraft.getInstance().player.getY();
                trueZ += Minecraft.getInstance().player.getZ();
            }

            Minecraft.getInstance().getSoundManager().play(new SimpleSoundInstance(sound, category, volume, pitch, repeat, repeatDelay, attenuation, trueX, trueY, trueZ, global));
        });
    }

    @Override
    public void showTrustPrompt(String device) {
        Minecraft.getInstance().execute(() -> {
            ConfirmScreen screen = new ConfirmScreen(new EffectMCCore.TrustBooleanConsumer(device, core), new TextComponent("EffectMC - Trust Prompt"), new TextComponent("Do you want to trust this device? (" + device + ")"));
            Minecraft.getInstance().setScreen(screen);
        });
    }

    @Override
    public void resetScreen() {
        Minecraft.getInstance().execute(() -> Minecraft.getInstance().setScreen(null));
    }

    @Override
    public void stopSound(String sound, String categoryName) {
        Minecraft.getInstance().execute(() -> {
            ResourceLocation location = sound == null ? null : ResourceLocation.tryParse(sound);
            SoundSource category = null;

            try {
                category = SoundSource.valueOf(categoryName);
            } catch (IllegalArgumentException | NullPointerException e) {
                // Do nothing, if soundId is non-null Minecraft will auto-search, otherwise Minecraft stops all sounds.
            }

            Minecraft.getInstance().getSoundManager().stop(location, category);
        });
    }

    @Override
    public void showToast(String title, String subtitle) {
        Minecraft.getInstance().execute(() -> {
            Minecraft.getInstance().getToasts().addToast(new SystemToast(null, new TextComponent(title), new TextComponent(subtitle)));
        });
    }

    @Override
    public void openBook(JsonObject bookJSON) {
        Minecraft.getInstance().execute(() -> {
            CompoundTag tag = null;
            try {
                tag = TagParser.parseTag(bookJSON.toString());
            } catch (CommandSyntaxException e) {
                LOGGER.error("Invalid JSON");
                return;
            }

            if (!WrittenBookItem.makeSureTagIsValid(tag)) {
                LOGGER.error("Invalid Book JSON");
                return;
            }

            ItemStack bookStack = new ItemStack(Items.WRITTEN_BOOK);
            bookStack.setTag(tag);

            BookViewScreen.BookAccess bookInfo = BookViewScreen.BookAccess.fromItem(bookStack);

            BookViewScreen screen = new BookViewScreen(bookInfo);

            Minecraft.getInstance().setScreen(screen);
        });
    }

    @Override
    public void narrate(String message, boolean interrupt) {
        Minecraft.getInstance().execute(() -> {
            narrator.say(message, interrupt);
        });
    }
}