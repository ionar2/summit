package com.salhack.summit.events.entity;

import com.salhack.summit.events.MinecraftEvent;

public class EventEntityCollisionBorderSize extends MinecraftEvent
{
    private float size;
    
    public float getSize()
    {
        return size;
    }
    
    public void setSize(float s)
    {
        size = s;
    }
}
