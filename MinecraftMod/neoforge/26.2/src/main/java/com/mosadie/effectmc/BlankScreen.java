package com.mosadie.effectmc;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

public class BlankScreen extends Screen {

    protected BlankScreen() {
        super(Component.literal("Loading..."));
    }

    @Override
    public void extractRenderState(@NotNull GuiGraphicsExtractor pGuiGraphics, int pMouseX, int pMouseY, float pPartialTick) {
        this.extractPanorama(pGuiGraphics, pPartialTick);
        super.extractRenderState(pGuiGraphics, pMouseX, pMouseY, pPartialTick);
    }
}
