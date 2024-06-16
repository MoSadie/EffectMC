package com.mosadie.effectmc;

import com.google.gson.JsonElement;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import com.mosadie.effectmc.core.EffectMCCore;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.toasts.RecipeToast;
import net.minecraft.client.gui.components.toasts.Toast;
import net.minecraft.client.gui.components.toasts.ToastComponent;
import net.minecraft.nbt.TagParser;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class ItemToast implements Toast {
    private final static ResourceLocation TEXTURE = ResourceLocation.tryParse("toast/recipe");
    private ItemStack item;
    private Component title;
    private Component body;

    private long lastChanged;
    private boolean changed = true;

    public ItemToast(String itemData, Component title, Component body, EffectMCCore core) {
        DataResult<Pair<ItemStack, JsonElement>> dataResult = ItemStack.CODEC.decode(JsonOps.INSTANCE, core.fromJson(itemData));

        if (dataResult.error().isPresent()) {
            EffectMC.LOGGER.warn("Error decoding item data: " + dataResult.error().get());
            item = new ItemStack(Items.AIR);
        } else if (dataResult.result().isPresent()) {
            item = dataResult.result().get().getFirst();
        } else {
            EffectMC.LOGGER.warn("Error decoding item data: No item data");
            item = new ItemStack(Items.AIR);
        }

        this.title = title;
        this.body = body;
    }
    @Override
    public Visibility render(GuiGraphics guiGraphics, ToastComponent toastComponent, long time) {
        if (changed) {
            lastChanged = time;
            changed = false;
        }


        guiGraphics.blitSprite(TEXTURE, 0, 0, this.width(), this.height());
        guiGraphics.drawString(toastComponent.getMinecraft().font, title, 30, 7, -16777216, false);
        guiGraphics.drawString(toastComponent.getMinecraft().font, body, 30, 18, -16777216, false);
        guiGraphics.renderFakeItem(item, 8, 8);
        return (double)(time - this.lastChanged) >= 5000.0D * toastComponent.getNotificationDisplayTimeMultiplier() ? Visibility.HIDE : Visibility.SHOW;
    }
}
