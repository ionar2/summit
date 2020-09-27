package com.salhack.summit.guiclick2;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.salhack.summit.guiclick2.components.MenuComponent;
import com.salhack.summit.guiclick2.components.MenuComponentHidden;
import com.salhack.summit.guiclick2.components.MenuComponentKeybind;
import com.salhack.summit.guiclick2.components.MenuComponentValue;
import org.lwjgl.input.Mouse;

import com.salhack.summit.gui.SalGuiScreen;
import com.salhack.summit.main.Summit;
import com.salhack.summit.module.Module;
import com.salhack.summit.module.Value;
import com.salhack.summit.util.render.RenderUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;

public class ModEditorScreen extends SalGuiScreen
{
    private final GuiScreen prev;
    private String displayName;
    private List<MenuComponent> items = new ArrayList<MenuComponent>();
    private float totalY;
    private float guiWidth = 320f;
    private boolean isDragging = false;
    private float deltaX;
    private float deltaY;
    
    private float x;
    private float y;

    public ModEditorScreen(String displayName, final List<Value<?>> valList, final ClickGuiS click, final Module mod)
    {
        this.prev = click;
        
        this.displayName = displayName;
        
        float currY = 20;
        int counter = 0;
        float currX = 0;
        
        guiWidth = 135;
        
        for (Value<?> val : valList)
        {
            MenuComponentValue component = new MenuComponentValue(val, currX + 15, currY, 105, 15);
            items.add(component);
            currY += 20;
            
            if (++counter == 10)
            {
                counter = 0;
                totalY = currY;
                currY = 20;
                currX += 110;
                guiWidth += 110;
            }
        }
        
        items.add(new MenuComponentKeybind(mod, currX + 15, currY, 105, 15));
        currY += 20;
        items.add(new MenuComponentHidden(mod, currX + 15, currY, 105, 15));
        currY += 20;
        
        if (totalY < currY)
            totalY = currY;
        
        ScaledResolution res = new ScaledResolution(Minecraft.getMinecraft());
        x = res.getScaledWidth() / 2 - guiWidth / 2;
        y = res.getScaledHeight() / 2 - totalY / 2;
        
        if (y <= 10)
            y = 10;
    }

    public ModEditorScreen(String displayName, final List<Value<?>> valList, final GuiScreen click)
    {
        this.prev = click;
        
        this.displayName = displayName;
        
        float currY = 20;
        int counter = 0;
        float currX = 0;
        
        guiWidth = 135;
        
        for (Value<?> val : valList)
        {
            MenuComponentValue component = new MenuComponentValue(val, currX + 15, currY, 105, 15);
            items.add(component);
            currY += 20;
            
            if (++counter == 10)
            {
                counter = 0;
                totalY = currY;
                currY = 20;
                currX += 110;
                guiWidth += 110;
            }
        }
        
        if (totalY < currY)
            totalY = currY;
        
        ScaledResolution res = new ScaledResolution(Minecraft.getMinecraft());
        x = res.getScaledWidth() / 2 - guiWidth / 2;
        y = res.getScaledHeight() / 2 - totalY / 2;
        
        if (y <= 10)
            y = 10;
    }
    
    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks)
    {
        super.drawScreen(mouseX, mouseY, partialTicks);
        
        if (isDragging)
        {
            x = mouseX - deltaX;
            y = mouseY - deltaY;
        }
        
        prev.drawScreen(-1, -1, partialTicks);

        RenderUtil.drawRect(x,
                y,
                x + guiWidth,
                y+15,
                0xFFc34143);
        RenderUtil.drawRect(x,
                y+15,
                x + guiWidth,
                y+totalY,
                0xFF000000);
        /*Summit.GetFontManager().getGameFont().drawCenteredString(displayName,
                x + guiWidth/2,
                y+5,
                -1,
                true);*/
        
        items.forEach(item ->
        {
            item.renderWith(x, y, mouseX, mouseY, partialTicks);
        });
    }
    
    @Override
    public void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException
    {
        super.mouseClicked(mouseX, mouseY, mouseButton);
        
        if (mouseX > x && mouseX < x + guiWidth && mouseY > y && mouseY < y + totalY)
        {
            if (mouseY < y + 15)
            {
                deltaX = mouseX-x;
                deltaY = mouseY-y;
                isDragging = true;
            }

            items.forEach(item ->
            {
                item.clicked(mouseButton);
            });
        }
        else if (!isDragging)
        {
            mc.displayGuiScreen(prev);
        }
    }

    @Override
    public void mouseReleased(int mouseX, int mouseY, int state)
    {
        super.mouseReleased(mouseX, mouseY, state);
        
        isDragging = false;
        
        items.forEach(item ->
        {
            item.onReleased(mouseX, mouseY, state);
        });
    }
    
    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException
    {
        if (keyCode == 1)
        {
            mc.displayGuiScreen(prev);
        }
        else
            super.keyTyped(typedChar, keyCode);

        items.forEach(item ->
        {
            item.keyTyped(typedChar, keyCode);
        });
    }
    
    @Override
    public void handleMouseInput() throws IOException
    {
        super.handleMouseInput();
        
        if (!Mouse.getEventButtonState())
            return;
        
        items.forEach(menu ->
        {
            menu.onMouseInput();
        });
    }
}
