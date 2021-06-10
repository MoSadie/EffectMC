package io.github.mosadie.effectmc.core;

import com.google.gson.JsonObject;
import io.github.mosadie.effectmc.core.handler.DisconnectHandler;
import io.github.mosadie.effectmc.core.handler.SkinLayerHandler;

public interface EffectExecutor {
    void log(String message);

    void joinServer(String serverIp);

    void setSkinLayer(SkinLayerHandler.SKIN_SECTION section, boolean visibility);
    void toggleSkinLayer(SkinLayerHandler.SKIN_SECTION section);

    void sendChatMessage(String message);
    void receiveChatMessage(String message);

    void showTitle(String title, String subtitle);

    void showActionMessage(String message);

    void showTrustPrompt(String device);

    void triggerDisconnect(DisconnectHandler.NEXT_SCREEN nextScreen, String title, String message);

    void playSound(String soundID, String categoryName, float volume, float pitch, boolean repeat, int repeatDelay, String attenuationType, double x, double y, double z, boolean relative, boolean global);

    void resetScreen();

    void stopSound(String sound, String category);

    void showToast(String title, String subtitle);

    void openBook(JsonObject bookJSON);

    void narrate(String message, boolean interrupt);
}
