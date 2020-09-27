package com.salhack.summit.mixin.client;

import com.salhack.summit.SummitMod;
import com.salhack.summit.events.render.EventRenderBossHealth;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.gui.GuiBossOverlay;

@Mixin(GuiBossOverlay.class)
public class MixinGuiBossOverlay
{
    @Inject(method = "renderBossHealth", at = @At("HEAD"), cancellable = true)
    public void renderBossHealth(CallbackInfo p_Info)
    {
        EventRenderBossHealth l_Event = new EventRenderBossHealth();
        SummitMod.EVENT_BUS.post(l_Event);
        if (l_Event.isCancelled())
            p_Info.cancel();
    }
}
