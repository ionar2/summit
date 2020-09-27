package com.salhack.summit.guiclick2.components;

import com.salhack.summit.guiclick2.ClickGuiS;
import com.salhack.summit.guiclick2.ModEditorScreen;
import com.salhack.summit.module.Module;

public class MenuComponentModItem extends MenuComponentItem
{
    private final Module mod;

    public MenuComponentModItem(String displayName, float x, float y, float width, float height, final Module mod)
    {
        super(displayName, x, y, width, height, -1, !mod.getValueList().isEmpty(), mod.isEnabled());
        
        this.mod = mod;
    }
    
    @Override
    public void onRender(float mouseX, float mouseY, float partialTicks)
    {
        super.onRender(mouseX, mouseY, partialTicks);
    }
    
    @Override
    public void clicked(int mouseButton)
    {
        super.clicked(mouseButton);

        if (mouseInside)
        {
            if (mouseButton == 1)
                mc.displayGuiScreen(new ModEditorScreen(mod.getDisplayName(), mod.getValueList(), (ClickGuiS) mc.currentScreen, mod));
            else if (mouseButton == 0)
                mod.toggle();
        }
    }
}
