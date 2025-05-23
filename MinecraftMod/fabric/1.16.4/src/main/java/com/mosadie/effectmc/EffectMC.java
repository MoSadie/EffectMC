package com.mosadie.effectmc;

import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
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
import net.fabricmc.fabric.api.client.command.v1.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v1.FabricClientCommandSource;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.*;
import net.minecraft.client.gui.screen.ingame.BookScreen;
import net.minecraft.client.gui.screen.ingame.BookScreen.Contents;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerScreen;
import net.minecraft.client.gui.screen.world.CreateWorldScreen;
import net.minecraft.client.gui.screen.world.SelectWorldScreen;
import net.minecraft.client.network.ServerInfo;
import net.minecraft.client.options.ChatVisibility;
import net.minecraft.client.options.GameOptions;
import net.minecraft.client.options.Option;
import net.minecraft.client.options.Perspective;
import net.minecraft.client.render.entity.PlayerModelPart;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.client.sound.SoundInstance;
import net.minecraft.client.toast.SystemToast;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.WrittenBookItem;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.StringNbtReader;
import net.minecraft.server.integrated.IntegratedServer;
import net.minecraft.sound.SoundCategory;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;
import net.minecraft.world.level.ServerWorldProperties;
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

public class EffectMC implements ModInitializer, ClientModInitializer, EffectExecutor {

    public static String MODID = "effectmc";

    private EffectMCCore core;

    public static Logger LOGGER = LogManager.getLogger();

    private static Narrator narrator = Narrator.getNarrator();
    private static ServerInfo serverInfo = new ServerInfo("", "", false); // Used to hold data during Open Screen

    private HttpClient authedClient;

    @Override
    public void onInitialize() {
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

        // Register command
        ClientCommandManager.DISPATCHER.register(clientCommand());

        Header authHeader = new BasicHeader("Authorization", "Bearer " + MinecraftClient.getInstance().getSession().getAccessToken());
        List<Header> headers = new ArrayList<>();
        headers.add(authHeader);
        authedClient = HttpClientBuilder.create().setDefaultHeaders(headers).build();
    }

    private LiteralArgumentBuilder<FabricClientCommandSource> clientCommand() {
        return ClientCommandManager.literal("effectmc")
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

                    if (bookStack.getTag() == null) {
                        receiveChatMessage("[EffectMC] Failed to export book: Missing tag.");
                        return 0;
                    }

                    LOGGER.info("Exported Book JSON: " + bookStack.getTag());
                    receiveChatMessage("[EffectMC] Exported the held book to the current log file.");
                    return 0;
                }))).then(ClientCommandManager.literal("exportitem").executes((context -> {
                    if (MinecraftClient.getInstance().player == null) {
                        LOGGER.info("Null player running exportitem, this shouldn't happen!");
                        return 0;
                    }
                    CompoundTag tag = new CompoundTag();
                    MinecraftClient.getInstance().player.getMainHandStack().toTag(tag);
                    LOGGER.info("Held Item Tag: " + tag);
                    showItemToast(tag.toString(), "Exported", MinecraftClient.getInstance().player.getMainHandStack().getName().getString());
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
                        case SUCCESS:
                            receiveChatMessage("[EffectMC] Effect \"" + request.getEffectId() + "\" triggered successfully: " + result.message);
                            break;
                        case ERROR:
                            receiveChatMessage("[EffectMC] Error triggering effect: " + result.message);
                            break;
                        case UNAUTHORIZED:
                            receiveChatMessage("[EffectMC] World/Server not trusted. Use /effectmc trust to trust the current world/server.");
                            break;
                        case UNKNOWN:
                            receiveChatMessage("[EffectMC] Unknown effect.");
                            break;
                        case SKIPPED:
                            receiveChatMessage("[EffectMC] Effect skipped: " + result.message);
                            break;
                        case UNSUPPORTED:
                            receiveChatMessage("[EffectMC] Effect unsupported: " + result.message);
                            break;
                    }

                    return 0;
                })))).executes((context -> {
                    receiveChatMessage("[EffectMC] Available subcommands: exportbook, exportitem, exporteffect, trigger, trust");
                    return 0;
                }));
    }

    @Override
    public void log(String message) {
        LOGGER.info(message);
    }

    @Override
    public boolean joinServer(String serverIp) {
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
        return true;
    }

    @Override
    public boolean setSkinLayer(SkinLayerEffect.SKIN_SECTION section, boolean visibility) {
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

        return true;
    }

    @Override
    public boolean toggleSkinLayer(SkinLayerEffect.SKIN_SECTION section) {
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

        return true;
    }

    @Override
    public boolean sendChatMessage(String message) {
        if (MinecraftClient.getInstance().player != null) {
            LOGGER.info("Sending chat message: " + message);
            MinecraftClient.getInstance().player.sendChatMessage(message);
            return true;
        }

        return false;
    }

    @Override
    public boolean receiveChatMessage(String message) {
        if (MinecraftClient.getInstance().player != null) {
            LOGGER.info("Showing chat message: " + message);
            MinecraftClient.getInstance().player.sendSystemMessage(Text.of(message), MinecraftClient.getInstance().player.getUuid());
            return true;
        }

        return false;
    }

    @Override
    public boolean showTitle(String title, String subtitle) {
        LOGGER.info("Showing Title: " + title + " Subtitle: " + subtitle);
        MinecraftClient.getInstance().inGameHud.setDefaultTitleFade();
        MinecraftClient.getInstance().inGameHud.setTitles(null, Text.of(subtitle), -1, -1, -1);
        MinecraftClient.getInstance().inGameHud.setTitles(Text.of(title), null, -1, -1, -1);

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
            MinecraftClient.getInstance().openScreen(screen);
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
            MinecraftClient.getInstance().openScreen(screen);
        });
        return true;
    }

    @Override
    public boolean playSound(String soundID, String categoryName, float volume, float pitch, boolean repeat, int repeatDelay, String attenuationType, double x, double y, double z, boolean relative, boolean global) {
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

            if (relative && MinecraftClient.getInstance().world != null && MinecraftClient.getInstance().player != null) {
                trueX += MinecraftClient.getInstance().player.getX();
                trueY += MinecraftClient.getInstance().player.getY();
                trueZ += MinecraftClient.getInstance().player.getZ();
            }

            MinecraftClient.getInstance().getSoundManager().play(new PositionedSoundInstance(sound, category, volume, pitch, repeat, repeatDelay, attenuation, trueX, trueY, trueZ, global));
        });
        return true;
    }

    @Override
    public void resetScreen() {
        MinecraftClient.getInstance().send(() -> MinecraftClient.getInstance().openScreen(null));
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
        MinecraftClient.getInstance().send(() -> MinecraftClient.getInstance().getToastManager().add(new ItemToast(itemData, Text.of(title), Text.of(subtitle))));

        return true;
    }

    @Override
    public boolean openBook(JsonObject bookJSON) {
        MinecraftClient.getInstance().send(() -> {
            CompoundTag tag;
            try {
                tag = StringNbtReader.parse(bookJSON.toString());
            } catch (CommandSyntaxException e) {
                LOGGER.error("Invalid JSON");
                return;
            }

            if (!WrittenBookItem.isValid(tag)) {
                LOGGER.error("Invalid Book JSON");
                return;
            }

            ItemStack bookStack = new ItemStack(Items.WRITTEN_BOOK);
            bookStack.setTag(tag);

            Contents bookContents = Contents.create(bookStack);

            BookScreen screen = new BookScreen(bookContents);

            MinecraftClient.getInstance().openScreen(screen);
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
                leaveIfNeeded();

                LOGGER.info("Loading world...");
                MinecraftClient.getInstance().startIntegratedServer(worldName);
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
                    MinecraftClient.getInstance().openScreen(new TitleScreen());
                    break;
                case SERVER_SELECT:
                    MinecraftClient.getInstance().openScreen(new MultiplayerScreen(new TitleScreen()));
                    break;
                case SERVER_DIRECT_CONNECT:
                    MinecraftClient.getInstance().openScreen(new DirectConnectScreen(new MultiplayerScreen(new TitleScreen()), this::connectIfTrue, serverInfo));
                    break;
                case WORLD_SELECT:
                    MinecraftClient.getInstance().openScreen(new SelectWorldScreen(new TitleScreen()));
                    break;
                case WORLD_CREATE:
                    MinecraftClient.getInstance().openScreen(CreateWorldScreen.method_31130(new SelectWorldScreen(new TitleScreen())));
                    break;
                default:
                    LOGGER.error("Unknown screen.");
            }
        });
        return true;
    }

    @Override
    public boolean setFOV(int fov) {
        MinecraftClient.getInstance().execute(() -> Option.FOV.set(MinecraftClient.getInstance().options, fov));
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
        if (MinecraftClient.getInstance().options.guiScale == scale) {
            return true;
        }

        MinecraftClient.getInstance().execute(() -> {
            MinecraftClient.getInstance().options.guiScale = scale;
            MinecraftClient.getInstance().options.write();
            MinecraftClient.getInstance().onResolutionChanged();
        });
        return true;
    }

    @Override
    public boolean setGamma(double gamma) {
        Option.GAMMA.set(MinecraftClient.getInstance().options, gamma);
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
            MinecraftClient.getInstance().options.chatVisibility = result;
            MinecraftClient.getInstance().options.write();
        });
        return true;
    }

    @Override
    public boolean setRenderDistance(int chunks) {
        Option.RENDER_DISTANCE.set(MinecraftClient.getInstance().options, chunks);
        return true;
    }

    @Override
    public WorldState getWorldState() {
        if (MinecraftClient.getInstance().world == null) {
            return WorldState.OTHER;
        }

        return MinecraftClient.getInstance().isIntegratedServerRunning() ? WorldState.SINGLEPLAYER : WorldState.MULTIPLAYER;
    }

    @Override
    public String getSPWorldName() {
        if (getWorldState() != WorldState.SINGLEPLAYER) {
            return null;
        }

        try {
            IntegratedServer server = MinecraftClient.getInstance().getServer();

            if (server != null && server.getWorld(World.OVERWORLD) != null && server.getWorld(World.OVERWORLD).getLevelProperties() instanceof ServerWorldProperties) {
                return ((ServerWorldProperties) server.getWorld(World.OVERWORLD).getLevelProperties()).getLevelName();
            }

            LOGGER.info("Attempted to get SP World Name, but no integrated server was found!");
            return null;
        } catch (NullPointerException e) {
            LOGGER.error("Failed to get SP World Name! Nullpointer");
            return null;
        }
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

            MinecraftClient.getInstance().options.setSoundVolume(mcCategory, (volume / 100.0f));
            MinecraftClient.getInstance().options.write();
        });
    }

    private void connectIfTrue(boolean connect) {
        if (connect) {
            joinServer(serverInfo.address);
        } else {
            MinecraftClient.getInstance().openScreen(new MultiplayerScreen(new TitleScreen()));
        }
    }

    public void handleTranslationTrigger(TranslatableText text) {
        if (!text.getKey().equals(EffectMCCore.TRANSLATION_TRIGGER_KEY)) {
            LOGGER.error("Received non-trigger message!");
            return;
        }

        if (text.getArgs().length == 0) {
            LOGGER.error("Received trigger message with no arguments!");
            return;
        }

        EffectRequest request = core.requestFromJson(String.valueOf(text.getArgs()[0]));

        if (request == null) {
            LOGGER.error("Failed to parse request!");
            return;
        }

        String worldId = getWorldState() == WorldState.SINGLEPLAYER ? getSPWorldName() : getServerIP();

        Device device = new Device(worldId, getWorldState() == WorldState.SINGLEPLAYER ? DeviceType.WORLD : DeviceType.SERVER);

        core.triggerEffect(device, request);
    }
}
