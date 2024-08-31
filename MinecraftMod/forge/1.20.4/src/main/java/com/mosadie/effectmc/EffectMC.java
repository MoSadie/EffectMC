package com.mosadie.effectmc;

import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.text2speech.Narrator;
import com.mosadie.effectmc.core.EffectExecutor;
import com.mosadie.effectmc.core.EffectMCCore;
import com.mosadie.effectmc.core.WorldState;
import com.mosadie.effectmc.core.effect.*;
import com.mosadie.effectmc.core.effect.internal.EffectRequest;
import com.mosadie.effectmc.core.handler.Device;
import com.mosadie.effectmc.core.handler.DeviceType;
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
import net.minecraft.commands.Commands;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.TagParser;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentContents;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.ChatVisiblity;
import net.minecraft.world.entity.player.PlayerModelPart;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.WrittenBookItem;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.ClientChatReceivedEvent;
import net.neoforged.neoforge.client.event.RegisterClientCommandsEvent;
import net.neoforged.neoforge.common.NeoForge;
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
    private static ServerData serverData = new ServerData("", "", ServerData.Type.OTHER); // Used to hold data during Open Screen

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

        NeoForge.EVENT_BUS.addListener(this::registerClientCommand);
        NeoForge.EVENT_BUS.addListener(this::listenForTranslation);

        Header authHeader = new BasicHeader("Authorization", "Bearer " + Minecraft.getInstance().getUser().getAccessToken());
        List<Header> headers = new ArrayList<>();
        headers.add(authHeader);
        authedClient = HttpClientBuilder.create().setDefaultHeaders(headers).build();
    }

    public void registerClientCommand(RegisterClientCommandsEvent event) {
        LOGGER.info("Registering effectmc command.");
        event.getDispatcher().register(Commands.literal("effectmc")
                .then(Commands.literal("trust").executes((context -> {
                    Minecraft.getInstance().execute(core::setTrustFlag);
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
                }))).then(Commands.literal("exportitem").executes((context -> {
                    if (Minecraft.getInstance().player == null) {
                        LOGGER.info("Null player running exportitem, this shouldn't happen!");
                        return 0;
                    }
                    CompoundTag tag = new CompoundTag();
                    Minecraft.getInstance().player.getMainHandItem().save(tag);
                    LOGGER.info("Held Item Tag: " + NbtUtils.prettyPrint(tag));
                    showItemToast(NbtUtils.prettyPrint(tag), "Exported", Minecraft.getInstance().player.getMainHandItem().getDisplayName().getString());
                    receiveChatMessage("[EffectMC] Exported held item data to log file!");
                    return 0;
                }))).then(Commands.literal("exporteffect").executes(context -> {
                    core.setExportFlag();
                    receiveChatMessage("[EffectMC] Will export the next triggered effect as JSON to the current log file.");
                    return 0;
                })).executes((context -> {
                    receiveChatMessage("[EffectMC] Available subcommands: exportbook, exportitem, trust");
                    return 0;
                })));
        LOGGER.info("Registered effectmc command.");
    }


    private void listenForTranslation(ClientChatReceivedEvent event) {
        Component component = event.getMessage();

        ComponentContents contents = component.getContents();
        if (contents instanceof TranslatableContents translatableContents && translatableContents.getKey().equals(EffectMCCore.TRANSLATION_TRIGGER_KEY)) {
            event.setCanceled(true);

            if (translatableContents.getArgs().length != 1) {
                log("Invalid length of args for translation trigger!");
                return;
            }

            String data = String.valueOf(translatableContents.getArgs()[0]);

            EffectRequest request = core.requestFromJson(data);

            if (request == null) {
                log("Invalid request json for translation trigger!");
                return;
            }

            String worldId = getWorldState() == WorldState.SINGLEPLAYER ? getSPWorldName() : getServerIP();

            Device device = new Device(worldId, getWorldState() == WorldState.SINGLEPLAYER ? DeviceType.WORLD : DeviceType.SERVER);

            core.triggerEffect(device, request);
        }
    }

    @Override
    public void log(String message) {
        LOGGER.info(message);
    }

    @Override
    public boolean joinServer(String serverIp) {
        Minecraft.getInstance().execute(() -> {
            leaveIfNeeded();

            // Create ServerAddress
            if (!ServerAddress.isValidAddress(serverIp)) {
                LOGGER.error("Invalid server IP!");

                DisconnectedScreen errorScreen = new DisconnectedScreen(new JoinMultiplayerScreen(new TitleScreen()), Component.literal("Failed to join server!"), Component.literal("Invalid server IP!"));
                Minecraft.getInstance().setScreen(errorScreen);

                return;
            }

            ServerAddress serverAddress = ServerAddress.parseString(serverIp);
            ServerData serverData = new ServerData("EffectMC", serverIp, ServerData.Type.OTHER);


            LOGGER.info("Connecting to " + serverAddress.getHost());
            // Connect to server

            ConnectScreen.startConnecting(new TitleScreen(), Minecraft.getInstance(), serverAddress, serverData, false);
        });
        return true;
    }

    @Override
    public boolean setSkinLayer(SkinLayerEffect.SKIN_SECTION section, boolean visibility) {
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
    public boolean toggleSkinLayer(SkinLayerEffect.SKIN_SECTION section) {
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
    public void showTrustPrompt(Device device) {
        Minecraft.getInstance().execute(() -> {
            ConfirmScreen screen = new ConfirmScreen(new EffectMCCore.TrustBooleanConsumer(device, core), Component.literal("EffectMC - Trust Prompt"), Component.literal("Do you want to trust this device?\n(Type: " + device.getType() + (device.getType() == DeviceType.OTHER ? " Device Id:" + device.getId() : "") + ")"));
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
        Minecraft.getInstance().execute(() -> Minecraft.getInstance().getToasts().addToast(new SystemToast(SystemToast.SystemToastId.NARRATOR_TOGGLE, Component.literal(title), Component.literal(subtitle))));

        return true;
    }

    @Override
    public boolean showItemToast(String itemData, String title, String subtitle) {
        Minecraft.getInstance().execute(() -> Minecraft.getInstance().getToasts().addToast(new ItemToast(itemData, Component.literal(title), Component.literal(subtitle))));

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
                leaveIfNeeded();

                LOGGER.info("Loading world...");
                Minecraft.getInstance().createWorldOpenFlows().checkForBackupAndLoad(worldName, () -> {
                    LOGGER.info("World load cancelled!");
                    Minecraft.getInstance().setScreen(new TitleScreen());
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
        if (Minecraft.getInstance().level != null) {
            LOGGER.info("Disconnecting from world...");

            Minecraft.getInstance().level.disconnect();
            Minecraft.getInstance().disconnect();
//            Minecraft.getInstance().clearClientLevel(new ProgressScreen(true));
        }
    }

    @Override
    public boolean openScreen(OpenScreenEffect.SCREEN screen) {
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

    private void connectIfTrue(boolean connect) {
        if (connect) {
            joinServer(serverData.ip);
        } else {
            Minecraft.getInstance().setScreen(new JoinMultiplayerScreen(new TitleScreen()));
        }
    }
}
