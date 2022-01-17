package io.github.mosadie.effectmc;

import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;

public class DelayedUnpressThread extends Thread {
    KeyBinding keyBinding;
    long holdTime;

    DelayedUnpressThread(KeyBinding keyBinding, long holdTime) {
        this.keyBinding = keyBinding;
        this.holdTime = holdTime;
    }

    @Override
    public void run() {
        try {
            sleep(holdTime);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        Minecraft.getInstance().enqueue(() -> keyBinding.setPressed(false));
    }
}
