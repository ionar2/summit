package com.salhack.summit.events.render;

import com.salhack.summit.events.MinecraftEvent;

public class EventRenderHurtCameraEffect extends MinecraftEvent
{
    public float Ticks;
    
    public EventRenderHurtCameraEffect(float p_Ticks)
    {
        super();
        Ticks = p_Ticks;
    }
}
