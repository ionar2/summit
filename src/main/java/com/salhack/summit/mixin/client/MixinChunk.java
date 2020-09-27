package com.salhack.summit.mixin.client;

import com.salhack.summit.SummitMod;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import com.salhack.summit.events.world.EventChunkLoad;
import com.salhack.summit.events.world.EventChunkLoad.Type;
import net.minecraft.world.chunk.Chunk;

@Mixin(Chunk.class)
public class MixinChunk
{
    @Inject(method = "onUnload", at = @At("RETURN"))
    public void onUnload(CallbackInfo info)
    {
        SummitMod.EVENT_BUS.post(new EventChunkLoad(Type.UNLOAD, (Chunk) (Object) this));
    }
}
