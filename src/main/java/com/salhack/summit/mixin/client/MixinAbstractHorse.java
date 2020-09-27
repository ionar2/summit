package com.salhack.summit.mixin.client;

import com.salhack.summit.SummitMod;
import com.salhack.summit.events.entity.EventHorseSaddled;
import com.salhack.summit.events.entity.EventSteerEntity;
import net.minecraft.entity.passive.AbstractHorse;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AbstractHorse.class)
public class MixinAbstractHorse
{
    @Inject(method = "canBeSteered", at = @At("HEAD"), cancellable = true)
    public void canBeSteered(CallbackInfoReturnable<Boolean> cir)
    {
        EventSteerEntity l_Event = new EventSteerEntity();
        SummitMod.EVENT_BUS.post(l_Event);

        if (l_Event.isCancelled())
        {
            cir.cancel();
            cir.setReturnValue(true);
        }
    }

    @Inject(method = "isHorseSaddled", at = @At("HEAD"), cancellable = true)
    public void isHorseSaddled(CallbackInfoReturnable<Boolean> cir)
    {
        EventHorseSaddled l_Event = new EventHorseSaddled();
        SummitMod.EVENT_BUS.post(l_Event);

        if (l_Event.isCancelled())
        {
            cir.cancel();
            cir.setReturnValue(true);
        }
    }
}
