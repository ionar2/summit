package com.salhack.summit.module.render;

import com.salhack.summit.events.bus.EventHandler;
import com.salhack.summit.events.bus.Listener;
import com.salhack.summit.events.render.EventRenderTooltip;
import com.salhack.summit.module.Module;
import org.lwjgl.opengl.GL11;

import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.item.ItemMap;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.storage.MapData;

public class MapTooltip extends Module
{
    public MapTooltip()
    {
        super("MapTooltip", new String[] {"MT"}, "Displays a map preview", "NONE", -1, ModuleType.RENDER);
    }

    private static final ResourceLocation RES_MAP_BACKGROUND = new ResourceLocation("textures/map/map_background.png");
    
    @EventHandler
    private Listener<EventRenderTooltip> OnRenderTooltip = new Listener<>(event ->
    {
        if (!event.getItemStack().isEmpty() && event.getItemStack().getItem() instanceof ItemMap)
        {
            MapData mapData = ((ItemMap)event.getItemStack().getItem()).getMapData(event.getItemStack(), mc.world);
            
            if (mapData != null)
            {
                event.cancel();
                
                GlStateManager.pushMatrix();
                GlStateManager.color(1f, 1f, 1f);
                
                int xl = event.getX() + 6;
                int yl = event.getY() + 6;

                Tessellator tessellator = Tessellator.getInstance();
                BufferBuilder bufferbuilder = tessellator.getBuffer();

                GlStateManager.translate(xl, yl, 0.0);
                GlStateManager.scale(1f, 1f, 0f);
                mc.getTextureManager().bindTexture(RES_MAP_BACKGROUND);
                RenderHelper.disableStandardItemLighting();

                GL11.glDepthRange(0, 0.01);
                bufferbuilder.begin(7, DefaultVertexFormats.POSITION_TEX);
                bufferbuilder.pos(-7.0D, 135.0D, 0.0D).tex(0.0D, 1.0D).endVertex();
                bufferbuilder.pos(135.0D, 135.0D, 0.0D).tex(1.0D, 1.0D).endVertex();
                bufferbuilder.pos(135.0D, -7.0D, 0.0D).tex(1.0D, 0.0D).endVertex();
                bufferbuilder.pos(-7.0D, -7.0D, 0.0D).tex(0.0D, 0.0D).endVertex();
                tessellator.draw();
                GL11.glDepthRange(0, 1.0);
                RenderHelper.enableStandardItemLighting();

                GlStateManager.disableDepth();
                GL11.glDepthRange(0, 0.01);
                mc.entityRenderer.getMapItemRenderer().renderMap(mapData, false);
                GL11.glDepthRange(0, 1.0);
                GlStateManager.enableDepth();
                
                GlStateManager.popMatrix();
            }
        }
    });
}
