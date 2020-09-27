package com.salhack.summit.gui.hud.components.util;

import com.mojang.realmsclient.gui.ChatFormatting;

import com.salhack.summit.util.render.RenderUtil;
import net.minecraft.client.Minecraft;

public class CornerItem
{
    private String name;
    private String metaData;
    private float x = 0;
    private float y = 0;
    private float width = 0;
    private int color = -1;
    private boolean isFading = false;
    private boolean isAppearing = false;
    private boolean markedToRemove = false;
    private boolean firstUpdate = false;
    private float lastY = 0f;

    public CornerItem(String name, String metaData2, int color, boolean firstUpdate)
    {
        this.setName(name);
        this.metaData = metaData2;
        this.color = color;
        this.isAppearing = true;
        this.firstUpdate = firstUpdate;
    }

    public float getWidth()
    {
        return width;
    }

    public float getHeight()
    {
        return 10f;
    }

    public void setMetaData(String metaData2)
    {
        metaData = metaData2;
    }

    public boolean render(float currY)
    {
        final float currFPS = Minecraft.getDebugFPS() / 60f;
        
        final float xIncrease = 3f / currFPS;
        final float yIncrease = 1f / currFPS;
        
        if (isFading)
        {
            if ((x+=xIncrease) >= width)
                markedToRemove = true;
            
            return true;
        }
        else if (lastY != currY)
        {
            if (lastY == 0)
            {
                lastY = currY;
                return true;
            }
            
            if (lastY > currY)
            {
                lastY -= yIncrease;
                
                if (lastY <= currY)
                    lastY = currY;
            }
            else if (lastY < currY)
                lastY = currY;
        }
        
        if (isAppearing)
        {
            if (x > 0)
            {
                x -= xIncrease;
                x = Math.max(0, x);
                return !firstUpdate;
            }
            else
            {
                firstUpdate = false;
                isAppearing = false;
            }
        }
        
        return true;
    }

    public void fadeAway()
    {
        isFading = true;
    }
    
    public boolean isMarkedToRemove()
    {
        return markedToRemove;
    }
    
    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        if (name.equals(this.name))
            return;
        
        this.name = name;
        this.width = RenderUtil.getStringWidth(name);
    }

    public float getX()
    {
        return x;
    }

    public void setX(float x)
    {
        this.x = x;
    }

    public float getY()
    {
        return y;
    }

    public void setY(float y)
    {
        this.y = y;
    }
    
    public float getLastY()
    {
        return lastY;
    }

    public void setLastY(float lastY)
    {
        this.lastY = lastY;
    }
    
    public int getColor()
    {
        return this.color;
    }
    
    public String getDisplayName()
    {
        return getName() + (metaData != null && !metaData.isEmpty() ? (" " + ChatFormatting.GRAY + metaData) : "");
    }

    public void setWidth(float f)
    {
        width = f;
    }

    public void resetAnimations()
    {
        isAppearing = true;
        firstUpdate = true;
        setX(getWidth() + 10);
    }
}
