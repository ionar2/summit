package com.salhack.summit.mixin.client;

import com.salhack.summit.SummitMod;
import com.salhack.summit.events.blocks.EventSetOpaqueCube;
import net.minecraft.client.renderer.chunk.VisGraph;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(VisGraph.class)
public class MixinVisGraph
{
    @Inject(method = "setOpaqueCube", at = @At("HEAD"), cancellable = true)
    public void setOpaqueCube(BlockPos pos, CallbackInfo info)
    {
        EventSetOpaqueCube l_Event = new EventSetOpaqueCube(); ///< pos is unused
        SummitMod.EVENT_BUS.post(l_Event);
        if (l_Event.isCancelled())
            info.cancel();
    }
}
