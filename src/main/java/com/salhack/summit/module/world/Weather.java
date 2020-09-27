package com.salhack.summit.module.world;

import com.salhack.summit.events.bus.EventHandler;
import com.salhack.summit.events.bus.Listener;
import com.salhack.summit.events.player.EventPlayerUpdate;
import com.salhack.summit.events.render.EventRenderRainStrength;
import com.salhack.summit.module.Module;
import com.salhack.summit.module.Value;

public final class Weather extends Module
{
    public final Value<String> Mode = new Value<>("Mode", new String[] {"Mode"}, "Mode to use", "Clear");
    
    public Weather()
    {
        super("Weather", new String[]
        { "AntiWeather", "NoWeather" }, "Allows you to control the weather client-side", "NONE", 0x92F4F4, ModuleType.WORLD);
        setMetaData(getMetaData());
        
        Mode.addString("Clear");
        Mode.addString("Rain");
        Mode.addString("Thunder");
    }
    
    public String getMetaData()
    {
        if (mc.world != null)
        {
            if (mc.world.isRaining())
            {
                if (mc.world.isThundering())
                    return "Rain";
                
                return "Rain";
            }
        }
        
        return "Clear";
    }

    @EventHandler
    private Listener<EventPlayerUpdate> OnPlayerUpdate = new Listener<>(event ->
    {
        if (mc.world == null)
            return;
        
        setMetaData(getMetaData());

        if (Mode.getValue().equals("Rain"))
            mc.world.setRainStrength(1.0f);
        else if (Mode.getValue().equals("Thunder"))
            mc.world.setThunderStrength(2.0f);
    });
    
    @EventHandler
    private Listener<EventRenderRainStrength> OnRainStrength = new Listener<>(p_Event ->
    {
        if (mc.world == null)
            return;

        if (Mode.getValue().equals("Clear"))
            p_Event.cancel();
    });

}
