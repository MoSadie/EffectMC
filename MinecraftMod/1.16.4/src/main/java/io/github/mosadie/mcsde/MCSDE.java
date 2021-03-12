package io.github.mosadie.mcsde;

import io.github.mosadie.mcsde.core.EffectExecutor;
import io.github.mosadie.mcsde.core.MCSDECore;
import io.github.mosadie.mcsde.core.handler.SkinLayerHandler;
import net.minecraft.client.GameSettings;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.*;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.entity.player.PlayerModelPart;
import net.minecraft.realms.RealmsBridgeScreen;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.fml.common.Mod;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.UUID;

@Mod(MCSDE.MODID)
public class MCSDE implements EffectExecutor {
    public final static String MODID = "mcsde";

    private final MCSDECore core;

    public static Logger LOGGER = LogManager.getLogger();

    public MCSDE() {
        LOGGER.info("Starting Core");
        core = new MCSDECore(3000, this);
        LOGGER.info("Core Started");

        LOGGER.info("Starting Server");
        boolean result = core.initServer();
        LOGGER.info("Server start result: " + result);
    }

    @Override
    public String getPlayerName() {
        if  (Minecraft.getInstance().player != null)
            return Minecraft.getInstance().player.getName().getString();
        else
            return null;
    }

    @Override
    public UUID getPlayerUUID() {
        if  (Minecraft.getInstance().player != null)
            return Minecraft.getInstance().player.getUniqueID();
        else
            return null;
    }

    @Override
    public void log(String message) {
        LOGGER.info(message);
    }

    @Override
    public void joinServer(String serverIp) {
        Minecraft.getInstance().enqueue(() -> {
            if (Minecraft.getInstance().world != null) {
                LOGGER.info("Disconnecting from world...");

                Minecraft.getInstance().world.sendQuittingDisconnectingPacket();
                Minecraft.getInstance().unloadWorld(new MultiplayerScreen(new MainMenuScreen()));
            }

            // Create ServerData
            ServerData server = new ServerData("MCSDE", serverIp, false);


            LOGGER.info("Connecting to " + server.serverIP);
            // Connect to server

            ConnectingScreen connectingScreen = new ConnectingScreen(new MainMenuScreen(), Minecraft.getInstance(), server);
            Minecraft.getInstance().displayGuiScreen(connectingScreen);
        });
    }

    @Override
    public void setSkinLayer(SkinLayerHandler.SKIN_SECTION section, boolean visibility) {
        GameSettings gameSettings = Minecraft.getInstance().gameSettings;
        switch (section) {

            case ALL:
                gameSettings.setModelPartEnabled(PlayerModelPart.CAPE, visibility);
                // Fall to ALL_BODY
            case ALL_BODY:
                gameSettings.setModelPartEnabled(PlayerModelPart.HAT, visibility);
                gameSettings.setModelPartEnabled(PlayerModelPart.JACKET, visibility);
                gameSettings.setModelPartEnabled(PlayerModelPart.LEFT_SLEEVE, visibility);
                gameSettings.setModelPartEnabled(PlayerModelPart.LEFT_PANTS_LEG, visibility);
                gameSettings.setModelPartEnabled(PlayerModelPart.RIGHT_SLEEVE, visibility);
                gameSettings.setModelPartEnabled(PlayerModelPart.RIGHT_PANTS_LEG, visibility);
                break;
            case CAPE:
                gameSettings.setModelPartEnabled(PlayerModelPart.CAPE, visibility);
                break;
            case JACKET:
                gameSettings.setModelPartEnabled(PlayerModelPart.JACKET, visibility);
                break;
            case LEFT_SLEEVE:
                gameSettings.setModelPartEnabled(PlayerModelPart.LEFT_SLEEVE, visibility);
                break;
            case RIGHT_SLEEVE:
                gameSettings.setModelPartEnabled(PlayerModelPart.RIGHT_SLEEVE, visibility);
                break;
            case LEFT_PANTS_LEG:
                gameSettings.setModelPartEnabled(PlayerModelPart.LEFT_PANTS_LEG, visibility);
                break;
            case RIGHT_PANTS_LEG:
                gameSettings.setModelPartEnabled(PlayerModelPart.RIGHT_PANTS_LEG, visibility);
                break;
            case HAT:
                gameSettings.setModelPartEnabled(PlayerModelPart.HAT, visibility);
                break;
        }
    }

    @Override
    public void toggleSkinLayer(SkinLayerHandler.SKIN_SECTION section) {
        GameSettings gameSettings = Minecraft.getInstance().gameSettings;
        switch (section) {

            case ALL:
                gameSettings.switchModelPartEnabled(PlayerModelPart.CAPE);
                // Fall to ALL_BODY
            case ALL_BODY:
                gameSettings.switchModelPartEnabled(PlayerModelPart.HAT);
                gameSettings.switchModelPartEnabled(PlayerModelPart.JACKET);
                gameSettings.switchModelPartEnabled(PlayerModelPart.LEFT_SLEEVE);
                gameSettings.switchModelPartEnabled(PlayerModelPart.LEFT_PANTS_LEG);
                gameSettings.switchModelPartEnabled(PlayerModelPart.RIGHT_SLEEVE);
                gameSettings.switchModelPartEnabled(PlayerModelPart.RIGHT_PANTS_LEG);
                break;
            case CAPE:
                gameSettings.switchModelPartEnabled(PlayerModelPart.CAPE);
                break;
            case JACKET:
                gameSettings.switchModelPartEnabled(PlayerModelPart.JACKET);
                break;
            case LEFT_SLEEVE:
                gameSettings.switchModelPartEnabled(PlayerModelPart.LEFT_SLEEVE);
                break;
            case RIGHT_SLEEVE:
                gameSettings.switchModelPartEnabled(PlayerModelPart.RIGHT_SLEEVE);
                break;
            case LEFT_PANTS_LEG:
                gameSettings.switchModelPartEnabled(PlayerModelPart.LEFT_PANTS_LEG);
                break;
            case RIGHT_PANTS_LEG:
                gameSettings.switchModelPartEnabled(PlayerModelPart.RIGHT_PANTS_LEG);
                break;
            case HAT:
                gameSettings.switchModelPartEnabled(PlayerModelPart.HAT);
                break;
        }
    }

    @Override
    public boolean getSkinLayerVisible(SkinLayerHandler.SKIN_SECTION section) {
        GameSettings gameSettings = Minecraft.getInstance().gameSettings;
        switch (section) {

            case CAPE:
                return gameSettings.getModelParts().contains(PlayerModelPart.CAPE);

            case ALL:
            case ALL_BODY:
            case JACKET:
                return gameSettings.getModelParts().contains(PlayerModelPart.JACKET);

            case LEFT_SLEEVE:
                return gameSettings.getModelParts().contains(PlayerModelPart.LEFT_SLEEVE);

            case RIGHT_SLEEVE:
                return gameSettings.getModelParts().contains(PlayerModelPart.RIGHT_SLEEVE);

            case LEFT_PANTS_LEG:
                return gameSettings.getModelParts().contains(PlayerModelPart.LEFT_PANTS_LEG);

            case RIGHT_PANTS_LEG:
                return gameSettings.getModelParts().contains(PlayerModelPart.RIGHT_PANTS_LEG);

            case HAT:
                return gameSettings.getModelParts().contains(PlayerModelPart.HAT);

            default:
                return false;
        }
    }

    @Override
    public void sendChatMessage(String message) {
        if (Minecraft.getInstance().player != null) {
            LOGGER.info("Sending chat message: " + message);
            Minecraft.getInstance().player.sendChatMessage(message);
        }
    }

    @Override
    public void receiveChatMessage(String message) {
        if (Minecraft.getInstance().player != null) {
            LOGGER.info("Showing chat message: " + message);
            Minecraft.getInstance().player.sendMessage(new StringTextComponent(message), Minecraft.getInstance().player.getUniqueID());
        }
    }

    @Override
    public void showTitle(String title, String subtitle) {
        LOGGER.info("Showing Title: " + title + " Subtitle: " + subtitle);
        Minecraft.getInstance().ingameGUI.func_238452_a_(null, new StringTextComponent(subtitle), -1, -1, -1);
        Minecraft.getInstance().ingameGUI.func_238452_a_(new StringTextComponent(title), null, -1, -1, -1);
    }

    private void disconnect() {
        LOGGER.info("Disconnecting from current server");
        boolean flag = Minecraft.getInstance().isIntegratedServerRunning();
        boolean flag1 = Minecraft.getInstance().isConnectedToRealms();


        if (flag) {
            Minecraft.getInstance().unloadWorld(new DirtMessageScreen(new TranslationTextComponent("menu.savingLevel")));
        } else {
            Minecraft.getInstance().unloadWorld();
        }

        if (flag) {
            Minecraft.getInstance().displayGuiScreen(new MainMenuScreen());
        } else if (flag1) {
            RealmsBridgeScreen realmsbridgescreen = new RealmsBridgeScreen();
            realmsbridgescreen.func_231394_a_(new MainMenuScreen());
        } else {
            Minecraft.getInstance().displayGuiScreen(new MultiplayerScreen(new MainMenuScreen()));
        }
    }
}
