package com.salhack.summit.gui.hud;

import com.salhack.summit.gui.hud.components.HudComponentItem;
import com.salhack.summit.main.SummitStatic;
import com.salhack.summit.managers.HudManager;
import com.salhack.summit.gui.SalGuiScreen;
import net.minecraft.client.gui.ScaledResolution;

import org.lwjgl.opengl.GL11;

import java.io.IOException;

public class GuiHudEditor extends SalGuiScreen
{
    public GuiHudEditor(com.salhack.summit.module.ui.HudEditor p_HudEditor)
    {
        super();
        
        SummitStatic.SELECTORMENU.UpdateMenu();
        HudEditor = p_HudEditor;
    }
    
    private com.salhack.summit.module.ui.HudEditor HudEditor;
    private boolean Clicked = false;
    private boolean Dragging = false;
    private int ClickMouseX = 0;
    private int ClickMouseY = 0;
    
    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks)
    {
        super.drawScreen(mouseX, mouseY, partialTicks);
        this.drawDefaultBackground();

        GL11.glPushMatrix();

        HudComponentItem l_LastHovered = null;
        
        ScaledResolution res = new ScaledResolution(mc);

        for (HudComponentItem l_Item : HudManager.Get().Items)
        {
            if (l_Item.isEnabled())
                l_Item.onRender(res, mouseX, mouseY, partialTicks);
        }

        if (l_LastHovered != null)
        {
            /// Add to the back of the list for rendering
            HudManager.Get().Items.remove(l_LastHovered);
            HudManager.Get().Items.add(l_LastHovered);
        }
        
        /*if (Clicked)
        {
            final float l_MouseX1 = Math.min(ClickMouseX, mouseX);
            final float l_MouseX2 = Math.max(ClickMouseX, mouseX);
            final float l_MouseY1 = Math.min(ClickMouseY, mouseY);
            final float l_MouseY2 = Math.max(ClickMouseY, mouseY);
            
            RenderUtil.drawOutlineRect(l_MouseX2, l_MouseY2, l_MouseX1, l_MouseY1, 1, 0x75056EC6);
            RenderUtil.drawRect(l_MouseX1, l_MouseY1, l_MouseX2, l_MouseY2, 0x56EC6, 205);

            HudManager.Get().Items.forEach(p_Item ->
            {
                if (!p_Item.IsHidden())
                {
                    if (p_Item.IsInArea(l_MouseX1, l_MouseX2, l_MouseY1, l_MouseY2))
                        p_Item.SetSelected(true);
                    else if (p_Item.IsSelected())
                        p_Item.SetSelected(false);
                }
            });
        }*/
        
        GL11.glPopMatrix();
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException
    {
        super.mouseClicked(mouseX, mouseY, mouseButton);

        for (HudComponentItem l_Item : HudManager.Get().Items)
        {
            if (l_Item.isEnabled())
            {
                if (l_Item.onMouseClick(mouseX, mouseY, mouseButton))
                    return;
            }
        }

        Clicked = true;
        ClickMouseX = mouseX;
        ClickMouseY = mouseY;
    }

    @Override
    public void mouseReleased(int mouseX, int mouseY, int state)
    {
        super.mouseReleased(mouseX, mouseY, state);
        
        HudManager.Get().Items.forEach(p_Item ->
        {
            if (p_Item.isEnabled())
            {
                p_Item.onMouseRelease();

                /*if (p_Item.IsSelected())
                    p_Item.SetMultiSelectedDragging(true);
                else
                    p_Item.SetMultiSelectedDragging(false);*/
            }
        });

        Clicked = false;
    }
    
    @Override
    public void mouseClickMove(int mouseX, int mouseY, int clickedMouseButton, long timeSinceLastClick)
    {
        super.mouseClickMove(mouseX, mouseY, clickedMouseButton, timeSinceLastClick);
    }

    @Override
    public void onGuiClosed()
    {
        super.onGuiClosed();

        if (HudEditor.isEnabled())
            HudEditor.toggle();

        Clicked = false;
        Dragging = false;
        ClickMouseX = 0;
        ClickMouseY = 0;
        HudManager.Get().onHudEditorClosed();
    }
}
