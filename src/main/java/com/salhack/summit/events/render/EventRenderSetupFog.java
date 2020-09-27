package com.salhack.summit.events.render;

import com.salhack.summit.events.MinecraftEvent;

public class EventRenderSetupFog extends MinecraftEvent
{
    public int StartCoords;
    public float PartialTicks;

    public EventRenderSetupFog(int startCoords, float partialTicks)
    {
        StartCoords = startCoords;
        PartialTicks = partialTicks;
    }

}
