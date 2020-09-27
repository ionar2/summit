package com.salhack.summit.module.world;

import com.salhack.summit.events.bus.EventHandler;
import com.salhack.summit.events.bus.Listener;
import com.salhack.summit.events.player.EventPlayerMotionUpdate;
import com.salhack.summit.module.Module;
import com.salhack.summit.module.Value;
import com.salhack.summit.util.BlockInteractionHelper;
import com.salhack.summit.util.entity.EntityUtil;
import com.salhack.summit.util.entity.PlayerUtil;
import net.minecraft.block.BlockDoublePlant;
import net.minecraft.block.BlockFlower;
import net.minecraft.block.BlockTallGrass;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;

import java.util.Comparator;

public class Lawnmower extends Module
{
    public static Value<Integer> Radius = new Value<Integer>("Radius", new String[] {"R"}, "Radius to search for and break tall grass", 4, 0, 10, 1);
    public static Value<Boolean> Flowers = new Value<Boolean>("Flowers", new String[] {"R"}, "Break Flowers", true);

    public Lawnmower()
    {
        super("Lawnmower", new String[] {""}, "Breaks grass and flowers in range", "NONE", -1, ModuleType.WORLD);
    }

    @EventHandler
    private Listener<EventPlayerMotionUpdate> OnPlayerUpdate = new Listener<>(p_Event ->
    {
        BlockPos l_ClosestPos = BlockInteractionHelper.getSphere(PlayerUtil.GetLocalPlayerPosFloored(), Radius.getValue(), Radius.getValue(), false, true, 0).stream()
                                .filter(p_Pos -> IsValidBlockPos(p_Pos))
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

            mc.player.swingArm(EnumHand.MAIN_HAND);
            mc.playerController.clickBlock(l_ClosestPos, EnumFacing.UP);
        }
    });

    private boolean IsValidBlockPos(final BlockPos p_Pos)
    {
        IBlockState l_State = mc.world.getBlockState(p_Pos);

        if (l_State.getBlock() instanceof BlockTallGrass || l_State.getBlock() instanceof BlockDoublePlant)
            return true;

        if (Flowers.getValue() && l_State.getBlock() instanceof BlockFlower)
            return true;

        return false;
    }
}
