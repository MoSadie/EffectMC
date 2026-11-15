package com.mosadie.effectmc;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class BlankScreen extends Screen {

    protected BlankScreen() {
        super(Component.literal("Loading..."));
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor context, int mouseX, int mouseY, float deltaTicks)  {
        this.extractPanorama(context, deltaTicks);
        super.extractRenderState(context, mouseX, mouseY, deltaTicks);
    }
}