package com.salhack.summit.events.player;

import com.salhack.summit.events.MinecraftEvent;
import net.minecraft.util.math.BlockPos;

public class EventPlayerDestroyBlock extends MinecraftEvent
{
    public BlockPos Location;

    public EventPlayerDestroyBlock(BlockPos loc)
    {
        Location = loc;
    }
}
