package com.salhack.summit.module.misc;

import com.salhack.summit.events.bus.EventHandler;
import com.salhack.summit.events.bus.Listener;
import com.salhack.summit.events.client.EventClientTick;
import com.salhack.summit.module.Module;

public class StopWatch extends Module
{
    public StopWatch()
    {
        super("Stopwatch", new String[] {""}, "Counts a stopwatch starting from 0 when toggled.", "NONE", -1, ModuleType.MISC);
    }
    
    public long StartMS;
    public long ElapsedMS;
    
    @Override
    public void onEnable()
    {
        super.onEnable();
        
        StartMS = System.currentTimeMillis();
        ElapsedMS = System.currentTimeMillis();
    }

    @EventHandler
    private Listener<EventClientTick> OnTick = new Listener<>(p_Event ->
    {
        ElapsedMS = System.currentTimeMillis();
    });
}
