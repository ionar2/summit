package com.salhack.summit.gui.tabgui;

import java.util.ArrayList;
import java.util.List;

import com.salhack.summit.managers.HudManager;
import com.salhack.summit.managers.ModuleManager;
import com.salhack.summit.module.Module;
import com.salhack.summit.util.render.RenderUtil;
import com.salhack.summit.events.minecraft.EventKeyInput;

public class TabGui
{
    private ArrayList<TabGuiItem> categories = new ArrayList<>();
    private TabGuiItem Hovered = null;
    private int currMainIndex = 0;
    
    // bad code starts here
    private TabGuiItem ItemToUse = null;
    private int currOffIndex = 0;
    // end
    
    public TabGui()
    {
        for (Module.ModuleType type : Module.ModuleType.values())
        {
            final List<Module> mods = ModuleManager.Get().GetModuleList(type);
            if (mods.isEmpty())
                continue;
            
            final String typeString = String.valueOf(type);
            String string = typeString.substring(0,1).toUpperCase() + typeString.substring(1).toLowerCase();
            
            if (string.equals("Ui"))
                string = "UI";
            
            TabGuiItem item = new TabGuiItem(string);
            
            for (Module mod : mods)
            {
                item.getAddons().add(new TabGuiItem(mod.getDisplayName(), mod));
            }
            
            categories.add(item);
        }
    }
    
    public void onRender(float x, float y)
    {
        RenderUtil.drawRect(x, y, x+80f, y+categories.size()*11, 0x59000000);
        RenderUtil.drawOutlineRect(x, y, x+80f, y+categories.size()*11, 3f, 0x443F3F);
        
        int totalY = 0;
        
        for (int i = 0; i < categories.size(); ++i)
        {
            TabGuiItem item =  categories.get(i);
            float offsetX = 5f;
            
            if (i == currMainIndex)
            {
                RenderUtil.drawLine(x + 1, y+totalY, x + 1, y+totalY+11, 4f, HudManager.Get().rainbow.GetRainbowColorAt(0));
                Hovered = item;
                
                if (item.isOpened())
                {
                    renderItemSubItems(item, x, y, totalY);
                }
                
                offsetX = 8f;
                item.setHovered(true);
            }
            else
            {
                item.setHovered(false);
                item.setOpened(false);
            }
            
            RenderUtil.drawStringWithShadow(item.getDisplayName(), x+offsetX, y+totalY + 3f, item.isHovered() ? HudManager.Get().rainbow.GetRainbowColorAt(0) : -1);
            
            totalY += 11;
        }
    }
    
    private void renderItemSubItems(TabGuiItem item, float currX, float currY, float totalY)
    {
        RenderUtil.drawRect(currX+81f, currY+totalY, currX+180f, currY+totalY+Hovered.getAddons().size()*11, 0x59000000);
        
        float totalSubY = 0f;
        
        int i = 0;
        
        int total = item.getAddons().size() - 1;
        
        if (currOffIndex > total)
            currOffIndex = 0;
        else if (currOffIndex < 0)
            currOffIndex = total;
        
        for (TabGuiItem subItem : item.getAddons())
        {
            float offsetX = 85f;
            
            if (i++ == currOffIndex)
            {
                RenderUtil.drawLine(currX + offsetX - 3, currY+totalY+totalSubY, currX + offsetX - 3, currY+totalY+11+totalSubY, 4f, HudManager.Get().rainbow.GetRainbowColorAt(0));
                ItemToUse = subItem;
                
                // not yet implemented.
                if (subItem.isOpened())
                {
                    for (TabGuiItem extraSubItems : subItem.getAddons())
                    {
                        renderItemSubItems(extraSubItems, currX+81f, currY+totalY, totalY);
                    }
                }

                subItem.setHovered(true);
                offsetX += 3;
            }
            else
                subItem.setHovered(false);
            
            RenderUtil.drawStringWithShadow(subItem.getDisplayName(), currX+offsetX, currY+totalY+totalSubY+3f, subItem.isHovered() ? HudManager.Get().rainbow.GetRainbowColorAt(0) : subItem.getColor());
            totalSubY += 11;
        }
    }
    
    public void onKeyInput(EventKeyInput event)
    {
        if (event.getKey().equals("UP"))
        {
            if (ItemToUse != null)
            {
                currOffIndex--;
                return;
            }
            
            if (currMainIndex <= 0)
            {
                currMainIndex = categories.size() - 1;
                return;
            }
            
            currMainIndex--;
        }
        else if (event.getKey().equals("DOWN"))
        {
            if (ItemToUse != null)
            {
                currOffIndex++;
                return;
            }
            
            if (currMainIndex >= categories.size() -1)
            {
                currMainIndex = 0;
                return;
            }
            
            currMainIndex++;
        }
        else if (event.getKey().equals("RIGHT"))
        {
            if (ItemToUse != null)
                ItemToUse.toggle();
            else if (Hovered != null)
                Hovered.setOpened(true);
        }
        else if (event.getKey().equals("LEFT"))
        {
            ItemToUse = null;
            if (Hovered != null)
                Hovered.setOpened(false);
        }
    }
}
