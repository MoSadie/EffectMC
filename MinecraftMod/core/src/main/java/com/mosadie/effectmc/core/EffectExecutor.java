package com.mosadie.effectmc.core;

import com.google.gson.JsonObject;
import com.mosadie.effectmc.core.effect.*;
import com.mosadie.effectmc.core.handler.Device;
import com.mosadie.effectmc.core.handler.DeviceType;

import java.net.URL;

public interface EffectExecutor {
    void log(String message);

    boolean joinServer(String serverIp);

    boolean setSkinLayer(SkinLayerEffect.SKIN_SECTION section, boolean visibility);
    boolean toggleSkinLayer(SkinLayerEffect.SKIN_SECTION section);

    boolean sendChatMessage(String message);
    boolean receiveChatMessage(String message);

    boolean showTitle(String title, String subtitle);

    boolean showActionMessage(String message);

    void showTrustPrompt(Device device);

    boolean triggerDisconnect(DisconnectEffect.NEXT_SCREEN nextScreen, String title, String message);

    boolean playSound(String soundID, String categoryName, float volume, float pitch, boolean repeat, int repeatDelay, String attenuationType, double x, double y, double z, boolean relative, boolean global);

    void resetScreen();

    boolean stopSound(String sound, String category);

    boolean showToast(String title, String subtitle);

    boolean showItemToast(String itemData, String title, String subtitle);

    boolean openBook(JsonObject bookJSON);

    boolean narrate(String message, boolean interrupt);

    boolean loadWorld(String worldName);

    //boolean refreshSkin(UUID uuid);

    boolean setSkin(URL url, SetSkinEffect.SKIN_TYPE skinType);

    boolean openScreen(OpenScreenEffect.SCREEN screen);

    boolean setFOV(int fov);

    boolean setPOV(SetPovEffect.POV pov);

    boolean setGuiScale(int scale);

    boolean setGamma(double gamma);

    boolean setChatVisibility(ChatVisibilityEffect.VISIBILITY visibility);

    boolean setRenderDistance(int chunks);

    WorldState getWorldState();

    String getSPWorldName();

    String getServerIP();
}
