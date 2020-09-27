package com.salhack.summit.module.movement;

import com.salhack.summit.events.bus.EventHandler;
import com.salhack.summit.events.bus.Listener;
import com.salhack.summit.events.player.EventPlayerTravel;
import com.salhack.summit.events.player.EventPlayerUpdate;
import com.salhack.summit.module.Module;
import com.salhack.summit.module.Value;
import com.salhack.summit.util.MathUtil;
import net.minecraft.entity.Entity;

public class EntitySpeed extends Module
{
    public final Value<Float> Speed = new Value<Float>("Speed", new String[]
    { "" }, "Speed to use", 0.5f, 0.0f, 10.0f, 1.0f);
    
    public EntitySpeed()
    {
        super("EntitySpeed", new String[] {"HorseHax"}, "Allows you to modify the horses speed", "NONE", -1, ModuleType.MOVEMENT);
        setMetaData(getMetaData());
    }
    
    public String getMetaData()
    {
        return String.valueOf(Speed.getValue());
    }
    
    @EventHandler
    private Listener<EventPlayerUpdate> onPlayerUpdate  = new Listener<>(event ->
    {
        setMetaData(getMetaData());
    });

    @EventHandler
    private Listener<EventPlayerTravel> OnTravel = new Listener<>(event ->
    {
        if (mc.player == null || !mc.player.isRiding())
            return;
        
        Entity riding = mc.player.getRidingEntity();
        
        double[] dir = MathUtil.directionSpeed(Speed.getValue());

        if (mc.player.movementInput.moveStrafe != 0 || mc.player.movementInput.moveForward != 0)
        {
            riding.motionX = dir[0];
            riding.motionZ = dir[1];
        }
        
        event.cancel();
    });
}
