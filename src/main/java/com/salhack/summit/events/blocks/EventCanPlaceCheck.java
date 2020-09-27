package com.salhack.summit.events.blocks;

import com.salhack.summit.events.bus.Cancellable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class EventCanPlaceCheck extends Cancellable
{
    public World World;
    public BlockPos Pos;
    public Class<?> Block;

    public EventCanPlaceCheck(World world, BlockPos pos, Class<?> block) {
        World = world;
        Pos = pos;
        Block = block;
    }
}
