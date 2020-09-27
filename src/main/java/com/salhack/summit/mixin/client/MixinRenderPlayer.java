package com.salhack.summit.mixin.client;

import com.salhack.summit.events.render.EventRenderEntityName;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.salhack.summit.SummitMod;
import com.salhack.summit.main.Wrapper;
import com.salhack.summit.managers.RotationManager;
import com.salhack.summit.util.CameraUtils;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.renderer.entity.RenderPlayer;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(RenderPlayer.class)
public class MixinRenderPlayer
{
    @Inject(method = "renderEntityName", at = @At("HEAD"), cancellable = true)
    public void renderLivingLabel(AbstractClientPlayer entityIn, double x, double y, double z, String name, double distanceSq, CallbackInfo info)
    {
        EventRenderEntityName l_Event = new EventRenderEntityName(entityIn, x, y, z, name, distanceSq);
        SummitMod.EVENT_BUS.post(l_Event);
        if (l_Event.isCancelled())
            info.cancel();
    }
    
    @Redirect(method = "doRender", require = 0, at = @At(
                value = "INVOKE",
                target = "Lnet/minecraft/client/entity/AbstractClientPlayer;isUser()Z"))
    private boolean overrideIsUser(AbstractClientPlayer entity)
    {
        // This allows the real player entity to be rendered in the free camera mode
        if (CameraUtils.freecamEnabled() && entity == Wrapper.GetPlayer())
            return false;

        return entity.isUser();
    }
}
