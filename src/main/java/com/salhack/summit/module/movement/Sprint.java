package com.salhack.summit.module.movement;

import com.salhack.summit.events.MinecraftEvent;
import com.salhack.summit.events.bus.EventHandler;
import com.salhack.summit.events.bus.Listener;
import com.salhack.summit.events.player.EventPlayerMotionUpdate;
import com.salhack.summit.events.player.EventPlayerUpdate;
import com.salhack.summit.module.Module;
import com.salhack.summit.module.Value;

public final class Sprint extends Module
{
    public final Value<String> Mode = new Value<>("Mode", new String[]
    { "Mode", "M" }, "The sprint mode to use.", "Rage");

    public Sprint()
    {
        super("Sprint", new String[]
        { "AutoSprint", "Spr" }, "Automatically sprints for you", "NONE", 0x90B48A, ModuleType.MOVEMENT);
        setMetaData(getMetaData());
        
        Mode.addString("Rage");
        Mode.addString("Legit");
    }

    @Override
    public void onDisable()
    {
        super.onDisable();

        if (mc.world != null)
        {
            mc.player.setSprinting(false);
        }
    }

    public String getMetaData()
    {
        return String.valueOf(Mode.getValue());
    }
    
    @EventHandler
    private Listener<EventPlayerUpdate> onPlayerUpdate  = new Listener<>(event ->
    {
        setMetaData(getMetaData());
    });
    
    @EventHandler
    private Listener<EventPlayerMotionUpdate> onMotionUpdates = new Listener<>(p_Event ->
    {
        if (p_Event.getStage() != MinecraftEvent.Stage.Pre)
            return;
        
        switch (this.Mode.getValue())
        {
            case "Rage":
                if ((mc.player.movementInput.moveForward != 0.0f || mc.player.movementInput.moveStrafe != 0.0f) && !mc.player.isSneaking() && !mc.player.collidedHorizontally && mc.player.getFoodStats().getFoodLevel() > 6f)
                {
                    mc.player.setSprinting(true);
                }
                break;
            case "Legit":
                if ((mc.gameSettings.keyBindForward.isKeyDown()) && !(mc.player.isSneaking()) && !(mc.player.isHandActive()) && !(mc.player.collidedHorizontally) && mc.currentScreen == null
                        && !(mc.player.getFoodStats().getFoodLevel() <= 6f))
                {
                    mc.player.setSprinting(true);
                }
                break;
        }
    });

}
