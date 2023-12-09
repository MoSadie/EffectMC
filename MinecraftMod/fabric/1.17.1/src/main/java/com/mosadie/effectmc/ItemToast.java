package com.mosadie.effectmc;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.toast.Toast;
import net.minecraft.client.toast.ToastManager;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.StringNbtReader;
import net.minecraft.text.Text;

public class ItemToast implements Toast {
    private ItemStack item;
    private Text title;
    private Text body;

    private long lastChanged;
    private boolean changed = true;

    public ItemToast(String itemData, Text title, Text body) {
        try {
            this.item = ItemStack.fromNbt(StringNbtReader.parse(itemData));
        } catch (CommandSyntaxException e) {
            EffectMC.LOGGER.error("Invalid Item Data for Item Toast", e);
            this.item = new ItemStack(Items.AIR);
        }
        this.title = title;
        this.body = body;
    }
    @Override
    public Visibility draw(MatrixStack matrices, ToastManager manager, long time) {
        if (changed) {
            lastChanged = time;
            changed = false;
        }

        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, TEXTURE);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        manager.drawTexture(matrices, 0, 0, 0, 32, this.getWidth(), this.getHeight());
        manager.getGame().textRenderer.draw(matrices, title, 30.0F, 7.0F, -16777216);
        manager.getGame().textRenderer.draw(matrices, body, 30.0F, 18.0F, -16777216);
        RenderSystem.applyModelViewMatrix();
        manager.getGame().getItemRenderer().renderInGui(item, 8, 8);
        return (double)(time - this.lastChanged) >= 5000.0D ? Toast.Visibility.HIDE : Toast.Visibility.SHOW;
    }
}
