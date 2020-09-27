package com.salhack.summit.events.player;

import java.util.function.Consumer;

import com.salhack.summit.events.MinecraftEvent;
import net.minecraft.client.entity.EntityPlayerSP;

public class EventPlayerMotionUpdate extends MinecraftEvent
{
    protected float _yaw;
    protected float _pitch;
    protected double x;
    protected double y;
    protected double z;
    protected boolean onGround;
    private Consumer<EntityPlayerSP> _funcToCall = null;
    private boolean _isForceCancelled;
    
    public EventPlayerMotionUpdate(Stage stage, double posX, double posY, double posZ, boolean pOnGround)
    {
        super(stage);
        x = posX;
        y = posY;
        z = posZ;
        onGround = pOnGround;
    }
    
    public Consumer<EntityPlayerSP> getFunc()
    {
        return _funcToCall;
    }
    
    public void setFunct(Consumer<EntityPlayerSP> post)
    {
        _funcToCall = post;
    }

    public float getYaw()
    {
        return _yaw;
    }

    public void setYaw(float yaw)
    {
        _yaw = yaw;
    }

    public float getPitch()
    {
        return _pitch;
    }

    public void setPitch(float pitch)
    {
        _pitch = pitch;
    }
    
    public void setYaw(double yaw)
    {
        _yaw = (float)yaw;
    }
    
    public void setPitch(double pitch)
    {
        _pitch = (float)pitch;
    }

    public void forceCancel()
    {
        _isForceCancelled = true;
    }
    
    public boolean isForceCancelled()
    {
        return _isForceCancelled;
    }
    
    public void setX(double posX)
    {
        x = posX;
    }
    
    public void setY(double d)
    {
        y = d;
    }
    
    public void setZ(double posZ)
    {
        z = posZ;
    }

    public void setOnGround(boolean b)
    {
        onGround = b;
    }
    
    public double getX()
    {
        return x;
    }
    
    public double getY()
    {
        return y;
    }
    
    public double getZ()
    {
        return z;
    }
    
    public boolean getOnGround()
    {
        return onGround;
    }
}
