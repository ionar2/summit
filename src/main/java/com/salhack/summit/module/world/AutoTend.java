package com.salhack.summit.module.world;

import com.salhack.summit.events.bus.EventHandler;
import com.salhack.summit.events.bus.Listener;
import com.salhack.summit.events.player.EventPlayerMotionUpdate;
import com.salhack.summit.module.Module;
import com.salhack.summit.module.Value;
import com.salhack.summit.util.BlockInteractionHelper;
import com.salhack.summit.util.Timer;
import com.salhack.summit.util.entity.EntityUtil;
import com.salhack.summit.util.entity.PlayerUtil;
import net.minecraft.block.Block;
import net.minecraft.block.BlockCrops;
import net.minecraft.block.BlockFarmland;
import net.minecraft.block.BlockReed;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.ItemDye;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.CPacketPlayerTryUseItemOnBlock;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;

import java.util.Comparator;

public class AutoTend extends Module
{
    public final Value<Integer> Radius = new Value<Integer>("Radius", new String[] {"R"}, "Radius to search for and break torches", 4, 0, 10, 1);
    public final Value<Boolean> Harvest = new Value<Boolean>("Harvest", new String[] {"Harvest"} , "Automatically Harvests", true);
    public final Value<Boolean> Replant = new Value<Boolean>("Replant", new String[] {"Replants"} , "Automatically plants if not harvesting, and there's nothing to harvest", true);
    public final Value<Boolean> Bonemeal = new Value<Boolean>("Bonemeal", new String[] {"Bonemeal"} , "Automatically bonemeals the plants", true);

    public final Value<Float> Delay = new Value<Float>("Delay", new String[] {"D"}, "Delay to harvest/replant", 1.0f, 0.0f, 10.0f, 0.1f);
    
    public AutoTend()
    {
        super("AutoTend", new String[] {""}, "Breaks and replants plants nearby", "NONE", -1, ModuleType.WORLD);
    }

    private Timer timer = new Timer();

    @EventHandler
    private Listener<EventPlayerMotionUpdate> OnPlayerUpdate = new Listener<>(p_Event ->
    {
        if (p_Event.isCancelled())
            return;
        
        if (Harvest.getValue())
        {
            if (!timer.passed(Delay.getValue() * 100))
                return;
            
            timer.reset();
            
            BlockPos l_ClosestPos = BlockInteractionHelper.getSphere(PlayerUtil.GetLocalPlayerPosFloored(), Radius.getValue(), Radius.getValue(), false, true, 0).stream()
                                    .filter(p_Pos -> IsHarvestBlock(p_Pos))
                                    .min(Comparator.comparing(p_Pos -> EntityUtil.GetDistanceOfEntityToBlock(mc.player, p_Pos)))
                                    .orElse(null);

            if (l_ClosestPos != null)
            {
                p_Event.cancel();

                final double l_Pos[] =  EntityUtil.calculateLookAt(
                        l_ClosestPos.getX() + 0.5,
                        l_ClosestPos.getY() - 0.5,
                        l_ClosestPos.getZ() + 0.5,
                        mc.player);

                p_Event.setPitch(l_Pos[1]);
                p_Event.setYaw(l_Pos[0]);

                mc.playerController.clickBlock(l_ClosestPos, EnumFacing.UP);
                mc.player.swingArm(EnumHand.MAIN_HAND);
                return;
            }
        }

        if (Replant.getValue() && HasSeeds() && !p_Event.isCancelled())
        {
            if (!timer.passed(Delay.getValue() * 100))
                return;
            
            timer.reset();
            
            BlockPos l_ClosestPos = BlockInteractionHelper.getSphere(PlayerUtil.GetLocalPlayerPosFloored(), Radius.getValue(), Radius.getValue(), false, true, 0).stream()
                                    .filter(p_Pos -> IsReplantBlock(p_Pos))
                                    .min(Comparator.comparing(p_Pos -> EntityUtil.GetDistanceOfEntityToBlock(mc.player, p_Pos)))
                                    .orElse(null);

            if (l_ClosestPos != null)
            {
                p_Event.cancel();

                final double l_Pos[] =  EntityUtil.calculateLookAt(
                        l_ClosestPos.getX() + 0.5,
                        l_ClosestPos.getY() - 0.5,
                        l_ClosestPos.getZ() + 0.5,
                        mc.player);

                p_Event.setPitch(l_Pos[1]);
                p_Event.setYaw(l_Pos[0]);

                SwitchToSeedSlot();
                mc.player.swingArm(IsItemStackSeed(mc.player.getHeldItemOffhand()) ? EnumHand.OFF_HAND : EnumHand.MAIN_HAND);
                mc.getConnection().sendPacket(new CPacketPlayerTryUseItemOnBlock(l_ClosestPos, EnumFacing.UP,
                        IsItemStackSeed(mc.player.getHeldItemOffhand()) ? EnumHand.OFF_HAND : EnumHand.MAIN_HAND, 0, 0, 0));
                return;
            }
        }
        
        if (Bonemeal.getValue() && !p_Event.isCancelled())
        {
            if (!timer.passed(Delay.getValue() * 100))
                return;
            
            timer.reset();
            
            BlockPos l_ClosestPos = BlockInteractionHelper.getSphere(PlayerUtil.GetLocalPlayerPosFloored(), Radius.getValue(), Radius.getValue(), false, true, 0).stream()
                                    .filter(p_Pos -> IsValidBonemealPos(p_Pos))
                                    .min(Comparator.comparing(p_Pos -> EntityUtil.GetDistanceOfEntityToBlock(mc.player, p_Pos)))
                                    .orElse(null);

            if (l_ClosestPos != null && UpdateBonemealIfNeed())
            {
                p_Event.cancel();

                final double l_Pos[] =  EntityUtil.calculateLookAt(
                        l_ClosestPos.getX() + 0.5,
                        l_ClosestPos.getY() + 0.5,
                        l_ClosestPos.getZ() + 0.5,
                        mc.player);

                p_Event.setPitch(l_Pos[1]);
                p_Event.setYaw(l_Pos[0]);

                mc.player.swingArm(EnumHand.MAIN_HAND);

                mc.getConnection().sendPacket(new CPacketPlayerTryUseItemOnBlock(l_ClosestPos, EnumFacing.UP,
                    mc.player.getHeldItemOffhand().getItem() == Items.DYE ? EnumHand.OFF_HAND : EnumHand.MAIN_HAND, 0, 0, 0));
            }
        }
    });

    private boolean IsHarvestBlock(final BlockPos p_Pos)
    {
        IBlockState l_State = mc.world.getBlockState(p_Pos);

        if (l_State.getBlock() instanceof BlockCrops)
        {
            BlockCrops l_Crop = (BlockCrops)l_State.getBlock();

            if (l_Crop.isMaxAge(l_State))
                return true;
        }
        else if (l_State.getBlock() instanceof BlockReed)
        {
            if (mc.world.getBlockState(p_Pos.down()).getBlock() == Blocks.REEDS)
                return true;
        }

        return false;
    }

    private boolean IsReplantBlock(final BlockPos p_Pos)
    {
        IBlockState l_State = mc.world.getBlockState(p_Pos);

        if (l_State.getBlock() instanceof BlockFarmland)
            return HasNoCropsAndIsPlantable(p_Pos);

        return false;
    }

    private boolean HasNoCropsAndIsPlantable(final BlockPos p_Pos)
    {
        Block block = mc.world.getBlockState(p_Pos.up()).getBlock();
        return block == Blocks.AIR;
    }

    private boolean HasSeeds()
    {
        for (int l_I = 0; l_I < 9; ++l_I)
        {
            ItemStack l_Stack = mc.player.inventory.getStackInSlot(l_I);

            if (l_Stack.isEmpty())
                continue;

            if (IsItemStackSeed(l_Stack))
                return true;
        }

        return IsItemStackSeed(mc.player.getHeldItemOffhand());
    }

    private boolean IsItemStackSeed(ItemStack p_Stack)
    {
        return !p_Stack.isEmpty() && (p_Stack.getItem() == Items.BEETROOT_SEEDS || p_Stack.getItem() == Items.POTATO || p_Stack.getItem() == Items.WHEAT_SEEDS || p_Stack.getItem() == Items.CARROT);
    }

    private void SwitchToSeedSlot()
    {
        if (IsItemStackSeed(mc.player.getHeldItemOffhand()) || IsItemStackSeed(mc.player.getHeldItemMainhand()))
            return;

        for (int l_I = 0; l_I < 9; ++l_I)
        {
            ItemStack l_Stack = mc.player.inventory.getStackInSlot(l_I);

            if (l_Stack.isEmpty())
                continue;

            if (IsItemStackSeed(l_Stack))
            {
                mc.player.inventory.currentItem = l_I;
                mc.playerController.updateController();
                return;
            }
        }
    } 

    private boolean IsValidBonemealPos(final BlockPos p_Pos)
    {
        IBlockState l_State = mc.world.getBlockState(p_Pos);

        if (l_State.getBlock() instanceof BlockCrops)
        {
            BlockCrops l_Crop = (BlockCrops)l_State.getBlock();

            if (!l_Crop.isMaxAge(l_State))
                return true;
        }

        return false;
    }

    
    private boolean UpdateBonemealIfNeed()
    {
        ItemStack l_Main = mc.player.getHeldItemMainhand();
        ItemStack l_Off = mc.player.getHeldItemOffhand();

        if (!l_Main.isEmpty() && l_Main.getItem() instanceof ItemDye)
        {
            if (IsBoneMealItem(l_Main))
                return true;
        }
        else if (!l_Off.isEmpty() && l_Off.getItem() instanceof ItemDye)
        {
            if (IsBoneMealItem(l_Off))
                return true;
        }

        for (int l_I = 0; l_I < 9; ++l_I)
        {
            ItemStack l_Stack = mc.player.inventory.getStackInSlot(l_I);

            if (l_Stack.isEmpty() || !(l_Stack.getItem() instanceof ItemDye))
                continue;

            if (IsBoneMealItem(l_Stack))
            {
                mc.player.inventory.currentItem = l_I;
                mc.playerController.updateController();
                return true;
            }
        }

        return false;
    }

    private boolean IsBoneMealItem(ItemStack p_Stack)
    {
        return EnumDyeColor.byDyeDamage(p_Stack.getMetadata()) == EnumDyeColor.WHITE;
    }
}
