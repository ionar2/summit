package com.salhack.summit.gui.hud.items;

import com.salhack.summit.gui.hud.components.DraggableHudComponent;
import com.salhack.summit.gui.hud.components.HudComponentFlags;
import com.salhack.summit.gui.hud.components.HudComponentItem;
import com.salhack.summit.guiclick2.components.MenuComponent;
import com.salhack.summit.guiclick2.components.MenuComponentHudItem;
import com.salhack.summit.guiclick2.components.MenuComponentModItem;
import com.salhack.summit.guiclick2.components.MenuListComponent;
import com.salhack.summit.managers.HudManager;
import com.salhack.summit.module.Module;

import net.minecraft.client.gui.ScaledResolution;

public class SelectorMenuComponent extends DraggableHudComponent
{
    private MenuListComponent list;
    
    public SelectorMenuComponent()
    {
        super("Selector", 300, 300, 100, 100);
        setEnabled(true);
        addFlag(HudComponentFlags.OnlyVisibleInHudEditor);
        
        UpdateMenu();
    }
    
    public void UpdateMenu()
    {
        list = new MenuListComponent("Selector", getX(), getY(), 100, 200, 15);
        
        float currY = 0;
        
        for (HudComponentItem item : HudManager.Get().Items)
        {
            final MenuComponent hudComponent = new MenuComponentHudItem(item.getDisplayName(), 0, currY, 100, 10, item);
            list.addItem(hudComponent);
            
            currY += 12;
        }
        
        list.setHeight(currY + 19);
    }

    @Override
    public void onRender(ScaledResolution res, float mouseX, float mouseY, float partialTicks)
    {
        super.onRender(res, mouseX, mouseY, partialTicks);
        
        list.onRender(mouseX, mouseY, partialTicks);
    }

    @Override
    public boolean onMouseClick(float mouseX, float mouseY, int mouseButton)
    {
        list.onClicked((int)mouseX, (int)mouseY, mouseButton);
        return false;
    }

    @Override
    public void onMouseRelease()
    {
        super.onMouseRelease();
        list.onReleased(0, 0, 0);
    }
}
