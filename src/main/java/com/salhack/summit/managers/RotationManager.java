package com.salhack.summit.managers;

import com.salhack.summit.SummitMod;
import com.salhack.summit.events.MinecraftEvent;
import com.salhack.summit.events.player.EventPlayerMotionUpdateCancelled;
import com.salhack.summit.events.player.EventPlayerUpdate;
import com.salhack.summit.main.Summit;
import com.salhack.summit.util.Timer;
import com.salhack.summit.events.bus.EventHandler;
import com.salhack.summit.events.bus.EventListener;
import com.salhack.summit.events.bus.Listener;

public class RotationManager implements EventListener
{
    public void init()
    {
        SummitMod.EVENT_BUS.subscribe(this);
    }
    
    private float[] _rotations;
    private Timer timer = new Timer();
    
    public void resetRotations()
    {
        _rotations = null;
    }
    
    public float getYawForMixin(float yaw)
    {
        return _rotations != null ? _rotations[0] : yaw;
    }

    public float getPitchForMixin(float pitch)
    {
        return _rotations != null ? _rotations[1] : pitch;
    }
    
    public void setRotations(float yaw, float pitch)
    {
        _rotations = new float[] { yaw, pitch };
    }
    
    public float[] getRotations()
    {
        return _rotations;
    }
    
    @EventHandler
    private Listener<EventPlayerUpdate> onPlayerUpdate = new Listener<>(event ->
    {
        if (timer.passed(100))
        {
            timer.reset();
            resetRotations();
        }
    });

    @EventHandler
    private Listener<EventPlayerMotionUpdateCancelled> onMotionUpdate = new Listener<>(event ->
    {
        timer.reset();
        
        if (event.getStage() == MinecraftEvent.Stage.Pre)
            setRotations(event.getYaw(), event.getPitch());
    });
    
    public static RotationManager Get()
    {
        return Summit.GetRotationManager();
    }
}
