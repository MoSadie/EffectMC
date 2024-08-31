package com.mosadie.effectmc.mixin;

import com.mosadie.effectmc.EffectMC;
import com.mosadie.effectmc.core.EffectMCCore;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.packet.s2c.play.GameMessageS2CPacket;
import net.minecraft.text.TranslatableText;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(ClientPlayNetworkHandler.class)
public class OnGameMessageMixin {

    @Inject(method = "onGameMessage", at = @At("HEAD"), cancellable = true)
    private void onGameMessageMixin(GameMessageS2CPacket packet, CallbackInfo ci) {
        if (packet.getMessage() instanceof TranslatableText) {
            TranslatableText translatableText = (TranslatableText) packet.getMessage();

            if (translatableText.getKey().equals(EffectMCCore.TRANSLATION_TRIGGER_KEY)) {
                EffectMC.LOGGER.info("Received trigger message");

                List<ClientModInitializer> list = FabricLoader.getInstance().getEntrypoints("client", ClientModInitializer.class);
                list.stream().filter(clientModInitializer -> clientModInitializer instanceof EffectMC).forEach(clientModInitializer -> {
                    EffectMC.LOGGER.info("Triggering effect");

                    EffectMC effectMC = (EffectMC) clientModInitializer;
                    effectMC.handleTranslationTrigger(translatableText);

                    // Cancel display of the trigger message
                    if (ci.isCancellable() && !ci.isCancelled()) {
                        ci.cancel();
                    }
                });
            }
        }
    }
}
