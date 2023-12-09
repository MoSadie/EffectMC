package com.mosadie.effectmc;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.client.gui.toasts.IToast;
import net.minecraft.client.gui.toasts.ToastGui;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.JsonToNBT;
import net.minecraft.util.text.TextComponent;

public class ItemToast implements IToast {
    private ItemStack item;
    private TextComponent title;
    private TextComponent body;

    private long lastChanged;
    private boolean changed = true;

    public ItemToast(String itemData, TextComponent title, TextComponent body) {
        try {
            this.item = ItemStack.read(JsonToNBT.getTagFromJson(itemData));
        } catch (CommandSyntaxException e) {
            EffectMC.LOGGER.error("Invalid Item Data for Item Toast", e);
            this.item = new ItemStack(Items.AIR, 1, null);
        }
        this.title = title;
        this.body = body;
    }

    @Override
    public Visibility func_230444_a_(MatrixStack matrixStack, ToastGui toastGui, long time) {
        if (changed) {
            lastChanged = time;
            changed = false;
        }

        toastGui.getMinecraft().getTextureManager().bindTexture(TEXTURE_TOASTS);
        RenderSystem.color3f(1.0F, 1.0F, 1.0F);
        toastGui.blit(matrixStack, 0, 0, 0, 32, this.func_230445_a_(), this.func_238540_d_());
        toastGui.getMinecraft().fontRenderer.func_243248_b(matrixStack, title, 30.0F, 7.0F, -16777216);
        toastGui.getMinecraft().fontRenderer.func_243248_b(matrixStack, body, 30.0F, 18.0F, -16777216);
        toastGui.getMinecraft().getItemRenderer().renderItemAndEffectIntoGuiWithoutEntity(item, 8, 8);
        return (double)(time - this.lastChanged) >= 5000.0D ? IToast.Visibility.HIDE : IToast.Visibility.SHOW;
    }
}
