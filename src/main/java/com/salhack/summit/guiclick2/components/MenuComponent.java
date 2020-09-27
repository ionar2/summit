package com.salhack.summit.guiclick2.components;

import com.salhack.summit.main.Wrapper;
import net.minecraft.client.Minecraft;

public class MenuComponent
{
    private float x;
    private float y;
    private float width;
    private float height;
    private float defaultX;
    private float defaultY;
    private String displayName;
    protected final Minecraft mc = Wrapper.GetMC();
   
    public MenuComponent(String displayName, float x, float y, float width, float height)
    {
        this.displayName = displayName;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.defaultX = x;
        this.defaultY = y;
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
    public float getDefaultX()
    {
        return defaultX;
    }
    public void setDefaultX(float defaultX)
    {
        this.defaultX = defaultX;
    }
    public float getDefaultY()
    {
        return defaultY;
    }
    public void setDefaultY(float defaultY)
    {
        this.defaultY = defaultY;
    }
    public String getDisplayName()
    {
        return displayName;
    }
    public void setDisplayName(String displayName)
    {
        this.displayName = displayName;
    }
    
    public boolean isInside(float mouseX, float mouseY)
    {
        return mouseX >= getX() && mouseX < getX() + getWidth() && mouseY > getY() && mouseY < getY() + getHeight();
    }
    
    public void onRender(float mouseX, float mouseY, float partialTicks)
    {
        
    }
    
    public void renderWith(float x, float y, float mouseX, float mouseY, float partialTicks)
    {
        
    }

    public void onClicked(int mouseX, int mouseY, int mouseButton)
    {
    }

    public void onReleased(int mouseX, int mouseY, int state)
    {
    }

    public void onClickMove(int mouseX, int mouseY, int clickedMouseButton, long timeSinceLastClick)
    {
    }
    
    public void clicked(int mouseButton)
    {
    }

    public void keyTyped(char typedChar, int keyCode)
    {
    }

    public void HandleMouseInput()
    {
    }

    public void onMouseInput()
    {
    }
}
