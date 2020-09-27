package com.salhack.summit.mixin.client.entity;

import com.salhack.summit.SummitMod;
import com.salhack.summit.events.player.EventPlayerIsPotionActive;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Potion;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import com.salhack.summit.events.entity.EventItemUseFinish;

@Mixin(EntityLivingBase.class)
public abstract class MixinEntityLivingBase extends MixinEntity
{
    public MixinEntityLivingBase()
    {
        super();
    }

    @Shadow
    public void jump()
    {
    }
    
    @Shadow
    protected ItemStack activeItemStack;
    @Shadow
    public float moveStrafing;
    @Shadow
    public float moveVertical;
    @Shadow
    public float moveForward;

    @Inject(method = "isPotionActive", at = @At("HEAD"), cancellable = true)
    public void isPotionActive(Potion potionIn, final CallbackInfoReturnable<Boolean> callbackInfoReturnable)
    {
        EventPlayerIsPotionActive l_Event = new EventPlayerIsPotionActive(potionIn);
        SummitMod.EVENT_BUS.post(l_Event);

        if (l_Event.isCancelled())
            callbackInfoReturnable.setReturnValue(false);
    }
    
    @Inject(method = "onItemUseFinish", at = @At("HEAD"), cancellable = true)
    protected void onItemUseFinish(CallbackInfo info)
    {
        EventItemUseFinish event = new EventItemUseFinish((EntityLivingBase) (Object) this, activeItemStack);
        SummitMod.EVENT_BUS.post(event);
        if (event.isCancelled())
            info.cancel();
    }
    
    @Shadow
    public abstract boolean isElytraFlying();
}
