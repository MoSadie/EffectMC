package com.mosadie.effectmc;

import com.google.gson.JsonElement;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import com.mosadie.effectmc.core.EffectMCCore;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.toast.Toast;
import net.minecraft.client.toast.ToastManager;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.RegistryOps;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class ItemToast implements Toast {

    private static final Identifier TEXTURE = Identifier.tryParse("toast/recipe");
    private ItemStack item;
    private Text title;
    private Text body;

    private long lastChanged;
    private boolean changed = true;
    private Visibility visibility = Visibility.HIDE;

    public ItemToast(String itemData, Text title, Text body, EffectMCCore core) {
        if (MinecraftClient.getInstance().world == null) {
            EffectMC.LOGGER.warn("Error decoding item data: No level");
            item = new ItemStack(Items.AIR);
            this.title = title;
            this.body = body;
            return;
        }
        DataResult<Pair<ItemStack, JsonElement>> dataResult = ItemStack.CODEC.decode(RegistryOps.of(JsonOps.INSTANCE, MinecraftClient.getInstance().world.getRegistryManager()), core.fromJson(itemData));

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
    public Visibility getVisibility() {
        return visibility;
    }

    @Override
    public void update(ToastManager manager, long time) {
        if (changed) {
            lastChanged = time;
            changed = false;
        }

        visibility = (double)(time - this.lastChanged) >= 5000.0D ? Visibility.HIDE : Visibility.SHOW;
    }

    @Override
    public void draw(DrawContext context, TextRenderer textRenderer, long startTime) {
        context.drawGuiTexture(RenderLayer::getGuiTextured, TEXTURE, 0, 0, this.getWidth(), this.getHeight());
        context.drawText(textRenderer, title, 30, 7, -16777216, false);
        context.drawText(textRenderer, body, 30, 18, -16777216, false);
        context.drawItemWithoutEntity(item, 8, 8);
    }
}
