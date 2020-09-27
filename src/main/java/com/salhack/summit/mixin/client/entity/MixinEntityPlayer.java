package com.salhack.summit.mixin.client.entity;

import com.salhack.summit.events.player.EventPlayerApplyCollision;
import com.salhack.summit.events.player.EventPlayerPushedByWater;
import com.salhack.summit.events.player.EventPlayerTravel;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.Entity;
import net.minecraft.entity.MoverType;
import net.minecraft.entity.player.EntityPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.salhack.summit.SummitMod;

@Mixin(value = EntityPlayer.class, priority = Integer.MAX_VALUE)
public abstract class MixinEntityPlayer extends MixinEntityLivingBase
{
    public MixinEntityPlayer()
    {
        super();
    }
    
    @Shadow
    public void onUpdate()
    {
        
    }

    @Inject(method = "travel", at = @At("HEAD"), cancellable = true)
    public void travel(float strafe, float vertical, float forward, CallbackInfo info)
    {
        EntityPlayer us = (EntityPlayer) (Object) this;
        if (!(us instanceof EntityPlayerSP))
            return;
        
        EventPlayerTravel event = new EventPlayerTravel(strafe, vertical, forward);
        SummitMod.EVENT_BUS.post(event);
        if (event.isCancelled())
        {
            move(MoverType.SELF, motionX, motionY, motionZ);
            info.cancel();
        }
    }

    @Inject(method = "applyEntityCollision", at = @At("HEAD"), cancellable = true)
    public void applyEntityCollision(Entity p_Entity, CallbackInfo info)
    {
        EventPlayerApplyCollision l_Event = new EventPlayerApplyCollision(p_Entity);
        SummitMod.EVENT_BUS.post(l_Event);
        if (l_Event.isCancelled())
            info.cancel();
    }

    @Inject(method = "isPushedByWater()Z", at = @At("HEAD"), cancellable = true)
    public void isPushedByWater(CallbackInfoReturnable<Boolean> ci)
    {
        EventPlayerPushedByWater l_Event = new EventPlayerPushedByWater();
        SummitMod.EVENT_BUS.post(l_Event);
        if (l_Event.isCancelled())
            ci.setReturnValue(false);
    }
}
