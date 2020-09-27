package com.salhack.summit.mixin.client.blocks;

import com.salhack.summit.SummitMod;
import com.salhack.summit.events.blocks.EventBlockCollisionBoundingBox;
import com.salhack.summit.events.blocks.EventBlockGetRenderLayer;
import net.minecraft.block.Block;
import net.minecraft.block.BlockCactus;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BlockCactus.class)
public class MixinBlockCactus
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
