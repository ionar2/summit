package com.salhack.summit.gui.hud.components;

import java.util.ArrayList;
import java.util.List;

import com.salhack.summit.SummitMod;
import com.salhack.summit.module.Value;
import com.salhack.summit.events.bus.EventListener;
import com.salhack.summit.main.Wrapper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;

public class HudComponentItem implements EventListener
{
    private String displayName;
    private float x;
    private float y;
    private final List<Value<?>> valueList = new ArrayList<>();
    protected boolean enabled;
    private int flags;
    private float width;
    private float height;
    protected boolean isMouseInThis;
    protected final Minecraft mc = Wrapper.GetMC();

    public HudComponentItem(String displayName, float x, float y, float width, float height)
    {
        this.displayName = displayName;
        this.x = x;
        this.y = y;
        this.setWidth(width);
        this.setHeight(height);
        this.enabled = false;
        this.flags = HudComponentFlags.None;
    }
    
    public String getDisplayName()
    {
        return displayName;
    }

    public void setDisplayName(String displayName)
    {
        this.displayName = displayName;
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

    public final List<Value<?>> getValueList()
    {
        return valueList;
    }

    public boolean isEnabled()
    {
        return enabled;
    }

    public void setEnabled(boolean enabled)
    {
        this.enabled = enabled;
        
        if (enabled)
            SummitMod.EVENT_BUS.subscribe(this);
        else
            SummitMod.EVENT_BUS.unsubscribe(this);
    }

    public int getFlags()
    {
        return flags;
    }

    public void setFlags(int flags)
    {
        this.flags = flags;
    }
    
    public void addFlag(int flags)
    {
        this.flags |= flags;
    }
    
    public void removeFlag(int flags)
    {
        this.flags &= ~flags;
    }
    
    public boolean hasFlag(int flags)
    {
        return (this.flags & flags) != 0;
    }

    public float getWidth()
    {
        return width;
    }

    public void setWidth(float width)
    {
        this.width = width;
    }

    public float getHeight()
    {
        return height;
    }

    public void setHeight(float height)
    {
        this.height = height;
    }
    
    protected boolean isMouseInThisComponent(float mouseX, float mouseY)
    {
        return mouseX != 0f && mouseY != 0f && mouseX > getX() && mouseX < getX() + getWidth() && mouseY > getY() && mouseY < getY() + getHeight();
    }
    
    // functions that will be overrided
    public void onRender(ScaledResolution res, float mouseX, float mouseY, float partialTicks)
    {
        
    }

    public boolean onMouseClick(float mouseX, float mouseY, int button)
    {
        return false;
    }
    
    public void onMouseRelease()
    {
        
    }
    
    public void onUpdate()
    {
        
    }

    public void afterLoad(boolean enabled2)
    {
    }
}
