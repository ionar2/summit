package com.salhack.summit.mixin.client;

import com.salhack.summit.main.Wrapper;
import com.salhack.summit.module.render.Skeleton;
import net.minecraft.client.model.ModelPlayer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ModelPlayer.class)
public class MixinModelPlayer
{
    @Inject(method = "setRotationAngles", at = @At("RETURN"))
    public void setRotationAngles(float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scaleFactor, Entity entityIn, CallbackInfo callbackInfo)
    {
        if (Wrapper.GetMC().world != null && Wrapper.GetMC().player != null && entityIn instanceof EntityPlayer)
        {
            Skeleton.addEntity((EntityPlayer)entityIn, (ModelPlayer) (Object) this);
        }
    }
}
