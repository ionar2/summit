package com.salhack.summit.module.movement;

import baritone.api.BaritoneAPI;
import baritone.api.pathing.goals.GoalXZ;
import com.salhack.summit.events.bus.EventHandler;
import com.salhack.summit.events.bus.Listener;
import com.salhack.summit.events.player.EventPlayerUpdateMoveState;
import com.salhack.summit.main.SummitStatic;
import com.salhack.summit.module.Module;
import com.salhack.summit.module.Value;
import com.salhack.summit.util.MathUtil;
import net.minecraft.entity.item.EntityBoat;

public final class AutoWalk extends Module
{
    public final Value<String> Mode = new Value<>("Mode", new String[] {"Modes", "M"}, "The mode for walking", "Forward");
    
    public AutoWalk()
    {
        super("AutoWalk", new String[]
        { "AW" }, "Automatically walks forward", "NONE", 0xC224DB, ModuleType.MOVEMENT);
        
        Mode.addString("Forward");
        Mode.addString("Smart");
    }
    
    @EventHandler
    private Listener<EventPlayerUpdateMoveState> OnUpdateMoveState = new Listener<>(p_Event ->
    {
        if (Mode.getValue().equals("Forward"))
        {
            if (!NeedPause())
            {
                mc.player.movementInput.moveForward++;
                
                if (mc.player.getRidingEntity() instanceof EntityBoat)
                {
                    double[] dir = MathUtil.directionSpeed(0.47f);
                    
                    mc.player.getRidingEntity().motionX = dir[0];
                    mc.player.getRidingEntity().motionZ = dir[1];
                }
            }
        }
        else
        {
            BaritoneAPI.getSettings().allowSprint.value = true;
            BaritoneAPI.getSettings().allowBreak.value = false;
            BaritoneAPI.getSettings().primaryTimeoutMS.value = 2000L;

            GoalXZ goal = GoalXZ.fromDirection(
                    mc.player.getPositionVector(),
                    mc.player.rotationYawHead,
                    100
            );
            
            BaritoneAPI.getProvider().getPrimaryBaritone().getCustomGoalProcess().setGoalAndPath(goal);
        }
    });
    
    @Override
    public void onDisable()
    {
        super.onDisable();
        BaritoneAPI.getProvider().getPrimaryBaritone().getCustomGoalProcess().setGoal(null);
    }
    
    private boolean NeedPause()
    {
        /*if (SummitStatic.AUTOTUNNEL.isEnabled() && SummitStatic.AUTOTUNNEL.PauseAutoWalk())
            return true;*/
        
        return false;
    }
}
