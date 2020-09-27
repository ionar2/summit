package com.salhack.summit.guiclick2.components;

import com.salhack.summit.guiclick2.ClickGuiS;
import com.salhack.summit.main.SummitStatic;
import com.salhack.summit.util.render.RenderUtil;

public class MenuComponentItem extends MenuComponent
{
    private int color;
    private boolean drawCircle;
    protected boolean enabled;
    protected boolean mouseInside = false;

    public MenuComponentItem(String displayName, float x, float y, float width, float height, int color, boolean drawCircle, boolean enabled)
    {
        super(displayName, x, y, width, height);
        setColor(color);
        this.drawCircle = drawCircle;
        this.enabled = enabled;
    }
    
    public int getColor()
    {
        return enabled ? (SummitStatic.COLORS.RainbowGUI.getValue() ? ClickGuiS.rainbowUtil.GetRainbowColorAt(0) : 0xd74749) : color;
    }
    
    public void setColor(int color)
    {
        this.color = color;
    }
    
    @Override
    public void renderWith(float x, float y, float mouseX, float mouseY, float partialTicks)
    {
        if (drawCircle)
        {
            RenderUtil.drawCircle(x + getX() + getWidth() - 5, y + getY()+3.5f, 2f, SummitStatic.COLORS.RainbowGUI.getValue() ? 0xFF000000 | ClickGuiS.rainbowUtil.GetRainbowColorAt(0) : 0xFFb64a4a);
        }
        
        if (mouseX >= x+getX() && mouseX < x+getX()+getWidth() && mouseY >= y+getY() && mouseY < y+getY()+getHeight())
        {
            RenderUtil.drawRect(x+getX(), y+getY()-4, x+getX()+getWidth(), y+getY()+getHeight(), 0x90000000);
            mouseInside = true;
        }
        else
            mouseInside = false;
        
        RenderUtil.drawStringWithShadow(getDisplayName(), x + 3 + getX(), y + getY(), getColor());
    }

    @Override
    public void clicked(int mouseButton)
    {
        if (mouseInside && mouseButton == 0)
            toggle();
    }
    
    public void toggle()
    {
        enabled = !enabled;
    }
}
