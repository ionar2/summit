package com.salhack.summit.events.player;

import com.salhack.summit.events.MinecraftEvent;
import net.minecraft.entity.MoverType;

public class EventPlayerMove extends MinecraftEvent
{
    public MoverType Type;
    public double X;
    public double Y;
    public double Z;

    public EventPlayerMove(Stage post, MoverType p_Type, double p_X, double p_Y, double p_Z)
    {
        this.setEra(post);
        Type = p_Type;
        X = p_X;
        Y = p_Y;
        Z = p_Z;
    }

    public void setY(double y)
    {
        this.Y = y;
    }

    public double getY()
    {
        return this.Y;
    }

    public void setX(double x)
    {
        this.X = x;
    }
    
    public void setZ(double z)
    {
        this.Z = z;
    }

    public void zeroXZ()
    {
        this.cancel();
        this.X = 0;
        this.Z = 0;
    }
}
