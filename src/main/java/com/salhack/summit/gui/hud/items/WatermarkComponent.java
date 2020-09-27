package com.salhack.summit.gui.hud.items;

import com.salhack.summit.gui.hud.components.DraggableHudComponent;
import com.salhack.summit.SummitMod;
import com.salhack.summit.util.render.RenderUtil;
import net.minecraft.client.gui.ScaledResolution;

public class WatermarkComponent extends DraggableHudComponent
{
    public WatermarkComponent()
    {
        super("Watermark", 2, 6, 100, 100);
        setEnabled(true);
    }

    @Override
    public void onRender(ScaledResolution res, float mouseX, float mouseY, float partialTicks)
    {
        super.onRender(res, mouseX, mouseY, partialTicks);

        RenderUtil.drawStringWithShadow(SummitMod.NAME,
                getX(),
                getY(),
                0xFF0000);
        
        setWidth(83);
        setHeight(16);
    }
}
