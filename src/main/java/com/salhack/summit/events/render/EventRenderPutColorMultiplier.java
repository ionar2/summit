package com.salhack.summit.events.render;

import com.salhack.summit.events.MinecraftEvent;

public class EventRenderPutColorMultiplier extends MinecraftEvent
{
    private float _opacity;
    
    public void setOpacity(float opacity)
    {
        _opacity = opacity;
    }
    
    public float getOpacity()
    {
        return _opacity;
    }
}
