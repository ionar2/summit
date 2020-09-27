package com.salhack.summit.mixin.client;

import com.salhack.summit.SummitMod;
import com.salhack.summit.events.render.EventRenderMap;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.gui.MapItemRenderer;
import net.minecraft.world.storage.MapData;

@Mixin(MapItemRenderer.class)
public class MixinMapItemRenderer
{
    @Inject(method = "renderMap", at = @At("HEAD"), cancellable = true)
    public void render(MapData mapdataIn, boolean noOverlayRendering, CallbackInfo p_Callback)
    {
        EventRenderMap l_Event = new EventRenderMap();
        
        SummitMod.EVENT_BUS.post(l_Event);
        
        if (l_Event.isCancelled())
            p_Callback.cancel();
    }
}
