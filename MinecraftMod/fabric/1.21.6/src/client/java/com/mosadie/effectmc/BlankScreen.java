package com.mosadie.effectmc;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

public class BlankScreen extends Screen {

    protected BlankScreen() {
        super(Text.of("Loading..."));
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float deltaTicks)  {
        this.renderPanoramaBackground(context, deltaTicks);
        super.render(context, mouseX, mouseY, deltaTicks);
    }
}
