package io.github.mosadie.effectmc.core;

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

    void resetScreen();
}
