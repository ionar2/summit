package com.salhack.summit.mixin.client.blocks;

import com.salhack.summit.SummitMod;
import com.salhack.summit.events.blocks.EventBlockCollisionBoundingBox;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.block.BlockAir;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;

@Mixin(BlockAir.class)
public class MixinBlockAir
{
    @Inject(method = "getCollisionBoundingBox", at = @At("HEAD"), cancellable = true)
    public void getCollisionBoundingBox(IBlockState blockState, IBlockAccess worldIn, BlockPos pos, final CallbackInfoReturnable<AxisAlignedBB> callbackInfoReturnable)
    {
        EventBlockCollisionBoundingBox event = new EventBlockCollisionBoundingBox(pos);
        SummitMod.EVENT_BUS.post(event);
        if (event.isCancelled())
        {
            callbackInfoReturnable.setReturnValue(event.getBoundingBox());
            callbackInfoReturnable.cancel();
        }
    }
}
