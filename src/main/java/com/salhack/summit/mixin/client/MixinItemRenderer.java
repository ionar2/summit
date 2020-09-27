package com.salhack.summit.mixin.client;

import com.salhack.summit.events.render.EventRenderUpdateEquippedItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.salhack.summit.SummitMod;
import net.minecraft.client.renderer.ItemRenderer;

@Mixin(ItemRenderer.class)
public class MixinItemRenderer
{
    @Inject(method = "updateEquippedItem", at = @At("HEAD"), cancellable = true)
    public void updateEquippedItem(CallbackInfo p_Info)
    {
        EventRenderUpdateEquippedItem l_Event = new EventRenderUpdateEquippedItem();
        SummitMod.EVENT_BUS.post(l_Event);
        if (l_Event.isCancelled())
            p_Info.cancel();
    }
}
