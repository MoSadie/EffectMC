package io.github.mosadie.mcsde.core;

import io.github.mosadie.mcsde.core.handler.SkinLayerHandler;

import java.util.UUID;

public interface EffectExecutor {
    public String getPlayerName();
    public UUID getPlayerUUID();

    public void log(String message);

    void joinServer(String serverIp);

    void setSkinLayer(SkinLayerHandler.SKIN_SECTION section, boolean visibility);
    void toggleSkinLayer(SkinLayerHandler.SKIN_SECTION section);
    boolean getSkinLayerVisible(SkinLayerHandler.SKIN_SECTION section);

    void sendChatMessage(String message);
    void receiveChatMessage(String message);

    void showTitle(String title, String subtitle);
}
