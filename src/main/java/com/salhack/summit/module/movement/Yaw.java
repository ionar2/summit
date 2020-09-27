package com.salhack.summit.module.movement;

import com.salhack.summit.events.MinecraftEvent;
import com.salhack.summit.events.bus.EventHandler;
import com.salhack.summit.events.bus.Listener;
import com.salhack.summit.events.player.EventPlayerMotionUpdate;
import com.salhack.summit.module.Module;
import com.salhack.summit.module.Value;
import net.minecraft.entity.Entity;

public final class Yaw extends Module
{
    public final Value<Boolean> Cardinal = new Value<Boolean>("Cardinal", new String[]
    { "C" }, "Locks the yaw to one of the cardinal directions", true);
    public final Value<Boolean> yawLock = new Value<Boolean>("Yaw", new String[]
    { "Y" }, "Lock the player's rotation yaw if enabled.", false);
    public final Value<Boolean> pitchLock = new Value<Boolean>("Pitch", new String[]
    { "P" }, "Lock the player's rotation pitch if enabled.", false);

    private float Yaw;
    private float Pitch;

    public Yaw()
    {
        super("Yaw", new String[]
        { "RotLock", "Rotation" }, "Locks you rotation for precision", "NONE", 0x6C2684, ModuleType.MOVEMENT);

        setMetaData(Cardinal.getValue() ? "Cardinal" : "One");
    }

    @Override
    public void onEnable()
    {
        super.onEnable();

        if (mc.player != null)
        {
            Yaw = mc.player.rotationYaw;
            Pitch = mc.player.rotationPitch;
        }
    }
    
    @Override
    public void toggleNoSave()
    {
        /// override don't trigger on logic, we access player at enable
    }

    @EventHandler
    private Listener<EventPlayerMotionUpdate> OnPlayerUpdate = new Listener<>(p_Event ->
    {
        if (p_Event.getStage() != MinecraftEvent.Stage.Pre)
            return;

        setMetaData(Cardinal.getValue() ? "Cardinal" : "One");
        
        Entity l_Entity = mc.player.isRiding() ? mc.player.getRidingEntity() : mc.player;
        
        if (this.yawLock.getValue())
            l_Entity.rotationYaw = Yaw;

        if (this.pitchLock.getValue())
            l_Entity.rotationPitch = Pitch;

        if (Cardinal.getValue())
            l_Entity.rotationYaw = Math.round((l_Entity.rotationYaw + 1.0f) / 45.0f) * 45.0f;
    });
}
