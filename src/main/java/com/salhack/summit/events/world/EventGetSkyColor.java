package com.salhack.summit.events.world;

import com.salhack.summit.events.MinecraftEvent;
import net.minecraft.util.math.Vec3d;

public class EventGetSkyColor extends MinecraftEvent
{
    private Vec3d color;
    
    public void setColor(Vec3d color)
    {
        this.color = color;
    }

    public Vec3d getVec3d()
    {
        return color;
    }
}
