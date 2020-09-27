package com.salhack.summit.module.ui;

import com.salhack.summit.module.Module;
import com.salhack.summit.module.Value;

public class Commands extends Module
{
    public static final Value<String> Prefix = new Value<>("Prefix", new String[] {"P"}, "Prefix for the command system", ".");
    
    public Commands()
    {
        super("Commands", new String[] { "Commands", "ChatCommands"}, "Chat commands", "NONE", -1, ModuleType.UI);
    }
}
