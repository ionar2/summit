package com.salhack.summit.mixin.client;

import com.salhack.summit.SummitMod;
import com.salhack.summit.events.render.EventRenderGuiUpdateTick;
import net.minecraft.client.gui.GuiIngame;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = GuiIngame.class, priority = Integer.MAX_VALUE)
public class MixinGuiIngame {
    @Inject(method = "updateTick", at = @At("RETURN"))
    public void updateTick(CallbackInfo info) {
        SummitMod.EVENT_BUS.post(new EventRenderGuiUpdateTick());
    }
}
