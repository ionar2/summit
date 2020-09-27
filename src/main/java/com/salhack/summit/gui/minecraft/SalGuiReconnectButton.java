package com.salhack.summit.gui.minecraft;

import com.salhack.summit.main.AlwaysEnabledModule;
import com.salhack.summit.main.SummitStatic;
import com.salhack.summit.util.Timer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.multiplayer.GuiConnecting;

import java.util.concurrent.TimeUnit;

public class SalGuiReconnectButton extends GuiButton
{

    public SalGuiReconnectButton(int buttonId, int x, int y, String buttonText)
    {
        super(buttonId, x, y, buttonText);
        
        timer.reset();
        
        ReconnectTimer = SummitStatic.AUTORECONNECT.Delay.getValue() * 1000f;
    }
    
    private Timer timer = new Timer();
    private float ReconnectTimer;

    @Override
    public void drawButton(Minecraft mc, int mouseX, int mouseY, float partialTicks)
    {
        super.drawButton(mc, mouseX, mouseY, partialTicks);
        
        if (visible)
        {
            if (SummitStatic.AUTORECONNECT.isEnabled() && !timer.passed(ReconnectTimer))
                this.displayString = "AutoReconnect (" + TimeUnit.MILLISECONDS.toSeconds(Math.abs((timer.getTime()+(long)ReconnectTimer) - System.currentTimeMillis())) + ")";
            else if (!SummitStatic.AUTORECONNECT.isEnabled())
                this.displayString = "AutoReconnect";
            
            if (timer.passed(ReconnectTimer) && SummitStatic.AUTORECONNECT.isEnabled() && AlwaysEnabledModule.LastIP != null && AlwaysEnabledModule.LastPort != -1)
            {
                if (mc.world == null)
                    mc.displayGuiScreen(new GuiConnecting(null, mc, AlwaysEnabledModule.LastIP, AlwaysEnabledModule.LastPort));
            }
        }
    }

    public void Clicked()
    {
        SummitStatic.AUTORECONNECT.toggle();
        
        timer.reset();
    }
}
