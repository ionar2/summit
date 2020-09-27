package com.salhack.summit.module.movement;

import com.salhack.summit.events.bus.EventHandler;
import com.salhack.summit.events.bus.Listener;
import com.salhack.summit.events.entity.EventHorseSaddled;
import com.salhack.summit.events.entity.EventSteerEntity;
import com.salhack.summit.module.Module;

public class EntityControl extends Module
{
    public EntityControl()
    {
        super("EntityControl", new String[]
        { "AntiSaddle", "EntityRide", "NoSaddle" }, "Allows you to control llamas, horses, pigs without a saddle/carrot", "NONE", 0x189B23, ModuleType.MOVEMENT);
    }

    @EventHandler
    private Listener<EventSteerEntity> OnSteerEntity = new Listener<>(p_Event ->
    {
        p_Event.cancel();
    });

    @EventHandler
    private Listener<EventHorseSaddled> OnHorseSaddled = new Listener<>(p_Event ->
    {
        p_Event.cancel();
    });
}
