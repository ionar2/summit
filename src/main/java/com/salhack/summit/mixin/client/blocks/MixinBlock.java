package com.salhack.summit.mixin.client.blocks;

import com.salhack.summit.SummitMod;
import com.salhack.summit.events.blocks.EventBlockCollisionBoundingBox;
import com.salhack.summit.events.blocks.EventBlockGetRenderLayer;
import com.salhack.summit.events.blocks.EventCanPlaceCheck;
import com.salhack.summit.main.SummitStatic;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Block.class)
public class MixinBlock
{
    @Inject(method = "canPlaceBlockAt", at = @At("HEAD"), cancellable = true)
    public void canPlaceBlockAt(World world, BlockPos pos, CallbackInfoReturnable<Boolean> callbackInfoReturnable)
    {
        EventCanPlaceCheck l_Event = new EventCanPlaceCheck(world, pos, this.getClass());
        SummitMod.EVENT_BUS.post(l_Event);

        if (l_Event.isCancelled())
        {
            callbackInfoReturnable.setReturnValue(!l_Event.isCancelled());
            callbackInfoReturnable.cancel();
        }
    }

    @Inject(method = "shouldSideBeRendered", at = @At("HEAD"), cancellable = true)
    public void shouldSideBeRendered(IBlockState blockState, IBlockAccess blockAccess, BlockPos pos, EnumFacing side,
            CallbackInfoReturnable<Boolean> callback)
    {
        if (SummitStatic.WALLHACK != null && SummitStatic.WALLHACK.isEnabled())
            SummitStatic.WALLHACK.processShouldSideBeRendered((Block)(Object)this, blockState, blockAccess, pos, side, callback);
    }
    
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
    
    @Inject(method = "getLightValue", at = @At("HEAD"), cancellable = true)
    public void getLightValue(CallbackInfoReturnable<Integer> callback)
    {
        if (SummitStatic.WALLHACK != null && SummitStatic.WALLHACK.isEnabled())
            SummitStatic.WALLHACK.processGetLightValue((Block)(Object)this, callback);
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
