package com.salhack.summit.gui.chest;

import com.salhack.summit.main.SummitStatic;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.gui.inventory.GuiChest;
import net.minecraft.inventory.IInventory;

import java.io.IOException;

public class SalGuiChest extends GuiChest
{
    public SalGuiChest(IInventory upperInv, IInventory lowerInv)
    {
        super(upperInv, lowerInv);
        
        this.mc = Minecraft.getMinecraft();
        ScaledResolution l_Res = new ScaledResolution(mc);
        
        this.setWorldAndResolution(this.mc, l_Res.getScaledWidth(), l_Res.getScaledHeight());
    }
    
    private boolean WasEnabledByGUI = false;
    
    @Override
    public void initGui()
    {
        super.initGui();
        
        this.buttonList.add(new GuiButton(1337, this.width / 2 + 100, this.height / 2 - this.ySize + 110, 50, 20, "Steal"));
        this.buttonList.add(new GuiButton(1338, this.width / 2 + 100, this.height / 2 - this.ySize + 130, 50, 20, "Store"));
        this.buttonList.add(new GuiButton(1339, this.width / 2 + 100, this.height / 2 - this.ySize + 150, 50, 20, "Drop"));
    }
    
    @Override
    protected void actionPerformed(GuiButton button) throws IOException
    {
        super.actionPerformed(button);
        
        switch (button.id)
        {
            case 1337:
                SummitStatic.CHESTSTEALER.Mode.setValue("Steal");
                
                if (!SummitStatic.CHESTSTEALER.isEnabled())
                {
                    WasEnabledByGUI = true;
                    SummitStatic.CHESTSTEALER.toggle();
                }
                break;
            case 1338:
                SummitStatic.CHESTSTEALER.Mode.setValue("Store");

                if (!SummitStatic.CHESTSTEALER.isEnabled())
                {
                    WasEnabledByGUI = true;
                    SummitStatic.CHESTSTEALER.toggle();
                }
                break;
            case 1339:
                SummitStatic.CHESTSTEALER.Mode.setValue("Drop");

                if (!SummitStatic.CHESTSTEALER.isEnabled())
                {
                    WasEnabledByGUI = true;
                    SummitStatic.CHESTSTEALER.toggle();
                }
                break;
        }
    }

    
    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks)
    {
        super.drawScreen(mouseX, mouseY, partialTicks);
    }
    
    @Override
    public void onGuiClosed()
    {
        super.onGuiClosed();
        
        if (WasEnabledByGUI)
            if (SummitStatic.CHESTSTEALER.isEnabled())
                SummitStatic.CHESTSTEALER.toggle();
    }
}
