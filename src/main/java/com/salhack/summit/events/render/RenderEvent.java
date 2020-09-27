package com.salhack.summit.events.render;

import com.salhack.summit.events.MinecraftEvent;

public class RenderEvent extends MinecraftEvent
{
    private float _partialTicks;
    
    public RenderEvent(float partialTicks)
    {
        _partialTicks = partialTicks;
    }
    
    public float getPartialTicks()
    {
        return _partialTicks;
    }
}
