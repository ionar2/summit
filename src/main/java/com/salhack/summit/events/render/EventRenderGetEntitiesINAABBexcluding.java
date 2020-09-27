package com.salhack.summit.events.render;

import com.google.common.base.Predicate;

import com.salhack.summit.events.MinecraftEvent;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.AxisAlignedBB;

public class EventRenderGetEntitiesINAABBexcluding extends MinecraftEvent
{

    public EventRenderGetEntitiesINAABBexcluding(WorldClient worldClient, Entity entityIn, AxisAlignedBB boundingBox, Predicate predicate)
    {
        // TODO Auto-generated constructor stub
    }

}
