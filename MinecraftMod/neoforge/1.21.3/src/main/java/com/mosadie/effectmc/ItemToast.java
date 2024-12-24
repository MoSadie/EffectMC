package com.mosadie.effectmc;

import com.google.gson.JsonElement;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import com.mosadie.effectmc.core.EffectMCCore;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.toasts.Toast;
import net.minecraft.client.gui.components.toasts.ToastManager;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class ItemToast implements Toast {
    private final static ResourceLocation TEXTURE = ResourceLocation.withDefaultNamespace("toast/recipe");
    private ItemStack item;
    private Component title;
    private Component body;

    private long lastChanged;
    private boolean changed = true;
    private Visibility visibility = Visibility.HIDE;

    public ItemToast(String itemData, Component title, Component body, EffectMCCore core) {
        if (Minecraft.getInstance().level == null) {
            EffectMC.LOGGER.warn("Error decoding item data: No level");
            item = new ItemStack(Items.AIR);
            this.title = title;
            this.body = body;
            return;
        }
        DataResult<Pair<ItemStack, JsonElement>> dataResult = ItemStack.CODEC.decode(RegistryOps.create(JsonOps.INSTANCE, Minecraft.getInstance().level.registryAccess()), core.fromJson(itemData));

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
    public Visibility getWantedVisibility() {
        return visibility;
    }

    @Override
    public void update(ToastManager toastManager, long time) {
        if (this.changed) {
            this.lastChanged = time;
            this.changed = false;
        }

        visibility = (double)(time - this.lastChanged) >= 5000.0D * toastManager.getNotificationDisplayTimeMultiplier() ? Visibility.HIDE : Visibility.SHOW;
    }

    @Override
    public void render(GuiGraphics guiGraphics, Font font, long time) {
        if (changed) {
            lastChanged = time;
            changed = false;
        }


        guiGraphics.blitSprite(RenderType::guiTextured, TEXTURE, 0, 0, this.width(), this.height());
        guiGraphics.drawString(font, title, 30, 7, -16777216, false);
        guiGraphics.drawString(font, body, 30, 18, -16777216, false);
        guiGraphics.renderFakeItem(item, 8, 8);
    }
}
