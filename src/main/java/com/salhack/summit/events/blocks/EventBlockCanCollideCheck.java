package com.salhack.summit.events.blocks;

import com.salhack.summit.events.MinecraftEvent;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;

public class EventBlockCanCollideCheck extends MinecraftEvent
{
    private Block _block = null;
    private IBlockState _state = null;

    public EventBlockCanCollideCheck(Block block, IBlockState state)
    {
        _block = block;
        _state = state;
    }

    public IBlockState getState()
    {
        return _state;
    }
}
