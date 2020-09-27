package com.salhack.summit.module.movement;

import com.salhack.summit.events.MinecraftEvent;
import com.salhack.summit.events.MinecraftEvent.Stage;
import com.salhack.summit.events.network.EventServerPacket;
import com.salhack.summit.events.player.EventPlayerMove;
import com.salhack.summit.events.player.EventPlayerUpdate;
import com.salhack.summit.main.SummitStatic;
import com.salhack.summit.module.Module;
import com.salhack.summit.module.Value;
import com.salhack.summit.util.MathUtil;
import com.salhack.summit.events.bus.EventHandler;
import com.salhack.summit.events.bus.Listener;
import net.minecraft.init.MobEffects;
import net.minecraft.network.play.server.SPacketPlayerPosLook;

public class FastSwim extends Module
{
    public final Value<String> Mode = new Value<>("Mode", new String[] {"M"}, "What mode should fast swim be on", "AAC");
    
    public enum Modes
    {
        Pulse,
        AAC,
        Boost,
    }
    
    public FastSwim()
    {
        super("FastSwim", new String[] {"FastSwim", "fastswim", "swim"}, "Allows you to swim faster than normal", "NONE", 0x00C3F4, ModuleType.MOVEMENT);
        setMetaData(getMetaData());
        
        Mode.addString("AAC");
        Mode.addString("Pulse");
        Mode.addString("Boost");
    }
    
    private int _ticks = 0;
    
    public String getMetaData()
    {
        return String.valueOf(Mode.getValue());
    }
    
    @Override
    public void onEnable()
    {
        super.onEnable();
        _ticks = 0;
    }

    @EventHandler
    private Listener<EventPlayerUpdate> onUpdate = new Listener<>(event ->
    {
        setMetaData(getMetaData());
        
        if (!mc.player.isInWater() || mc.player.isElytraFlying())
            return;
        
        switch (Mode.getValue())
        {
            case "AAC":
            {
                double[] dir = MathUtil.directionSpeed(0.095f);
                
                mc.player.motionX = dir[0];
                mc.player.motionZ = dir[1];
                break;
            }
            case "Pulse":
            {
                ++_ticks;
                if (_ticks == 4)
                {
                    double[] dir = MathUtil.directionSpeed(0.08f);
                    
                    mc.player.motionX = dir[0];
                    mc.player.motionZ = dir[1];
                }
                else if (_ticks >= 5)
                {
                    _ticks = 0;
                    
                    double[] dir = MathUtil.directionSpeed(0.10f);
                    
                    mc.player.motionX = dir[0];
                    mc.player.motionZ = dir[1];  
                }
                break;
            }
            default:
                break;
        }
    });

    @EventHandler
    private Listener<EventServerPacket> onPlayerPosLook = new Listener<>(event ->
    {
        if (event.getStage() == Stage.Pre && event.getPacket() instanceof SPacketPlayerPosLook)
            _ticks = 0;
    });
    

    @EventHandler
    private Listener<EventPlayerMove> OnPlayerMove = new Listener<>(p_Event ->
    {
        if (p_Event.getStage() != MinecraftEvent.Stage.Pre || !Mode.getValue().equals("Boost") || !mc.player.isInWater() || p_Event.isCancelled())
            return;
        
        ++_ticks;
        if (_ticks < 15)
            return;
        
        if (mc.player.capabilities != null)
        {
            if (mc.player.capabilities.isFlying || SummitStatic.FLIGHT.isEnabled() || mc.player.isElytraFlying())
                return;
        }
        
        if (mc.player.onGround)
            return;
        
        // movement data variables
        float playerSpeed = 0.42f;
        float moveForward = mc.player.movementInput.moveForward;
        float moveStrafe = mc.player.movementInput.moveStrafe;
        float rotationYaw = mc.player.rotationYaw;

        // check for speed potion
        if (mc.player.isPotionActive(MobEffects.SPEED))
        {
            final int amplifier = mc.player.getActivePotionEffect(MobEffects.SPEED).getAmplifier();
            playerSpeed *= (1.0f + 0.2f * (amplifier + 1));
        }

        // not movement input, stop all motion
        if (moveForward == 0.0f && moveStrafe == 0.0f)
        {
            p_Event.X = (0.0d);
            p_Event.Z = (0.0d);
        }
        else
        {
            if (moveForward != 0.0f)
            {
                if (moveStrafe > 0.0f)
                {
                    rotationYaw += ((moveForward > 0.0f) ? -45 : 45);
                }
                else if (moveStrafe < 0.0f)
                {
                    rotationYaw += ((moveForward > 0.0f) ? 45 : -45);
                }
                moveStrafe = 0.0f;
                if (moveForward > 0.0f)
                {
                    moveForward = 1.0f;
                }
                else if (moveForward < 0.0f)
                {
                    moveForward = -1.0f;
                }
            }
            p_Event.X = ((moveForward * playerSpeed) * Math.cos(Math.toRadians((rotationYaw + 90.0f))) + (moveStrafe * playerSpeed) * Math.sin(Math.toRadians((rotationYaw + 90.0f))));
            p_Event.Z = ((moveForward * playerSpeed) * Math.sin(Math.toRadians((rotationYaw + 90.0f))) - (moveStrafe * playerSpeed) * Math.cos(Math.toRadians((rotationYaw + 90.0f))));
        }
        p_Event.cancel();
    });
}
