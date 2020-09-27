package com.salhack.summit.gui.hud.items;

import com.salhack.summit.gui.hud.components.DraggableHudComponent;
import com.salhack.summit.gui.tabgui.TabGui;
import net.minecraft.client.gui.ScaledResolution;
import com.salhack.summit.events.bus.EventHandler;
import com.salhack.summit.events.bus.Listener;
import com.salhack.summit.events.minecraft.EventKeyInput;

public class TabGUIComponent extends DraggableHudComponent
{
    public TabGUIComponent()
    {
        super("TabGUI", 0, 25, 100, 100);
        setEnabled(true);
    }

    private TabGui gui = new TabGui();

    @Override
    public void onRender(ScaledResolution res, float mouseX, float mouseY, float partialTicks)
    {
        super.onRender(res, mouseX, mouseY, partialTicks);
        
        gui.onRender(getX(), getY());
    }
    
    @EventHandler
    private final Listener<EventKeyInput> onKeyInput = new Listener<>(event ->
    {
        gui.onKeyInput(event);
    });
}
