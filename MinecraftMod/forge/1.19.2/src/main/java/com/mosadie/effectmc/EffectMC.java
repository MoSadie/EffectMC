package com.mosadie.effectmc;

import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.text2speech.Narrator;
import com.mosadie.effectmc.core.EffectExecutor;
import com.mosadie.effectmc.core.EffectMCCore;
import com.mosadie.effectmc.core.handler.*;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.client.gui.components.toasts.SystemToast;
import net.minecraft.client.gui.screens.*;
import net.minecraft.client.gui.screens.inventory.BookViewScreen;
import net.minecraft.client.gui.screens.multiplayer.JoinMultiplayerScreen;
import net.minecraft.client.gui.screens.worldselection.CreateWorldScreen;
import net.minecraft.client.gui.screens.worldselection.SelectWorldScreen;
import net.minecraft.client.gui.screens.worldselection.WorldOpenFlows;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.multiplayer.resolver.ServerAddress;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.commands.Commands;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.TagParser;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.WorldStem;
import net.minecraft.server.packs.repository.PackRepository;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.ChatVisiblity;
import net.minecraft.world.entity.player.PlayerModelPart;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.WrittenBookItem;
import net.minecraft.world.level.storage.LevelStorageSource;
import net.minecraftforge.client.event.ClientChatEvent;
import net.minecraftforge.client.event.RegisterClientCommandsEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;
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

@Mod(EffectMC.MODID)
public class EffectMC implements EffectExecutor {
    public final static String MODID = "effectmc";

    private final EffectMCCore core;

    public static Logger LOGGER = LogManager.getLogger();

    private static Narrator narrator = Narrator.getNarrator();
    private static RandomSource randomSource = RandomSource.create();
    private static ServerData serverData = new ServerData("", "", false); // Used to hold data during Open Screen

    private final HttpClient authedClient;

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
        boolean result;
        try {
            result = core.initServer();
        } catch (URISyntaxException e) {
            LOGGER.error("Failed to initialize server due to internal error, please report this!", e);
            result = false;
        }
        LOGGER.info("Server start result: " + result);

        MinecraftForge.EVENT_BUS.addListener(this::registerClientCommand);

        Header authHeader = new BasicHeader("Authorization", "Bearer " + Minecraft.getInstance().getUser().getAccessToken());
        List<Header> headers = new ArrayList<>();
        headers.add(authHeader);
        authedClient = HttpClientBuilder.create().setDefaultHeaders(headers).build();
    }

    public void registerClientCommand(RegisterClientCommandsEvent event) {
        LOGGER.info("Registering effectmc command.");
        event.getDispatcher().register(Commands.literal("effectmc")
                .then(Commands.literal("trust").executes((context -> {
                    Minecraft.getInstance().execute(core::setTrustNextRequest);
                    receiveChatMessage("[EffectMC] Now prompting to trust the next request sent.");
                    return 0;
                })))
                .then(Commands.literal("exportbook").executes((context -> {
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

                    if (bookStack.getTag() == null) {
                        receiveChatMessage("[EffectMC] Failed to export book: Missing tag.");
                        return 0;
                    }

                    LOGGER.info("Exported Book JSON: " + bookStack.getTag());
                    receiveChatMessage("[EffectMC] Exported the held book to the current log file.");
                    return 0;
                }))).executes((context -> {
                    receiveChatMessage("[EffectMC] Available subcommands: exportbook, trust");
                    return 0;
                })));
        LOGGER.info("Registered effectmc command.");
    }

    @Override
    public void log(String message) {
        LOGGER.info(message);
    }

    @Override
    public boolean joinServer(String serverIp) {
        Minecraft.getInstance().execute(() -> {
            if (Minecraft.getInstance().level != null) {
                LOGGER.info("Disconnecting from world...");

                Minecraft.getInstance().level.disconnect();
                Minecraft.getInstance().clearLevel();
            }

            // Create ServerAddress
            if (!ServerAddress.isValidAddress(serverIp)) {
                LOGGER.error("Invalid server IP!");

                DisconnectedScreen errorScreen = new DisconnectedScreen(new JoinMultiplayerScreen(new TitleScreen()), Component.literal("Failed to join server!"), Component.literal("Invalid server IP!"));
                Minecraft.getInstance().setScreen(errorScreen);

                return;
            }

            ServerAddress serverAddress = ServerAddress.parseString(serverIp);
            ServerData serverData = new ServerData("EffectMC", serverIp, false);


            LOGGER.info("Connecting to " + serverAddress.getHost());
            // Connect to server

            ConnectScreen.startConnecting(new TitleScreen(), Minecraft.getInstance(), serverAddress, serverData);
        });
        return true;
    }

    @Override
    public boolean setSkinLayer(SkinLayerHandler.SKIN_SECTION section, boolean visibility) {
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

        return true;
    }

    @Override
    public boolean toggleSkinLayer(SkinLayerHandler.SKIN_SECTION section) {
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

        return true;
    }

    @Override
    public boolean sendChatMessage(String message) {
        if (Minecraft.getInstance().player != null) {
            if (message.startsWith("/")) {
                LOGGER.info("Sending command message: " + message);
                Minecraft.getInstance().player.commandSigned(message.substring(1), null);
            } else {
                LOGGER.info("Sending chat message: " + message);
                Minecraft.getInstance().player.chatSigned(message, null);
            }

            return true;
        }

        return false;
    }

    @Override
    public boolean receiveChatMessage(String message) {
        if (Minecraft.getInstance().player != null) {
            LOGGER.info("Showing chat message: " + message);
            Minecraft.getInstance().player.displayClientMessage(Component.literal(message), false);//.sendMessage(Component.literal(message), Minecraft.getInstance().player.getUUID());

            return true;
        }

        return false;
    }

    @Override
    public boolean showTitle(String title, String subtitle) {
        LOGGER.info("Showing Title: " + title + " Subtitle: " + subtitle);
        Minecraft.getInstance().gui.resetTitleTimes();
        Minecraft.getInstance().gui.setSubtitle(Component.literal(subtitle));
        Minecraft.getInstance().gui.setTitle(Component.literal(title));

        return true;
    }

    @Override
    public boolean showActionMessage(String message) {
        LOGGER.info("Showing ActionBar message: " + message);
        Minecraft.getInstance().gui.setOverlayMessage(Component.literal(message), false);

        return true;
    }

    @Override
    public boolean triggerDisconnect(DisconnectHandler.NEXT_SCREEN nextScreenType, String title, String message) {
        Minecraft.getInstance().execute(() -> {
            if (Minecraft.getInstance().level != null) {
                LOGGER.info("Disconnecting from world...");

                Minecraft.getInstance().level.disconnect();
                Minecraft.getInstance().clearLevel();
            }

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
            Minecraft.getInstance().setScreen(screen);
        });

        return true;
    }

    @Override
    public boolean playSound(String soundID, String categoryName, float volume, float pitch, boolean repeat, int repeatDelay, String attenuationType, double x, double y, double z, boolean relative, boolean global) {
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

            if (relative && Minecraft.getInstance().level != null && Minecraft.getInstance().player != null) {
                trueX += Minecraft.getInstance().player.getX();
                trueY += Minecraft.getInstance().player.getY();
                trueZ += Minecraft.getInstance().player.getZ();
            }

            Minecraft.getInstance().getSoundManager().play(new SimpleSoundInstance(sound, category, volume, pitch, randomSource, repeat, repeatDelay, attenuation, trueX, trueY, trueZ, global));
        });

        return true;
    }

    @Override
    public void showTrustPrompt(String device) {
        Minecraft.getInstance().execute(() -> {
            ConfirmScreen screen = new ConfirmScreen(new EffectMCCore.TrustBooleanConsumer(device, core), Component.literal("EffectMC - Trust Prompt"), Component.literal("Do you want to trust this device? (" + device + ")"));
            Minecraft.getInstance().setScreen(screen);
        });
    }

    @Override
    public void resetScreen() {
        Minecraft.getInstance().execute(() -> Minecraft.getInstance().setScreen(null));
    }

    @Override
    public boolean stopSound(String sound, String categoryName) {
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

        return true;
    }

    @Override
    public boolean showToast(String title, String subtitle) {
        Minecraft.getInstance().execute(() -> Minecraft.getInstance().getToasts().addToast(new SystemToast(SystemToast.SystemToastIds.NARRATOR_TOGGLE, Component.literal(title), Component.literal(subtitle))));

        return true;
    }

    @Override
    public boolean openBook(JsonObject bookJSON) {
        Minecraft.getInstance().execute(() -> {
            CompoundTag tag;
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

        return true;
    }

    @Override
    public boolean narrate(String message, boolean interrupt) {
        if (narrator.active()) {
            Minecraft.getInstance().execute(() -> narrator.say(message, interrupt));
            return true;
        }
        LOGGER.error("Narrator is unavailable!");
        return false;
    }

    @Override
    public boolean loadWorld(String worldName) {
        Minecraft.getInstance().execute(() -> {
            if (Minecraft.getInstance().getLevelSource().levelExists(worldName)) {
                if (Minecraft.getInstance().level != null) {
                    LOGGER.info("Disconnecting from world...");

                    Minecraft.getInstance().level.disconnect();
                    Minecraft.getInstance().clearLevel();
                }

                LOGGER.info("Loading world...");
                Minecraft.getInstance().createWorldOpenFlows().loadLevel(new TitleScreen(), worldName);
            } else {
                LOGGER.warn("World " + worldName + " does not exist!");
            }
        });

        return true;
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
        if (Minecraft.getInstance().level != null) {
            LOGGER.info("Disconnecting from world...");

            Minecraft.getInstance().level.disconnect();
            Minecraft.getInstance().clearLevel();
        }
    }

    @Override
    public boolean openScreen(OpenScreenHandler.SCREEN screen) {
        Minecraft.getInstance().execute(() -> {
            leaveIfNeeded();

            switch (screen) {
                case MAIN_MENU:
                    Minecraft.getInstance().setScreen(new TitleScreen());
                    break;
                case SERVER_SELECT:
                    Minecraft.getInstance().setScreen(new JoinMultiplayerScreen(new TitleScreen()));
                    break;
                case SERVER_DIRECT_CONNECT:
                    Minecraft.getInstance().setScreen(new DirectJoinServerScreen(new JoinMultiplayerScreen(new TitleScreen()), this::connectIfTrue, serverData));
                    break;
                case WORLD_SELECT:
                    Minecraft.getInstance().setScreen(new SelectWorldScreen(new TitleScreen()));
                    break;
                case WORLD_CREATE:
                    CreateWorldScreen.openFresh(Minecraft.getInstance(), new SelectWorldScreen(new TitleScreen()));
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
    public boolean setPOV(SetPovHandler.POV pov) {
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
            Minecraft.getInstance().resizeDisplay();
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
    public boolean setChatVisibility(ChatVisibilityHandler.VISIBILITY visibility) {
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

    private void connectIfTrue(boolean connect) {
        if (connect) {
            joinServer(serverData.ip);
        } else {
            Minecraft.getInstance().setScreen(new JoinMultiplayerScreen(new TitleScreen()));
        }
    }
}
