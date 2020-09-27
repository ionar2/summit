package com.salhack.summit.module.ui;

import com.salhack.summit.gui.chat.SalGuiConsole;
import com.salhack.summit.module.Module;

public final class Console extends Module
{
    private SalGuiConsole m_Console;

    public Console()
    {
        super("Console", new String[]{"Console"}, "Displays the click gui", "U", 0xDBB024, ModuleType.UI);
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
            if (m_Console == null)
                m_Console = new SalGuiConsole(this);
            
            mc.displayGuiScreen(m_Console);
        }
    }
}
