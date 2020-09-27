package com.salhack.summit.module.render;

import com.salhack.summit.events.bus.EventHandler;
import com.salhack.summit.events.bus.Listener;
import com.salhack.summit.events.player.EventPlayerUpdate;
import com.salhack.summit.module.Module;

public class NoBob extends Module
{
    public NoBob()
    {
        super("NoBob", new String[] {"NoBob"}, "Prevents bobbing by setting distance walked modifier to a static number", "NONE", 0x5E4ED6, ModuleType.RENDER);
    }
    
    @EventHandler
    private Listener<EventPlayerUpdate> OnPlayerUpdate = new Listener<>(event ->
    {
        mc.player.distanceWalkedModified = 4.0f;
    });
}
