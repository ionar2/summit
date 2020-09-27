package com.salhack.summit.mixin.client.blocks;

import com.salhack.summit.SummitMod;
import com.salhack.summit.events.blocks.EventCanCollideCheck;
import com.salhack.summit.events.blocks.EventBlockCollisionBoundingBox;
import com.salhack.summit.events.blocks.EventBlockGetRenderLayer;
import net.minecraft.block.Block;
import net.minecraft.block.BlockLiquid;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BlockLiquid.class)
public class MixinBlockLiquid
{
   @Inject(method = "getRenderLayer", at = @At("HEAD"), cancellable = true)
   public void getRenderLayer(CallbackInfoReturnable<BlockRenderLayer> callback)
   {
       EventBlockGetRenderLayer event = new EventBlockGetRenderLayer((Block) (Object) this);
       SummitMod.EVENT_BUS.post(event);

       if (event.isCancelled())                                                               
       {
           callback.cancel();
           callback.setReturnValue(event.getBlockRenderLayer());
       }
   }
   
   @Inject(method = "canCollideCheck", at = @At("HEAD"), cancellable = true)
   public void canCollideCheck(final IBlockState blockState, final boolean b, final CallbackInfoReturnable<Boolean> callbackInfoReturnable)
   {
       EventCanCollideCheck event = new EventCanCollideCheck();
       SummitMod.EVENT_BUS.post(event);
       callbackInfoReturnable.setReturnValue(event.isCancelled());
   }
   
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
