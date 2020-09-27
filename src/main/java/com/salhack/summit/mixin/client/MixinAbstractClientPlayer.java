package com.salhack.summit.mixin.client;

import com.salhack.summit.SummitMod;
import com.salhack.summit.events.player.EventPlayerGetLocationCape;
import com.salhack.summit.mixin.client.entity.MixinEntityPlayer;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.util.ResourceLocation;

@Mixin(AbstractClientPlayer.class)
public abstract class MixinAbstractClientPlayer extends MixinEntityPlayer
{
    public MixinAbstractClientPlayer()
    {
        super();
    }

    @Shadow
    protected abstract boolean isSpectator();
    
    @Inject(method = "getLocationCape", at = @At(value = "RETURN"), cancellable = true)
    public void getCape(CallbackInfoReturnable<ResourceLocation> callbackInfo)
    {
        EventPlayerGetLocationCape l_Event = new EventPlayerGetLocationCape((AbstractClientPlayer)(Object)this);
        SummitMod.EVENT_BUS.post(l_Event);

        if (l_Event.isCancelled())
        {
            // p_Callback.cancel();
            callbackInfo.setReturnValue(l_Event.GetResourceLocation());
        }
    }
}
