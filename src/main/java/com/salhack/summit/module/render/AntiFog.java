package com.salhack.summit.module.render;

import com.salhack.summit.events.render.EventRenderSetupFog;
import com.salhack.summit.module.Module;
import com.salhack.summit.events.bus.EventHandler;
import com.salhack.summit.events.bus.Listener;
import net.minecraft.client.renderer.GlStateManager;

public class AntiFog extends Module
{
    public AntiFog()
    {
        super("AntiFog", new String[] {"NoFog"}, "Prevents fog from being rendered", "NONE", 0xDB24AB, ModuleType.RENDER);
    }
    
    @EventHandler
    private Listener<EventRenderSetupFog> SetupFog = new Listener<>(p_Event ->
    {
        p_Event.cancel();
        
        GlStateManager.pushMatrix();
        GlStateManager.setFogDensity(0);
        GlStateManager.popMatrix();
    });
}
