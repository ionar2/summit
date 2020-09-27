package com.salhack.summit.events.blocks;

import com.salhack.summit.events.MinecraftEvent;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;

public class EventBlockCollisionBoundingBox extends MinecraftEvent
{
    private BlockPos _pos;
    private AxisAlignedBB _boundingBox;
    
    public EventBlockCollisionBoundingBox(BlockPos pos)
    {
        _pos = pos;
    }
    
    public BlockPos getPos()
    {
        return _pos;
    }

    public AxisAlignedBB getBoundingBox()
    {
        return _boundingBox;
    }

    public void setBoundingBox(AxisAlignedBB boundingBox)
    {
        this._boundingBox = boundingBox;
    }
}
