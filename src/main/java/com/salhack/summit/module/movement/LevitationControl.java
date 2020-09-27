package com.salhack.summit.module.movement;

import com.salhack.summit.events.bus.EventHandler;
import com.salhack.summit.events.bus.Listener;
import com.salhack.summit.events.player.EventPlayerTravel;
import com.salhack.summit.module.Module;
import com.salhack.summit.util.MathUtil;
import net.minecraft.init.MobEffects;

public final class LevitationControl extends Module
{
    public LevitationControl()
    {
        super("LevitationControl", new String[]
        { "NoLevitate" }, "Allows you to control your levitation", "NONE", 0xCCFAFA, ModuleType.MOVEMENT);
    }
    
    @EventHandler
    private Listener<EventPlayerTravel> OnTravel = new Listener<>(event ->
    {
        if (mc.player == null || mc.player.isRiding())
            return;
        
        if (!mc.player.isPotionActive(MobEffects.LEVITATION))
            return;
        
        mc.player.setVelocity(0, 0, 0);

        final double[] dir = MathUtil.directionSpeed(0.1f);

        if (mc.player.movementInput.moveStrafe != 0 || mc.player.movementInput.moveForward != 0)
        {
            mc.player.motionX = dir[0];
            mc.player.motionZ = dir[1];
        }

        if (mc.player.movementInput.jump && !mc.player.isElytraFlying())
            mc.player.motionY = 0.1f;

        if (mc.player.movementInput.sneak)
            mc.player.motionY = -0.1f;

        event.cancel();

        mc.player.prevLimbSwingAmount = 0;
        mc.player.limbSwingAmount = 0;
        mc.player.limbSwing = 0;
    });
}
