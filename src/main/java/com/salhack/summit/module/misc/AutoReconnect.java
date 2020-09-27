package com.salhack.summit.module.misc;

import com.salhack.summit.module.Module;
import com.salhack.summit.module.Value;

public class AutoReconnect extends Module
{
    public final Value<Float> Delay = new Value<Float>("Delay", new String[] {"Delay"}, "Delay to use between attempts", 5.0f, 0.0f, 20.0f, 1.0f);
    
    public AutoReconnect()
    {
        super("AutoReconnect", new String[] {"Reconnect"}, "Automatically reconnects you to your last server", "NONE", 0x4D4D89, ModuleType.MISC);
    }
}
