package com.salhack.summit.events.render;

import com.salhack.summit.events.MinecraftEvent;

public class EventRenderHand extends MinecraftEvent
{
    public float PartialTicks;
    public int Pass;

    public EventRenderHand(float partialTicks, int pass)
    {
        super();
        
        PartialTicks = partialTicks;
        Pass = pass;
    }

}
