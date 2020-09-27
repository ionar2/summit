package com.salhack.summit.module.movement;

import com.salhack.summit.events.bus.EventHandler;
import com.salhack.summit.events.bus.Listener;
import com.salhack.summit.events.player.EventPlayerTravel;
import com.salhack.summit.module.Module;
import com.salhack.summit.module.Value;

public class HighJump extends Module
{
    public final Value<Boolean> InAir = new Value<Boolean>("InAir", new String[]
    { "Air", "OnGroundOnly", "OnGround", "GroundOnly", "Ground"  }, "Should you be able to jump in air", true);
    public final Value<Float> Height = new Value<Float>("Height", new String[]
    { "Height", "Heigh", "Hight", "High", "Size", "Scaling", "Scale" }, "Height to increase", 1.4f, 0.0f, 10.f, 1.0f);
    
    public HighJump()
    {
        super("HighJump", new String[]
        { "AW" }, "Jump way higher than a normal jump", "NONE", 0xC224DB, ModuleType.MOVEMENT);
    }

    @EventHandler
    private Listener<EventPlayerTravel> OnTravel = new Listener<>(event ->
    { 
        if (!event.isCancelled())
        {
            if (mc.player == null || mc.player.isRiding() || mc.player.isElytraFlying())
                return;
            
            if ((mc.player.movementInput.jump && InAir.getValue()) || mc.player.onGround)
            {
                event.cancel();
                mc.player.motionY = Height.getValue();
            }
        }
    });
}
