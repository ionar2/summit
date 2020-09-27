package com.salhack.summit.module.ui;

import com.salhack.summit.gui.hud.GuiHudEditor;
import com.salhack.summit.module.Module;

public final class HudEditor extends Module
{
    public HudEditor()
    {
        super("HudEditor", new String[]{"HudEditor"}, "Displays the HudEditor", "GRAVE", 0xDBC824, ModuleType.UI);
    }
    
    @Override
    public void toggleNoSave()
    {
        
    }

    @Override
    public void onToggle()
    {
        super.onToggle();
        
        if (mc.world != null)
        {
            mc.displayGuiScreen(new GuiHudEditor(this));
        }
    }
}
