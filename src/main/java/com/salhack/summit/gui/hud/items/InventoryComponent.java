package com.salhack.summit.gui.hud.items;

import com.salhack.summit.gui.hud.components.DraggableHudComponent;
import com.salhack.summit.module.Value;
import com.salhack.summit.util.render.RenderUtil;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.item.ItemStack;

public class InventoryComponent extends DraggableHudComponent
{
	public final Value<Boolean> ShowHotbar = new Value<Boolean>("ShowHotbar", new String[] {""}, "Displays the hotbar", false);
    public final Value<Boolean> ShowXCarry = new Value<Boolean>("ShowXCarry", new String[] {""}, "Displays the crafting inventory", true);
    public final Value<Boolean> Background = new Value<Boolean>("Background", new String[] {""}, "Displays the Background", true);
    public final Value<Float> Scale = new Value<Float>("Scale", new String[] {""}, "Allows you to modify the scale", 1.0f, 0.0f, 10.0f, 1.0f);
	
    public InventoryComponent()
    {
        super("Inventory", 2, 15, 100, 100);
        setEnabled(true);
    }

    @Override
    public void onRender(ScaledResolution res, float mouseX, float mouseY, float partialTicks)
    {
        super.onRender(res, mouseX, mouseY, partialTicks);
        
        GlStateManager.pushMatrix();
        RenderHelper.enableGUIStandardItemLighting();
        GlStateManager.scale(Scale.getValue(), Scale.getValue(), Scale.getValue());
        if (Background.getValue())
            RenderUtil.drawRect(getX(), getY(), getX() + getWidth(), getY() + getHeight(), 0x75101010); // background
        for (int i = 0; i < 27; i++)
        {
            ItemStack itemStack = mc.player.inventory.mainInventory.get(i + 9);
            int offsetX = (int) getX() + (i % 9) * 16;
            int offsetY = (int) getY() + (i / 9) * 16;
			mc.getRenderItem().renderItemAndEffectIntoGUI(itemStack, offsetX, offsetY);
			mc.getRenderItem().renderItemOverlayIntoGUI(mc.fontRenderer, itemStack, offsetX, offsetY, null);
		}

		if (ShowHotbar.getValue())
        {
	        for (int i = 0; i < 9; i++)
	        {
	            ItemStack itemStack = mc.player.inventory.mainInventory.get(i);
	            int offsetX = (int) getX() + (i % 9) * 16;
	            int offsetY = (int) getY() + 48;
	            mc.getRenderItem().renderItemAndEffectIntoGUI(itemStack, offsetX, offsetY);
	            mc.getRenderItem().renderItemOverlayIntoGUI(mc.fontRenderer, itemStack, offsetX, offsetY, null);
	        }
        }

		if (ShowXCarry.getValue())
		{
	        if (Background.getValue())
	            RenderUtil.drawRect(getX() + getWidth(), getY(), getX() + getWidth() + 32, getY() + 32, 0x75101010); // background
    
            for (int i = 1; i < 5; i++)
            {
                ItemStack itemStack = mc.player.inventoryContainer.getInventory().get(i);
    
                int offsetX = (int) getX();
                int offsetY = (int) getY();
    
                switch (i)
                {
                    case 1:
                    case 2:
                        offsetX += 128 + (i * 16);
                        break;
                    case 3:
                    case 4:
                        offsetX += 128 + ((i - 2) * 16);
                        offsetY += 16;
                        break;
                }
    
                mc.getRenderItem().renderItemAndEffectIntoGUI(itemStack, offsetX, offsetY);
                mc.getRenderItem().renderItemOverlayIntoGUI(mc.fontRenderer, itemStack, offsetX, offsetY, null);
            }
		}
		
        setWidth(16 * 9 );
        setHeight(16 * (ShowHotbar.getValue() ? 4 : 3));

        RenderHelper.disableStandardItemLighting();
        mc.getRenderItem().zLevel = 0.0F;
        GlStateManager.popMatrix();
    }
}
