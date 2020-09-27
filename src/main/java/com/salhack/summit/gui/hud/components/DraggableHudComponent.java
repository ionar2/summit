package com.salhack.summit.gui.hud.components;

import com.salhack.summit.util.render.RenderUtil;
import net.minecraft.client.gui.ScaledResolution;

public class DraggableHudComponent extends HudComponentItem
{
    private boolean isDragging = false;
    private float deltaX = 0f;
    private float deltaY = 0f;
    
    public DraggableHudComponent(String displayName, float x, float y, float width, float height)
    {
        super(displayName, x, y, width, height);
    }
    
    @Override
    public void onRender(ScaledResolution res, float mouseX, float mouseY, float partialTicks)
    {
        if (isDragging())
        {
            float newX = mouseX - deltaX;
            float newY = mouseY - deltaY;

            setX(Math.min(Math.max(0, newX), res.getScaledWidth() - getWidth()));
            setY(Math.min(Math.max(0, newY), res.getScaledHeight() - getHeight()));
        }
        
        if (isMouseInThisComponent(mouseX, mouseY))
        {
            RenderUtil.drawRect(getX(), getY(), getX() + getWidth(), getY() + getHeight(), 0x50384244);
            isMouseInThis = true;
        }
        else
            isMouseInThis = false;
    }
    
    @Override
    public boolean onMouseClick(float mouseX, float mouseY, int button)
    {
        if (button == 0 && isMouseInThis)
        {
            setDragging(true);
            setDeltaX(mouseX - getX());
            setDeltaY(mouseY - getY());
            return true;
        }
        
        return false;
    }
    
    @Override
    public void onMouseRelease()
    {
        if (isDragging())
            setDragging(false);
    }

    public boolean isDragging()
    {
        return isDragging;
    }

    public void setDragging(boolean isDragging)
    {
        this.isDragging = isDragging;
    }

    public float getDeltaX()
    {
        return deltaX;
    }

    public void setDeltaX(float deltaX)
    {
        this.deltaX = deltaX;
    }

    public float getDeltaY()
    {
        return deltaY;
    }

    public void setDeltaY(float deltaY)
    {
        this.deltaY = deltaY;
    }
    
    @Override
    public void afterLoad(boolean enabled)
    {
        setEnabled(enabled);
    }
}
