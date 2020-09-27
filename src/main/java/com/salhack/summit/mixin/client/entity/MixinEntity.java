package com.salhack.summit.mixin.client.entity;

import com.salhack.summit.SummitMod;
import com.salhack.summit.events.entity.EventEntityCollisionBorderSize;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.salhack.summit.util.CameraUtils;
import net.minecraft.entity.Entity;
import net.minecraft.entity.MoverType;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.world.World;

@Mixin(Entity.class)
public abstract class MixinEntity
{

    @Shadow public abstract boolean equals(Object p_equals_1_);

    @Shadow
    public double posX;

    @Shadow
    public double posY;

    @Shadow
    public double posZ;

    @Shadow
    public double prevPosX;

    @Shadow
    public double prevPosY;

    @Shadow
    public double prevPosZ;

    @Shadow
    public double lastTickPosX;

    @Shadow
    public double lastTickPosY;

    @Shadow
    public double lastTickPosZ;

    @Shadow
    public float prevRotationYaw;

    @Shadow
    public float prevRotationPitch;

    @Shadow
    public float rotationPitch;

    @Shadow
    public float rotationYaw;

    @Shadow
    public boolean onGround;

    @Shadow
    public double motionX;

    @Shadow
    public double motionY;

    @Shadow
    public double motionZ;

    @Shadow
    public abstract boolean isSprinting();

    @Shadow
    public abstract boolean isRiding();

    @Shadow
    public void move(MoverType type, double x, double y, double z)
    {

    }

    @Shadow
    public abstract AxisAlignedBB getEntityBoundingBox();
    
    @Shadow
    public abstract boolean getFlag(int flag);
    
    @Shadow
    public abstract Entity getLowestRidingEntity();

    @Shadow
    public World world;
    
    @Inject(method = "turn",
            at = @At(value = "FIELD",
                     target = "Lnet/minecraft/entity/Entity;prevRotationPitch:F", ordinal = 0))
    private void overrideYaw(float yawChange, float pitchChange, CallbackInfo ci)
    {
        if ((Object) this instanceof net.minecraft.client.entity.EntityPlayerSP)
        {
            if (CameraUtils.shouldPreventPlayerMovement())
            {
                this.rotationYaw = this.prevRotationYaw;
                this.rotationPitch = this.prevRotationPitch;

                CameraUtils.updateCameraRotations(yawChange, pitchChange);

                return;
            }
        }
    }
    
    @Inject(method = "getCollisionBorderSize", at = @At("HEAD"), cancellable = true)
    public void getCollisionBorderSize(CallbackInfoReturnable<Float> callback)
    {
        EventEntityCollisionBorderSize event = new EventEntityCollisionBorderSize();
        SummitMod.EVENT_BUS.post(event);

        if (event.isCancelled())                                                               
        {
            callback.cancel();
            callback.setReturnValue(event.getSize());
        }
    }
}
