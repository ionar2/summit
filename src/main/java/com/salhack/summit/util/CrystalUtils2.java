package com.salhack.summit.util;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import com.salhack.summit.main.Summit;
import com.salhack.summit.main.SummitStatic;
import com.salhack.summit.main.Wrapper;
import com.salhack.summit.module.combat.AutoCrystal;
import com.salhack.summit.util.entity.PlayerUtil;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityEnderCrystal;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

public class CrystalUtils2
{
    private static Minecraft mc = Wrapper.GetMC();

    public static List<BlockPos> obbyRock = new CopyOnWriteArrayList<>();
    public static List<BlockPos> crystalBlocks = new CopyOnWriteArrayList<>();
    
    public static boolean canPlaceCrystalAt(BlockPos blockpos, IBlockState state)
    {
        World worldIn = mc.world;
        BlockPos blockpos1 = blockpos.up();
        BlockPos blockpos2 = blockpos.up().up();

        boolean flag = !worldIn.isAirBlock(blockpos1) && !worldIn.getBlockState(blockpos).getBlock().isReplaceable(worldIn, blockpos1);
        
        flag |= !worldIn.isAirBlock(blockpos2) && !worldIn.getBlockState(blockpos2).getBlock().isReplaceable(worldIn, blockpos2);
        
        if (flag)
            return false;

        double d0 = (double)blockpos.getX();
        double d1 = (double)blockpos.getY();
        double d2 = (double)blockpos.getZ();
        
        if (!worldIn.getEntitiesWithinAABBExcludingEntity(null, new AxisAlignedBB(d0, d1, d2, d0 + 1.0D, d1 + 2.0D, d2 + 1.0D)).isEmpty())
            return false;
        
        return true;
    }

    public static void onSetBlockState(BlockPos pos, IBlockState newState, int flags)
    {
        if (newState.getBlock() == Blocks.OBSIDIAN || newState.getBlock() == Blocks.BEDROCK)
        {
            if (!obbyRock.contains(pos))
                obbyRock.add(pos);
        }
        else
        {
            obbyRock.remove(pos);
            if (crystalBlocks.contains(pos))
                crystalBlocks.remove(pos);
        }
        
        /*updateList(pos, newState);
        // update surrounding blocks
        final BlockPos[] surrounding = { pos.up(), pos.down(), pos.west(), pos.east(), pos.north(), pos.west() };
        
        for (BlockPos surr : surrounding)
        {
            final BlockPos[] surrounding2 = { surr.up(), surr.down(), surr.west(), surr.east(), surr.north(), surr.west() };
            
            for (BlockPos surr2 : surrounding2)
                updateList(surr2, mc.world.getBlockState(surr2));
            
            updateList(surr, mc.world.getBlockState(surr));
        }*/
    }
    
    public static void updateList(BlockPos pos, IBlockState newState)
    {
        if (newState.getBlock() == Blocks.OBSIDIAN || newState.getBlock() == Blocks.BEDROCK)
        {
            if (canPlaceCrystalAt(pos, newState))
            {
                if (!crystalBlocks.contains(pos))
                {
                    crystalBlocks.add(pos);
                    Summit.SendMessage("Added a block at " + pos.toString());
                }
            }
            else
            {
                Summit.SendMessage("removed a block " + pos.toString());
                crystalBlocks.remove(pos);
            }
        }
        else
            crystalBlocks.remove(pos);
    }

    public static void loadWorld()
    {
        crystalBlocks.clear();
    }

    public static void markBlockRangeForRenderUpdate(int x1, int y1, int z1, int x2, int y2, int z2)
    {
        long ms = System.currentTimeMillis();
        
        int minX = x1 < x2 ? x1 : x2;
        int minY = y1 < y2 ? y1 : y2;
        int minZ = z1 < z2 ? z1 : z2;
        int maxX = x2 > x1 ? x2 : x1;
        int maxY = y2 > y1 ? y2 : y1;
        int maxZ = z2 > z1 ? z2 : z1;
        
        for (int x = minX; x < maxX; ++x)
        {
            for (int y = minY; y1 < maxY; ++y)
            {
                for (int z = minZ; z1 < maxZ; ++z)
                {
                    BlockPos pos = new BlockPos(x, y, z);
                    IBlockState state = mc.world.getBlockState(pos);
                    
                    if (state.getBlock() != Blocks.OBSIDIAN && state.getBlock() != Blocks.BEDROCK)
                    {
                        obbyRock.remove(pos);
                        continue;
                    }
                    
                    if (!obbyRock.contains(pos))
                        obbyRock.add(pos);
                }
            }
        }
        
        System.out.println("[2] Took " + (System.currentTimeMillis() - ms) + " ms to execute.");
    }

    public static void onUpdate()
    {
        if (mc.player == null)
            return;
        
        if (SummitStatic.AUTOCRYSTAL == null || !SummitStatic.AUTOCRYSTAL.isEnabled())
            return;
        
        long ms = System.currentTimeMillis();
        
        for (BlockPos pos : obbyRock)
        {
            IBlockState state = mc.world.getBlockState(pos);
            
            boolean alreadyContains = crystalBlocks.contains(pos);
            
            if (state.getBlock() != Blocks.OBSIDIAN && state.getBlock() != Blocks.BEDROCK)
            {
                crystalBlocks.remove(pos);
                obbyRock.remove(pos);
                continue;
            }
            
            float dist = (float) mc.player.getDistance(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5);
            if (dist > AutoCrystal.PlaceRadius.getValue())
            {
                if (alreadyContains)
                    crystalBlocks.remove(pos);
                obbyRock.remove(pos);
                continue;
            }
            
            if (!alreadyContains && canPlaceCrystalAt(pos, mc.world.getBlockState(pos)))
            {
                if (AutoCrystal.VerifyCrystalBlocks(mc, pos))
                    crystalBlocks.add(pos);
                continue;
            }
            
            if (alreadyContains && (!canPlaceCrystalAt(pos, mc.world.getBlockState(pos)) || !AutoCrystal.VerifyCrystalBlocks(mc, pos)))
                crystalBlocks.remove(pos);
        }
        
        int flooredRadius = MathHelper.floor(AutoCrystal.PlaceRadius.getValue()) + 1;
        BlockPos playerPosFloored = PlayerUtil.GetLocalPlayerPosFloored();
        
        for (int x = playerPosFloored.getX() - flooredRadius; x <= playerPosFloored.getX() + flooredRadius; ++x)
            for (int y = playerPosFloored.getY() - flooredRadius; y <= playerPosFloored.getY() + flooredRadius; ++y)
                for (int z = playerPosFloored.getZ() - flooredRadius; z <= playerPosFloored.getZ() + flooredRadius; ++z)
                {
                    BlockPos pos = new BlockPos(x, y, z);
                    
                    if (obbyRock.contains(pos))
                        continue;

                    float dist = (float) mc.player.getDistance(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5);
                    if (dist > AutoCrystal.PlaceRadius.getValue())
                        continue;
                    
                    IBlockState state = mc.world.getBlockState(pos);
                    
                    if (state.getBlock() == Blocks.OBSIDIAN || state.getBlock() == Blocks.BEDROCK)
                    {
                        //Summit.SendMessage("Found a new obisidan or bedrock block at " + pos.toString());
                        obbyRock.add(pos);
                    }
                }
        
        long diff = System.currentTimeMillis() - ms;
        
       // if (diff > 0)
       //     Summit.SendMessage("[3] Took " + (System.currentTimeMillis() - ms) + " ms to execute.");
    }

    public static void onEntityRemoved(Entity entity)
    {
        if (entity instanceof EntityEnderCrystal && mc.player.getDistance(entity) <= AutoCrystal.PlaceRadius.getValue())
        {
            final BlockPos pos = entity.getPosition().down();
            IBlockState state = mc.world.getBlockState(pos);
            
            if (state.getBlock() == Blocks.OBSIDIAN || state.getBlock() == Blocks.BEDROCK)
            {
                //Summit.SendMessage("EZ");
                crystalBlocks.add(pos);
            }
        }
    }
}
