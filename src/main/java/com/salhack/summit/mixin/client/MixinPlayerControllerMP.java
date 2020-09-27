package com.salhack.summit.mixin.client;

import com.salhack.summit.SummitMod;
import com.salhack.summit.events.player.EventPlayerClickBlock;
import com.salhack.summit.events.player.EventPlayerDamageBlock;
import com.salhack.summit.events.player.EventPlayerDestroyBlock;
import com.salhack.summit.events.player.EventPlayerResetBlockRemoving;
import com.salhack.summit.main.SummitStatic;
import com.salhack.summit.events.player.EventPlayerOnStoppedUsingItem;
import net.minecraft.block.Block;
import net.minecraft.block.BlockCommandBlock;
import net.minecraft.block.BlockStructure;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.PlayerControllerMP;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.GameType;
import net.minecraft.world.World;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerControllerMP.class)
public class MixinPlayerControllerMP
{
    @Shadow @Final
    private Minecraft mc;
    @Shadow
    private GameType currentGameType;
    @Shadow
    private BlockPos currentBlock;

    @Inject(method = "getBlockReachDistance", at = @At("HEAD"), cancellable = true)
    public void resetBlockRemoving(CallbackInfoReturnable<Float> callbackInfo)
    {
        if (SummitStatic.REACH != null && SummitStatic.REACH.isEnabled())
        {
            float attrib = (float) mc.player.getEntityAttribute(EntityPlayer.REACH_DISTANCE).getAttributeValue() + (SummitStatic.REACH != null && SummitStatic.REACH.isEnabled() ? SummitStatic.REACH.ReachAdd.getValue() : 0);
            callbackInfo.setReturnValue(this.currentGameType.isCreative() ? attrib : attrib - 0.5F);
        }
    }

    @Inject(method = "onPlayerDamageBlock", at = @At("HEAD"), cancellable = true)
    public void onPlayerDamageBlock(BlockPos posBlock, EnumFacing directionFacing, CallbackInfoReturnable<Boolean> p_Info)
    {
        EventPlayerDamageBlock l_Event = new EventPlayerDamageBlock(posBlock, directionFacing);
        SummitMod.EVENT_BUS.post(l_Event);
        if (l_Event.isCancelled())
        {
            p_Info.setReturnValue(false);
            p_Info.cancel();
        }
    }
    
    @Inject(method = "resetBlockRemoving", at = @At("HEAD"), cancellable = true)
    public void resetBlockRemoving(CallbackInfo callbackInfo)
    {
        EventPlayerResetBlockRemoving event = new EventPlayerResetBlockRemoving();

        SummitMod.EVENT_BUS.post(event);
        if (event.isCancelled())
            callbackInfo.cancel();
    }
    
    @Inject(method = "clickBlock", at = @At("HEAD"), cancellable = true)
    public void clickBlock(BlockPos loc, EnumFacing face, CallbackInfoReturnable<Boolean> callbackInfo)
    {
        EventPlayerClickBlock event = new EventPlayerClickBlock(loc, face);
        SummitMod.EVENT_BUS.post(event);
        if (event.isCancelled())
        {
            callbackInfo.setReturnValue(false);
            callbackInfo.cancel();
        }
    }

    /*@Inject(method = "onPlayerDestroyBlock", at = @At("HEAD"), cancellable = true)
    public void onPlayerDestroyBlock(BlockPos pos, CallbackInfoReturnable<Boolean> callbackInfo)
    {
        EventPlayerDestroyBlock event = new EventPlayerDestroyBlock(pos);
        SummitMod.EVENT_BUS.post(event);
        if (event.isCancelled())
        {
            callbackInfo.setReturnValue(false);
            callbackInfo.cancel();
        }
    }*/
    
    @Inject(method = "onPlayerDestroyBlock", at = @At("HEAD"), cancellable = true)
    public void onPlayerDestroyBlock(BlockPos pos, CallbackInfoReturnable<Boolean> callbackInfo)
    {
        EventPlayerDestroyBlock event = new EventPlayerDestroyBlock(pos);
        SummitMod.EVENT_BUS.post(event);
        if (event.isCancelled())
        {
            callbackInfo.cancel();
            callbackInfo.setReturnValue(false);
        }

        if (SummitStatic.NOGLITCHBLOCKS != null && SummitStatic.NOGLITCHBLOCKS.isEnabled())
        {
            callbackInfo.cancel();
            if (this.currentGameType.hasLimitedInteractions())
            {
                if (this.currentGameType == GameType.SPECTATOR)
                {
                    callbackInfo.setReturnValue(false);
                }

                if (!this.mc.player.isAllowEdit())
                {
                    ItemStack itemstack = this.mc.player.getHeldItemMainhand();

                    if (itemstack.isEmpty())
                    {
                        callbackInfo.setReturnValue(false);
                    }

                    if (!itemstack.canDestroy(this.mc.world.getBlockState(pos).getBlock()))
                    {
                        callbackInfo.setReturnValue(false);
                    }
                }
            }

            if (this.currentGameType.isCreative() && !this.mc.player.getHeldItemMainhand().isEmpty()
                    && this.mc.player.getHeldItemMainhand().getItem() instanceof ItemSword)
            {
                callbackInfo.setReturnValue(false);
            } else
            {
                World world = this.mc.world;
                IBlockState iblockstate = world.getBlockState(pos);
                Block block = iblockstate.getBlock();

                if ((block instanceof BlockCommandBlock || block instanceof BlockStructure)
                        && !this.mc.player.canUseCommandBlock())
                {
                    callbackInfo.setReturnValue(false);
                } else if (iblockstate.getMaterial() == Material.AIR)
                {
                    callbackInfo.setReturnValue(false);
                } else
                {
                    world.playEvent(2001, pos, Block.getStateId(iblockstate));
                    block.onBlockHarvested(world, pos, iblockstate, this.mc.player);

                    boolean flag = false;
                    boolean skipClientDestroy = SummitStatic.NOGLITCHBLOCKS != null
                            && SummitStatic.NOGLITCHBLOCKS.isEnabled()
                            && SummitStatic.NOGLITCHBLOCKS.Destroy.getValue();

                    if (!skipClientDestroy)
                    {
                        flag = world.setBlockState(pos, Blocks.AIR.getDefaultState(), 11);

                        if (flag)
                        {
                            block.onPlayerDestroy(world, pos, iblockstate);
                        }
                    }

                    this.currentBlock = new BlockPos(this.currentBlock.getX(), -1, this.currentBlock.getZ());

                    if (!this.currentGameType.isCreative())
                    {
                        ItemStack itemstack1 = this.mc.player.getHeldItemMainhand();

                        if (!itemstack1.isEmpty())
                        {
                            itemstack1.onBlockDestroyed(world, iblockstate, pos, this.mc.player);

                            if (itemstack1.isEmpty())
                            {
                                this.mc.player.setHeldItem(EnumHand.MAIN_HAND, ItemStack.EMPTY);
                            }
                        }
                    }

                    callbackInfo.setReturnValue(flag);
                }
            }
        }
    }
    
    @Inject(method = "onStoppedUsingItem", at = @At("HEAD"), cancellable = true)
    public void onPlayerDestroyBlock(EntityPlayer playerIn, CallbackInfo info)
    {
        EventPlayerOnStoppedUsingItem event = new EventPlayerOnStoppedUsingItem();
        SummitMod.EVENT_BUS.post(event);
        if (event.isCancelled())
            info.cancel();
    }
}
