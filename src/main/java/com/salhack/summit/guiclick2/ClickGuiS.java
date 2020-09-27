package com.salhack.summit.guiclick2;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.salhack.summit.guiclick2.components.MenuComponent;
import com.salhack.summit.guiclick2.components.MenuComponentModItem;
import com.salhack.summit.guiclick2.components.MenuListComponent;
import com.salhack.summit.guiclick2.effects.Snow;
import com.salhack.summit.gui.SalGuiScreen;
import com.salhack.summit.main.SummitStatic;
import com.salhack.summit.managers.ModuleManager;
import com.salhack.summit.module.Module;
import com.salhack.summit.module.Module.ModuleType;
import com.salhack.summit.util.colors.SalRainbowUtil;
import com.salhack.summit.util.render.RenderUtil;

import net.minecraft.client.gui.ScaledResolution;

public class ClickGuiS extends SalGuiScreen
{
    public ClickGuiS()
    {
        addModMenuComponent(ModuleType.COMBAT,     10,  10);
        addModMenuComponent(ModuleType.EXPLOIT,    130, 10);
        addModMenuComponent(ModuleType.MISC,       250, 10);
        addModMenuComponent(ModuleType.MOVEMENT,   370, 10);
        addModMenuComponent(ModuleType.RENDER,     490, 10);
        addModMenuComponent(ModuleType.UI,         610, 10);
        addModMenuComponent(ModuleType.WORLD,      730, 10);
        addModMenuComponent(ModuleType.BOT,        10, 340);
        
        for (int i = 0; i < 100; ++i)
        {
            for (int y = 0; y < 3; ++y)
            {
                Snow snow = new Snow(25 * i, y * -50);
                _snowList.add(snow);
            }
        }
    }

    private ArrayList<Snow> _snowList = new ArrayList<Snow>();
    private final ArrayList<MenuComponent> menuComponents = new ArrayList<>();
    public static SalRainbowUtil rainbowUtil = new SalRainbowUtil(9);
    
    private void addModMenuComponent(final ModuleType type, float x, float y)
    {
        final String typeString = String.valueOf(type);
        String string = typeString.substring(0,1).toUpperCase() + typeString.substring(1).toLowerCase();
        
        if (string.equals("Ui"))
            string = "UI";
        
        final List<Module> modList = ModuleManager.Get().GetModuleList(type);
        
        string += " (" + modList.size() + ")";
        
        final MenuListComponent component = new MenuListComponent(string, x, y, 100, 200, 15);
        
        float currY = 0;
        
        for (Module mod : modList)
        {
            final MenuComponent modComp = new MenuComponentModItem(mod.getDisplayName(), 0, currY, 100, 10, mod);
            component.addItem(modComp);
            
            currY += 12;
        }
        
        component.setHeight(currY + 19);
        
        menuComponents.add(component);
    }
    
    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks)
    {
        super.drawScreen(mouseX, mouseY, partialTicks);

        ScaledResolution res = new ScaledResolution(mc);

        if (!_snowList.isEmpty())
        {
            _snowList.forEach(snow -> snow.Update(res));
        }
        
        menuComponents.forEach(item -> item.onRender(mouseX, mouseY, partialTicks));
        rainbowUtil.OnRender();
    }
    
    @Override
    public void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException
    {
        super.mouseClicked(mouseX, mouseY, mouseButton);

        menuComponents.forEach(item -> item.onClicked(mouseX, mouseY, mouseButton));
    }

    @Override
    public void mouseReleased(int mouseX, int mouseY, int state)
    {
        super.mouseReleased(mouseX, mouseY, state);
        
        menuComponents.forEach(item -> item.onReleased(mouseX, mouseY, state));
    }
    
    @Override
    public void mouseClickMove(int mouseX, int mouseY, int clickedMouseButton, long timeSinceLastClick)
    {
        super.mouseClickMove(mouseX, mouseY, clickedMouseButton, timeSinceLastClick);

        menuComponents.forEach(item -> item.onClickMove(mouseX, mouseY, clickedMouseButton, timeSinceLastClick));
    }
    
    @Override
    public void onGuiClosed()
    {
        if (SummitStatic.CLICKGUI.isEnabled())
            SummitStatic.CLICKGUI.toggle();
    }
}
