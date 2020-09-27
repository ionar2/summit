package com.salhack.summit.guiclick2.components;

import com.salhack.summit.util.render.RenderUtil;

public class MenuComponentEditorItem extends MenuComponent
{
    private boolean withRect;
    protected boolean hovered;
    protected boolean toggled;
    protected boolean displayRect;
    protected boolean outlineRectInstead;
    protected float sliderWidth;

    public MenuComponentEditorItem(String displayName, float x, float y, float width, float height, boolean withRect)
    {
        super(displayName, x, y, width, height);
        this.withRect = withRect;
    }

    @Override
    public void renderWith(float x, float y, float mouseX, float mouseY, float partialTicks)
    {
        if (mouseX >= x+getX() && mouseX < x+getX()+getWidth() && mouseY >= y+getY() && mouseY < y+getY()+getHeight())
        {
            RenderUtil.drawRect(x+getX(), y+getY()-4, x+getX()+getWidth(), y+getY()+getHeight(), 0x90000000);
            hovered = true;
        }
        else
            hovered = false;
        
        if (withRect)
        {
            if (outlineRectInstead)
            {
                RenderUtil.drawLine(x+getX(), y+getY(), x+getX()+getWidth(), y+getY(), 1, 0xFFc34143);
                RenderUtil.drawLine(x+getX(), y+getY()+getHeight(), x+getX()+getWidth(), y+getY()+getHeight(), 1, 0xFFc34143);
                RenderUtil.drawLine(x+getX(), y+getY(), x+getX(), y+getY()+getHeight(), 1, 0xFFc34143);
                RenderUtil.drawLine(x+getX()+getWidth(), y+getY(), x+getX()+getWidth(), y+getY()+getHeight(), 1, 0xFFc34143);
                
                RenderUtil.drawRect(
                        x + getX(),
                        y + getY(),
                        x + getX()+sliderWidth,
                        y + getY()+getHeight(),
                        0xFFc34143);
            }
            else
            {
                int color = 0xFFc34143;
                
                if (displayRect)
                    color = 0xFF3F0606;
                
                RenderUtil.drawRect(
                        x + getX(),
                        y + getY(),
                        x + getX()+getWidth(),
                        y + getY()+getHeight(),
                        color);
            }
        }
        
        RenderUtil.drawStringWithShadow(getDisplayName(), x + 3 + getX(), y + getY() + 4.5f, -1);
    }
    
    @Override
    public void clicked(int mouseButton)
    {
        if (mouseButton == 0 && hovered)
        {
            toggle();
        }
    }
    
    public void toggle()
    {
        toggled = !toggled;
    }
}
