package com.salhack.summit.module.render;

import com.salhack.summit.events.bus.EventHandler;
import com.salhack.summit.events.bus.Listener;
import com.salhack.summit.events.player.EventPlayerTravel;
import com.salhack.summit.module.Module;

public class Parkour extends Module
{
    public Parkour()
    {
        super("Parkour", new String[] {"Parkour", "EdgeJump", "Parkourmaster", "Parkuur", "Park"}, "Jump at the edge of a block.", "NONE", -1, ModuleType.MOVEMENT);
    }

    @EventHandler
    private Listener<EventPlayerTravel> onTravel = new Listener<>(event ->
    {
        if (!mc.player.onGround || mc.player.isSneaking())
            return;
        
        if (mc.world.getCollisionBoxes(mc.player, mc.player.getEntityBoundingBox().offset(0, -0.5, 0).expand(-0.001, 0, -0.001)).isEmpty())
        {
            mc.player.jump();
            return;
        }
    });
}
