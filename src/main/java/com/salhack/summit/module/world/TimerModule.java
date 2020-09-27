package com.salhack.summit.module.world;

import com.salhack.summit.events.network.EventServerPacket;
import com.salhack.summit.managers.TickRateManager;
import com.salhack.summit.events.MinecraftEvent.Stage;
import com.salhack.summit.events.bus.EventHandler;
import com.salhack.summit.events.bus.Listener;
import com.salhack.summit.util.Timer;

import net.minecraft.network.play.server.SPacketPlayerPosLook;

import com.salhack.summit.events.player.EventPlayerUpdate;
import com.salhack.summit.module.Module;
import com.salhack.summit.module.Value;
import java.text.DecimalFormat;

public final class TimerModule extends Module
{

    public final Value<Float> speed = new Value<Float>("Speed", new String[]
    { "Spd" }, "Tick-rate multiplier. [(20tps/second) * (this value)]", 4.0f, 0.1f, 10.0f, 0.1f);
    public final Value<Boolean> Accelerate = new Value<Boolean>("Accelerate", new String[]
    { "Acc" }, "Accelerate's from 1.0 until the anticheat lags you back", false);
    public final Value<Boolean> TPSSync = new Value<Boolean>("TPSSync", new String[]
    { "TPS" }, "Syncs the game time to the current TPS", false);

    private Timer timer = new Timer();

    public TimerModule()
    {
        super("Timer", new String[]
        { "Time", "Tmr" }, "Speeds up the client tick rate", "NONE", 0xF18219, ModuleType.WORLD);
        setMetaData(getMetaData());
    }

    @Override
    public void onDisable()
    {
        super.onDisable();
        mc.timer.tickLength = 50;
    }
    
    private float OverrideSpeed = 1.0f;
    
    /// store this as member to save cpu
    private DecimalFormat l_Format = new DecimalFormat("#.#");

    public String getMetaData()
    {
        if (OverrideSpeed != 1.0f)
            return String.valueOf(OverrideSpeed);
        
        if (TPSSync.getValue())
        {
            float l_TPS = TickRateManager.Get().getTickRate();

            return l_Format.format((l_TPS/20));
        }
        
        return l_Format.format(GetSpeed());
    }

    @EventHandler
    private Listener<EventPlayerUpdate> OnPlayerUpdate = new Listener<>(p_Event ->
    {
        setMetaData(getMetaData());
        if (OverrideSpeed != 1.0f && OverrideSpeed > 0.1f)
        {
            mc.timer.tickLength = 50.0f / OverrideSpeed;
            return;
        }
        
        if (TPSSync.getValue())
        {
            float l_TPS = TickRateManager.Get().getTickRate();

            mc.timer.tickLength = Math.min(500, 50.0f * (20/l_TPS));
        }
        else
            mc.timer.tickLength = 50.0f / GetSpeed();

        if (Accelerate.getValue() && timer.passed(2000))
        {
            timer.reset();
            speed.setValue(speed.getValue() + 0.1f);
        }
    });

    @EventHandler
    private Listener<EventServerPacket> onPlayerPosLook = new Listener<>(event ->
    {
        if (event.getStage() == Stage.Pre && event.getPacket() instanceof SPacketPlayerPosLook)
            if (Accelerate.getValue())
                speed.setValue(1.0f);
    });
    
    private float GetSpeed()
    {
        return Math.max(speed.getValue(), 0.1f);
    }

    public void SetOverrideSpeed(float f)
    {
        OverrideSpeed = f;
    }

    public float getCurrentSpeed()
    {
        if (!isEnabled())
            return 1.0f;

        if (OverrideSpeed != 1.0f)
            return OverrideSpeed;
        
        if (TPSSync.getValue())
        {
            float l_TPS = TickRateManager.Get().getTickRate();

            return (l_TPS/20);
        }
        
        return GetSpeed();
    }

}
