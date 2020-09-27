package com.salhack.summit.module.ui;

import com.salhack.summit.guiclick2.ClickGuiS;
import com.salhack.summit.module.Module;
import com.salhack.summit.module.Value;

public final class ClickGui extends Module
{
    public final Value<Boolean> AllowOverflow = new Value<Boolean>("AllowOverflow", new String[]
    { "AllowOverflow" }, "Allows the GUI to overflow", true);
    public final Value<Boolean> Watermark = new Value<Boolean>("Watermark", new String[]
    { "Watermark" }, "Displays the watermark on the GUI", true);
    public final Value<Boolean> HoverDescriptions = new Value<Boolean>("HoverDescriptions", new String[] {"HD"}, "Displays hover descriptions over values and modules", true);
    public final Value<Boolean> Snowing = new Value<Boolean>("Snowing", new String[] {"SN"}, "Play a snowing animation in ClickGUI", true);

    public ClickGui()
    {
        super("ClickGui", new String[]
        { "ClickGui", "ClickGui" }, "Displays the click gui", "RSHIFT", 0xDB9324, ModuleType.UI);
    }
    
    @Override
    public void toggleNoSave()
    {
        
    }

    @Override
    public void onEnable()
    {
        super.onEnable();
        
        if (mc.world != null)
        {
            mc.displayGuiScreen(new ClickGuiS());
        }
    }

    public void ResetToDefaults()
    {
    }
}
