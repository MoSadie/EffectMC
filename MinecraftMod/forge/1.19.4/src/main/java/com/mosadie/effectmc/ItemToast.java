package com.mosadie.effectmc;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.components.toasts.Toast;
import net.minecraft.client.gui.components.toasts.ToastComponent;
import net.minecraft.nbt.TagParser;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class ItemToast implements Toast {
    private ItemStack item;
    private Component title;
    private Component body;

    private long lastChanged;
    private boolean changed = true;

    public ItemToast(String itemData, Component title, Component body) {
        try {
            this.item = ItemStack.of(TagParser.parseTag(itemData));
        } catch (CommandSyntaxException e) {
            EffectMC.LOGGER.error("Invalid Item Data for Item Toast", e);
            this.item = new ItemStack(Items.AIR, 1, null);
        }
        this.title = title;
        this.body = body;
    }
    @Override
    public Visibility render(PoseStack poseStack, ToastComponent toastComponent, long time) {
        if (changed) {
            lastChanged = time;
            changed = false;
        }

        RenderSystem.setShaderTexture(0, TEXTURE);
        GuiComponent.blit(poseStack, 0, 0, 0, 32, this.width(), this.height());
        toastComponent.getMinecraft().font.draw(poseStack, title, 30.0F, 7.0F, -16777216);
        toastComponent.getMinecraft().font.draw(poseStack, body, 30.0F, 18.0F, -16777216);
        toastComponent.getMinecraft().getItemRenderer().renderAndDecorateFakeItem(poseStack, item, 8, 8);
        return (double)(time - this.lastChanged) >= 5000.0D * toastComponent.getNotificationDisplayTimeMultiplier() ? Toast.Visibility.HIDE : Toast.Visibility.SHOW;
    }
}
