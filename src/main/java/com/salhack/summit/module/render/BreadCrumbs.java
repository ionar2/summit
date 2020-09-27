package com.salhack.summit.module.render;

import com.salhack.summit.events.bus.EventHandler;
import com.salhack.summit.events.bus.Listener;
import com.salhack.summit.events.player.EventPlayerUpdate;
import com.salhack.summit.events.render.RenderEvent;
import com.salhack.summit.module.Module;
import com.salhack.summit.module.Value;
import com.salhack.summit.util.Timer;
import com.salhack.summit.util.render.RenderUtil;

import java.awt.*;
import java.util.LinkedList;

import org.lwjgl.opengl.GL11;

import static org.lwjgl.opengl.GL11.*;

public class BreadCrumbs extends Module
{
    private Value<Boolean> Render = new Value<>("Render", new String[]
    { "Draw", "r" }, "Should this render or be silent", true);
    private Value<Float> Delay = new Value<>("Delay", new String[]
    { "Delay", "Del", "d" }, "Delay in point generation", 0.0f, 0.0f, 10.0f, 1.0f);
    private Value<Float> Width = new Value<>("Width", new String[]
    { "Width", "With", "Radius", "raidus" }, "Width of lines drawn", 1.6f, 0.1f, 10.0f, 1.0f);

    public BreadCrumbs()
    {
        super("BreadCrumbs", new String[]
        { "BreadCrumbs", "BreadMan", "BreadManCrumbs", "Breads", "BreadyCrumbs" },
                "Draws a path from the places you have gone through.", "NONE", -1, ModuleType.RENDER);
    }

    private final LinkedList<double[]> positions = new LinkedList<>();
    private Timer timer = new Timer();

    @EventHandler
    private Listener<RenderEvent> OnRenderEvent = new Listener<>(p_Event ->
    {
        if (mc.getRenderManager() == null || !Render.getValue())
            return;

        final Color color = new Color(255, 0, 72);

        synchronized (positions) {
            glPushMatrix();

            glDisable(GL_TEXTURE_2D);
            glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
            glLineWidth(Width.getValue());
            glEnable(GL_LINE_SMOOTH);
            glEnable(GL_BLEND);
            glDisable(GL_DEPTH_TEST);
            mc.entityRenderer.disableLightmap();
            glBegin(GL_LINE_STRIP);
            RenderUtil.setColor(color);
            final double renderPosX = mc.getRenderManager().viewerPosX;
            final double renderPosY = mc.getRenderManager().viewerPosY;
            final double renderPosZ = mc.getRenderManager().viewerPosZ;

            for (final double[] pos : positions)
                glVertex3d(pos[0] - renderPosX, pos[1] - renderPosY, pos[2] - renderPosZ);

            glColor4d(1, 1, 1, 1);
            glEnd();
            glEnable(GL_DEPTH_TEST);
            glDisable(GL_LINE_SMOOTH);
            glDisable(GL_BLEND);
            glEnable(GL_TEXTURE_2D);
            glPopMatrix();
        }
    });
    
    @EventHandler
    private Listener<EventPlayerUpdate> onPlayerUpdate = new Listener<>(event ->
    {
        if (!timer.passed(Delay.getValue() * 1000))
            return;

        timer.reset();

        synchronized (positions)
        {
            positions.add(new double[]
            { mc.player.posX, mc.player.getEntityBoundingBox().minY, mc.player.posZ });
        }
    });

    @Override
    public void onEnable()
    {
        super.onEnable();
        if (mc.player == null)
            return;

        synchronized (positions)
        {
            positions.add(new double[]
            { mc.player.posX, mc.player.getEntityBoundingBox().minY + (mc.player.getEyeHeight() * 0.5f),
                    mc.player.posZ });
            positions.add(new double[]
            { mc.player.posX, mc.player.getEntityBoundingBox().minY, mc.player.posZ });
        }
    }

    @Override
    public void onDisable()
    {
        synchronized (positions)
        {
            positions.clear();
        }
        super.onDisable();
    }
}
