package com.salhack.summit.events.salhack;

import com.salhack.summit.module.Module;
import com.salhack.summit.events.MinecraftEvent;

public class EventSalHackModule extends MinecraftEvent
{
    public final Module Mod;
    
    public EventSalHackModule(final Module p_Mod)
    {
        super();
        Mod = p_Mod;
    }
}
