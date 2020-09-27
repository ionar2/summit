package com.salhack.summit.gui.hud.items;

import com.mojang.realmsclient.gui.ChatFormatting;

import com.salhack.summit.events.player.EventPlayerUpdate;
import com.salhack.summit.gui.hud.components.OptionalListHudComponent;
import com.salhack.summit.events.bus.EventHandler;
import com.salhack.summit.events.bus.Listener;
import com.salhack.summit.util.Timer;
import net.minecraft.client.Minecraft;

public class FPSComponent extends OptionalListHudComponent
{
    public FPSComponent()
    {
        super("FPS", 2, 140);
        setEnabled(true);
    }
    
    private Timer timer = new Timer();
    private int[] FPS = { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };

    @Override
    public void onUpdate()
    {
        this.cornerItem.setName(new StringBuilder("FPS ").append(ChatFormatting.WHITE).append(Minecraft.getDebugFPS())
                .append(ChatFormatting.RESET)
                .append(" Average ")
                .append(ChatFormatting.WHITE)
                .append(getAverage()).toString());
        
    }
    
    private int getAverage()
    {
        int avg = 0;
        
        for (int i : FPS)
            avg += i;
        
        return avg / 10;
    }

    @EventHandler
    private Listener<EventPlayerUpdate> onPlayerUpdate = new Listener<>(event ->
    {
        if (timer.passed(1000))
        {
            timer.reset();
            
            for (int i = 0; i < FPS.length - 1; ++i)
            {
                FPS[i] = FPS[i+1];
            }
            
            FPS[FPS.length - 1] = Minecraft.getDebugFPS();
        }
    });
}
