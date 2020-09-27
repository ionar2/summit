package com.salhack.summit.guiclick2.components;

import java.util.ArrayList;
import java.util.List;

import com.salhack.summit.guiclick2.ClickGuiS;
import com.salhack.summit.main.Summit;
import com.salhack.summit.main.SummitStatic;
import com.salhack.summit.util.render.RenderUtil;

public class MenuListComponent extends MenuComponent
{
    private List<MenuComponent> Items = new ArrayList<>();
    private boolean isDragging = false;
    private float topbarHeight;
    private float deltaX;
    private float deltaY;
    
    public MenuListComponent(String displayName, float x, float y, float width, float height, float topbarHeight)
    {
        super(displayName, x, y, width, height);
        this.topbarHeight = topbarHeight;
    }
    
    public void addItem(MenuComponent item)
    {
        Items.add(item);
    }
    
    public final List<MenuComponent> getItems()
    {
        return Items;
    }

    @Override
    public void onRender(float mouseX, float mouseY, float partialTicks)
    {
        if (isInside(mouseX, mouseY))
        {
        }
        
        if (isDragging)
        {
            setX(mouseX - deltaX);
            setY(mouseY - deltaY);
        }
        
        boolean rainbow = SummitStatic.COLORS.RainbowGUI.getValue();
        
        int borderColor = rainbow ? 0xFF000000 + ClickGuiS.rainbowUtil.GetRainbowColorAt(0) : 0xFFc34143;

        RenderUtil.drawRect(getX(), getY(), getX()+getWidth(), getY()+getHeight(), 0x991E1E1E);
        RenderUtil.drawRect(getX(), getY(), getX()+getWidth(), getY() + 15, borderColor);
        
        //if (!rainbow)
        //    Summit.GetFontManager().getGameFont().drawCenteredString(getDisplayName(), getX() + getWidth() / 2, getY() + 4, -1, true);
        
        Items.forEach(item -> item.renderWith(getX(), getY()+19, mouseX, mouseY, partialTicks));
        
        RenderUtil.drawLine(getX(), getY(), getX(), getY() + getHeight(), 2, borderColor);
        RenderUtil.drawLine(getX()+getWidth(), getY(), getX()+getWidth(), getY() + getHeight(), 2, borderColor);
        RenderUtil.drawLine(getX(), getY() + getHeight(), getX()+getWidth(), getY() + getHeight(), 2, borderColor);

        RenderUtil.drawTriangle(getX(), getY()+8, 8, 180, borderColor);
        RenderUtil.drawTriangle(getX()+getWidth(), getY()+8, 8, 180, borderColor);
        RenderUtil.drawLine(getX()-4, getY(), getX(), getY() + 15, 3, borderColor);
        RenderUtil.drawLine(getX() + getWidth(), getY() + 15, getX() + getWidth() + 4, getY(), 3, borderColor);
    }

    @Override
    public void onClicked(int mouseX, int mouseY, int mouseButton)
    {
        if (isInside(mouseX, mouseY) && mouseY <= topbarHeight+getY())
        {
            isDragging = true;
            deltaX = mouseX-getX();
            deltaY = mouseY-getY();
        }
        
        Items.forEach(item -> item.clicked(mouseButton));
    }

    @Override
    public void onReleased(int mouseX, int mouseY, int state)
    {
        if (isDragging)
            isDragging = false;
        
        Items.forEach(item -> item.onReleased(mouseX, mouseY, state));
    }
}
