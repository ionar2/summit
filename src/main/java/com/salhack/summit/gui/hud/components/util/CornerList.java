package com.salhack.summit.gui.hud.components.util;

import java.util.ArrayList;
import java.util.Comparator;

import com.salhack.summit.managers.HudManager;
import com.salhack.summit.main.Wrapper;
import com.salhack.summit.util.render.RenderUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiChat;
import net.minecraft.client.gui.ScaledResolution;

public class CornerList
{
    private ArrayList<CornerItem> ToDisplay = new ArrayList<>();
    protected final Minecraft mc = Wrapper.GetMC();
    private final int Side;
    private float lastY;
    
    public CornerList(int side)
    {
        this.Side = side;
    }
    
    public float render(ScaledResolution res, boolean background, boolean useRainbow, int rainbowIncrease)
    {
        if (ToDisplay.isEmpty())
            return 0f;
        
        sort();
        
        ToDisplay.removeIf(mod -> mod.isMarkedToRemove());

        int i = 0;
        
        lastY = 0;
        boolean canUpdate = true;
        
        for (CornerItem mod : ToDisplay)
        {
            if (canUpdate)
                canUpdate = mod.render(lastY);
            mod.render(lastY);
            
            i += rainbowIncrease;
            
            if (i >= 360)
                i = 0;
            
            float xOffset = res.getScaledWidth() + mod.getX() - mod.getWidth() - 2;
            float yOffset = mod.getLastY() + 2;
            
            switch (Side)
            {
                case 1:
                    yOffset = res.getScaledHeight() - mod.getLastY() - 10 - (mc.currentScreen instanceof GuiChat ? 13 : 0);
                    break;
                case 2:
                    xOffset = -mod.getX();
                    yOffset = res.getScaledHeight() - mod.getLastY() - 10 - (mc.currentScreen instanceof GuiChat ? 20 : 0);
                    break;
                case 3:
                    xOffset = -mod.getX();
                    break;
            }

            if (background)
            {
                RenderUtil.drawRect(xOffset - 2f,
                        yOffset,
                        xOffset + mod.getWidth() + 2f,
                        yOffset + mod.getHeight(),
                        0x75101010);
            }

            RenderUtil.drawStringWithShadow(mod.getDisplayName(),
                    xOffset,
                    yOffset+2,
                    useRainbow ? HudManager.Get().rainbow.GetRainbowColorAt(i) : mod.getColor());
            
            lastY += mod.getHeight();
        }
        
        return lastY;
    }
    
    public void clear()
    {
        ToDisplay.clear();
    }
    
    public void sort()
    {
        final Comparator<CornerItem> comparator = (first, second) ->
        {
            final String firstName = first.getName();
            final String secondName = second.getName();
            final float dif = second.getWidth() - first.getWidth();
            return dif != 0 ? (int) dif : secondName.compareTo(firstName);
        };

        ToDisplay.sort(comparator);
    }
    
    public void removeMod(String name)
    {
        for (CornerItem mod : ToDisplay)
        {
            if (mod.getName().equals(name))
            {
                mod.fadeAway();
            }
        }
    }
    
    public void setModMetaData(String name, String metaData)
    {
        for (CornerItem mod : ToDisplay)
        {
            if (mod.getName().equals(name))
            {
                mod.setMetaData(metaData);
                mod.setWidth(RenderUtil.getStringWidth(mod.getDisplayName()));
            }
        }
    }

    public float getHeight()
    {
        return lastY;
    }

    public void addCornerItem(CornerItem cornerItem)
    {
        ToDisplay.add(cornerItem);
        cornerItem.resetAnimations();
    }

    public void removeCornerItem(CornerItem cornerItem)
    {
        ToDisplay.remove(cornerItem);
    }

    public void removeCornerWithAnimation(CornerItem cornerItem)
    {
        cornerItem.fadeAway();
    }
    
    public boolean contains(CornerItem cornerItem)
    {
        return ToDisplay.contains(cornerItem);
    }
}
