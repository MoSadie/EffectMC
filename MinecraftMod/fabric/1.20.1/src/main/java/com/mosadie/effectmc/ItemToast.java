package com.mosadie.effectmc;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.client.gui.DrawContext;
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
    public Visibility draw(DrawContext context, ToastManager manager, long time) {
        if (changed) {
            lastChanged = time;
            changed = false;
        }

        context.drawTexture(TEXTURE, 0, 0, 0, 32, this.getWidth(), this.getHeight());
        context.drawText(manager.getClient().textRenderer, title, 30, 7, -16777216, false);
        context.drawText(manager.getClient().textRenderer, body, 30, 18, -16777216, false);
        context.drawItemWithoutEntity(item, 8, 8);
        return (double)(time - this.lastChanged) >= 5000.0D ? Visibility.HIDE : Visibility.SHOW;
    }
}
