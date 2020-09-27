package com.salhack.summit.mixin.client;

import org.spongepowered.asm.mixin.Mixin;

import net.minecraft.client.particle.ParticleManager;

@Mixin(ParticleManager.class)
public class MixinParticleManager
{
    /*@Inject(method = "emitParticleAtEntity", at = @At("HEAD"), cancellable = true)
    public void emitParticleAtEntity(Entity p_Entity, EnumParticleTypes p_Type, int p_Amount, CallbackInfo p_Info)
    {
        EventParticleEmitParticleAtEntity l_Event = new EventParticleEmitParticleAtEntity(p_Entity, p_Type, p_Amount);
        
        SummitMod.EVENT_BUS.post(l_Event);
        
        if (l_Event.isCancelled())
            l_Event.cancel();
    }*/
}
