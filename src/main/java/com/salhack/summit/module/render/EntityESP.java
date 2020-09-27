package com.salhack.summit.module.render;

import com.salhack.summit.events.bus.EventHandler;
import com.salhack.summit.events.bus.Listener;
import com.salhack.summit.events.render.RenderEvent;
import com.salhack.summit.module.Module;
import com.salhack.summit.module.Value;
import com.salhack.summit.util.entity.EntityUtil;
import com.salhack.summit.util.render.RenderUtil;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.passive.AbstractHorse;
import net.minecraft.entity.passive.EntityTameable;
import net.minecraft.entity.player.EntityPlayer;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.util.HashMap;
import java.util.UUID;

public class EntityESP extends Module
{
    public final Value<ESPMode> Mode = new Value<ESPMode>("Mode", new String[] {"ESPMode"}, "Mode of rendering to use for ESP", ESPMode.Outline);
    
    /// Entities
    public final Value<Boolean> Players = new Value<Boolean>("Players", new String[] { "Players" }, "Highlights players", true);
    public final Value<Boolean> Monsters = new Value<Boolean>("Monsters", new String[] { "Monsters" }, "Highlights Monsters", false);
    public final Value<Boolean> Animals = new Value<Boolean>("Animals", new String[] { "Animals" }, "Highlights Animals", false);
    public final Value<Boolean> Vehicles = new Value<Boolean>("Vehicles", new String[] { "Vehicles" }, "Highlights Vehicles", false);
    public final Value<Boolean> Others = new Value<Boolean>("Others", new String[] { "Others" }, "Highlights Others", false);
    public final Value<Boolean> Items = new Value<Boolean>("Items", new String[] { "Items" }, "Highlights Items", false);
    public final Value<Boolean> Tamed = new Value<Boolean>("Tamed", new String[] { "Tamed" }, "Highlights Tamed", false);
    public final Value<Float> Width = new Value<Float>("Width", new String[] { "Width" }, "Highlights Width", 3.0f, 0.0f, 10.0f, 1.0f);

    private enum ESPMode
    {
        Outline
    }
    
    public EntityESP()
    {
        super("EntityESP", new String[] {""}, "Highlights different kind of storages", "NONE", 0xF41B94, ModuleType.RENDER);
    }
    
    private final HashMap<UUID, String> _uuidToName = new HashMap<>();

    public void doRenderOutlines(ModelBase mainModel, Entity entitylivingbaseIn, float limbSwing, float limbSwingAmount, float ageInTicks,
            float netHeadYaw, float headPitch, float scaleFactor)
    {
        if (entitylivingbaseIn == mc.player.getRidingEntity())
            return;
        
        RenderUtil.camera.setPosition(mc.getRenderViewEntity().posX, mc.getRenderViewEntity().posY, mc.getRenderViewEntity().posZ);

        if (!RenderUtil.camera.isBoundingBoxInFrustum(entitylivingbaseIn.getEntityBoundingBox()))
            return;
            
        if (entitylivingbaseIn instanceof EntityPlayer
                && !(entitylivingbaseIn instanceof EntityPlayerSP)
                && Players.getValue())
        {
            GlStateManager.pushMatrix();
            Color n = generateColor(entitylivingbaseIn);
            RenderUtil.setColor(n);
            mainModel.render(entitylivingbaseIn, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch,
                    scaleFactor);
            RenderUtil.renderOne(Width.getValue());
            mainModel.render(entitylivingbaseIn, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch,
                    scaleFactor);
            RenderUtil.renderTwo();
            mainModel.render(entitylivingbaseIn, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch,
                    scaleFactor);
            RenderUtil.renderThree();
            RenderUtil.renderFour();
            RenderUtil.setColor(n);
            mainModel.render(entitylivingbaseIn, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch,
                    scaleFactor);
            RenderUtil.renderFive();
            RenderUtil.setColor(Color.WHITE);
            GL11.glColor4f(1f, 1f, 1f, 1f);
            GlStateManager.popMatrix();
        }
        else if ((EntityUtil.isPassive(entitylivingbaseIn) && Animals.getValue())
                || (EntityUtil.isHostileMob(entitylivingbaseIn) && Monsters.getValue())
                || (EntityUtil.IsVehicle(entitylivingbaseIn) && Vehicles.getValue())
                || (entitylivingbaseIn instanceof EntityItem && Items.getValue())
                )
        {
            GlStateManager.pushMatrix();
            Color n = generateColor(entitylivingbaseIn);
            RenderUtil.setColor(n);
            mainModel.render(entitylivingbaseIn, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch,
                    scaleFactor);
            RenderUtil.renderOne(Width.getValue());
            mainModel.render(entitylivingbaseIn, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch,
                    scaleFactor);
            RenderUtil.renderTwo();
            mainModel.render(entitylivingbaseIn, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch,
                    scaleFactor);
            RenderUtil.renderThree();
            RenderUtil.renderFour();
            RenderUtil.setColor(n);
            mainModel.render(entitylivingbaseIn, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch,
                    scaleFactor);
            RenderUtil.renderFive();
            RenderUtil.setColor(Color.WHITE);
            GL11.glColor4f(1f, 1f, 1f, 1f);
            GlStateManager.popMatrix();
        }
    }

    @EventHandler
    private Listener<RenderEvent> OnRenderGameOverlay = new Listener<>(event ->
    {
        if (mc.world == null || mc.renderEngine == null || mc.getRenderManager() == null
                || mc.getRenderManager().options == null || !Tamed.getValue())
            return;

        GL11.glPushMatrix();
        GL11.glTranslated(-mc.getRenderManager().viewerPosX, -mc.getRenderManager().viewerPosY, -mc.getRenderManager().viewerPosZ);

        mc.world.loadedEntityList.forEach(e ->
        {
            if (e instanceof EntityTameable || e instanceof AbstractHorse)
            {
                UUID owner = null;
                
                if (e instanceof EntityTameable)
                    owner = ((EntityTameable)e).getOwnerId();
                if (e instanceof AbstractHorse)
                    owner = ((AbstractHorse)e).getOwnerUniqueId();
                
                if (owner != null)
                {
                    String name = getUserFromUUID(owner);
                    
                    if (name != null)
                    {
                        name += " [" + e.getEntityId() + "]";
                        
                        double distance = mc.getRenderViewEntity().getDistance(e);

                        double scale = 0.04;

                        if (distance > 0.0)
                            scale = 0.02 + (3 / 1000f) * distance;
                        
                        double posX = e.lastTickPosX + (e.posX - e.lastTickPosX) * event.getPartialTicks();
                        double posY = e.lastTickPosY + (e.posY - e.lastTickPosY) * event.getPartialTicks();
                        double posZ = e.lastTickPosZ + (e.posZ - e.lastTickPosZ) * event.getPartialTicks();

                        GL11.glPushMatrix();
                        GL11.glTranslated(posX, posY + 1.4, posZ);
                        GL11.glNormal3i(0, 1, 0);
                        GL11.glRotatef(-mc.getRenderManager().playerViewY, 0, 1, 0);
                        GL11.glRotatef(mc.getRenderManager().playerViewX, 1, 0, 0);
                        GL11.glScaled(-scale, -scale, scale);
                        GL11.glDisable(GL11.GL_LIGHTING);
                        GL11.glDisable(GL11.GL_DEPTH_TEST);
                        
                        float width = RenderUtil.getStringWidth(name) / 2;

                        GL11.glEnable(GL11.GL_BLEND);
                        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
                        RenderUtil.drawStringWithShadow(name, -width, -(12 - 1), -1);

                        GL11.glEnable(GL11.GL_DEPTH_TEST);
                        GL11.glDisable(GL11.GL_BLEND);
                        GL11.glEnable(GL11.GL_LIGHTING);
                        GL11.glColor4f(1, 1, 1, 1);
                        GL11.glPopMatrix();
                    }
                }
            }
        });

        GL11.glPopMatrix();
    });

    private String getUserFromUUID(UUID id)
    {
        return _uuidToName.computeIfAbsent(id, e -> 
            EntityUtil.getNameFromUUID(id)
        );
    }

    
    private Color generateColor(Entity e)
    {
        if (EntityUtil.isPassive(e))
            return new Color(0, 200, 0);
        if (EntityUtil.isHostileMob(e))
            return Color.RED;
        if (EntityUtil.IsVehicle(e))
            return Color.WHITE;
        /*if (e instanceof EntityPlayer)
        {
            float distance = mc.getRenderViewEntity().getDistance(e);
            
            float hue = distance + distance;
            
            if (distance >= 60.0f)
                hue = 120.0f;
            
            return Color.getHSBColor(hue/360.0f, 100.0f/360.0f, 0.55f);
        }*/
        
        return new Color(5, 255, 240);
    }
}
