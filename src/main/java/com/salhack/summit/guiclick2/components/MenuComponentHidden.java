package com.salhack.summit.guiclick2.components;

import com.salhack.summit.module.Module;

public class MenuComponentHidden extends MenuComponentEditorItem
{
    private final Module mod;

    public MenuComponentHidden(final Module mod, float x, float y, float width, float height)
    {
        super("Hidden", x, y, width, height, true);
        this.mod = mod;
        displayRect = mod.isHidden();
    }
    
    @Override
    public void clicked(int mouseButton)
    {
        super.clicked(mouseButton);
        
        if (hovered)
            mod.setHidden(!mod.isHidden());
    }
    
    @Override
    public void toggle()
    {
        super.toggle();
        displayRect = toggled;
    }
}
