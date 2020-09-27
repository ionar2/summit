package com.salhack.summit.gui.hud.items;

import java.text.SimpleDateFormat;
import java.util.Date;

import com.salhack.summit.module.Value;
import com.salhack.summit.gui.hud.components.OptionalListHudComponent;

/// @todo: Needs enum options

public class TimeComponent extends OptionalListHudComponent
{
    public final Value<Boolean> Date = new Value<Boolean>("Date", new String[] {""}, "Show current date", false);

    public TimeComponent()
    {
        super("Time", 2, 110, 0, 0);
        setEnabled(true);
    }

    SimpleDateFormat _formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");  
    
    @Override
    public void onUpdate()
    {
        Date now = new Date();

        String time = new SimpleDateFormat("h:mm a").format(now);

        if (Date.getValue())
            time += " " + _formatter.format(now);
        
        this.cornerItem.setName(time);
    }
}
