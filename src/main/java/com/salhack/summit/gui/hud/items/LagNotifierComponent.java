package com.salhack.summit.gui.hud.items;

import java.text.DecimalFormat;

import com.salhack.summit.events.network.EventServerPacket;
import com.salhack.summit.gui.hud.components.DraggableHudComponent;
import com.salhack.summit.util.render.RenderUtil;
import com.salhack.summit.events.bus.EventHandler;
import com.salhack.summit.events.bus.Listener;
import com.salhack.summit.util.Timer;
import net.minecraft.client.gui.ScaledResolution;

public class LagNotifierComponent extends DraggableHudComponent
{
    private Timer timer = new Timer();

    public LagNotifierComponent()
    {
        super("LagNotifier", 500, 500, 100, 100);
        setEnabled(true);
    }
    
    @EventHandler
    private Listener<EventServerPacket> onServerPacket = new Listener<>(event ->
    {
        timer.reset();
    });

    @Override
    public void onRender(ScaledResolution res, float mouseX, float mouseY, float partialTicks)
    {
        super.onRender(res, mouseX, mouseY, partialTicks);

        final float seconds = ((System.currentTimeMillis() - this.timer.getTime()) / 1000.0f) % 60.0f;
        
        if (seconds < 3)
            return;
        
        final String delay = "Server has stopped responding for " + new DecimalFormat("#.#").format(seconds) + " seconds..";

        setWidth(RenderUtil.getStringWidth(delay));
        setHeight(RenderUtil.getStringHeight(delay));

        RenderUtil.drawStringWithShadow(delay, res.getScaledWidth() / 2 - getWidth() / 2, 20, -1);
    }
}
