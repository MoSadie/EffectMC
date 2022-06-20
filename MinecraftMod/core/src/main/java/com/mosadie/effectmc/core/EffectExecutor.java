package com.mosadie.effectmc.core;

import com.google.gson.JsonObject;
import com.mosadie.effectmc.core.handler.DisconnectHandler;
import com.mosadie.effectmc.core.handler.OpenScreenHandler;
import com.mosadie.effectmc.core.handler.SetSkinHandler;
import com.mosadie.effectmc.core.handler.SkinLayerHandler;

import java.net.URL;

public interface EffectExecutor {
    void log(String message);

    boolean joinServer(String serverIp);

    boolean setSkinLayer(SkinLayerHandler.SKIN_SECTION section, boolean visibility);
    boolean toggleSkinLayer(SkinLayerHandler.SKIN_SECTION section);

    boolean sendChatMessage(String message);
    boolean receiveChatMessage(String message);

    boolean showTitle(String title, String subtitle);

    boolean showActionMessage(String message);

    void showTrustPrompt(String device);

    boolean triggerDisconnect(DisconnectHandler.NEXT_SCREEN nextScreen, String title, String message);

    boolean playSound(String soundID, String categoryName, float volume, float pitch, boolean repeat, int repeatDelay, String attenuationType, double x, double y, double z, boolean relative, boolean global);

    void resetScreen();

    boolean stopSound(String sound, String category);

    boolean showToast(String title, String subtitle);

    boolean openBook(JsonObject bookJSON);

    boolean narrate(String message, boolean interrupt);

    boolean loadWorld(String worldName);

    //boolean refreshSkin(UUID uuid);

    boolean setSkin(URL url, SetSkinHandler.SKIN_TYPE skinType);

    boolean openScreen(OpenScreenHandler.SCREEN screen);
}
