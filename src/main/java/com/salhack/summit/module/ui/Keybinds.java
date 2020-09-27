package com.salhack.summit.module.ui;

import com.salhack.summit.module.Module;
import com.salhack.summit.module.Value;

public class Keybinds extends Module
{
    public final Value<Boolean> Shift = new Value<Boolean>("StrictShift", new String[] {"Shift"}, "Activates strict keybinds when shift key is down", false);
    public final Value<Boolean> Ctrl = new Value<Boolean>("StrictCtrl", new String[] {"Ctrl"}, "Activates strict keybinds when ctrl key is down", false);
    public final Value<Boolean> Alt = new Value<Boolean>("StrictAlt", new String[] {"Alt"}, "Activates strict keybinds when alt key is down", false);

    public Keybinds()
    {
        super("Keybinds", new String[] {"Keys"}, "Allows you to modify the behavior of keybinds", "NONE", -1, ModuleType.UI);
    }

}
