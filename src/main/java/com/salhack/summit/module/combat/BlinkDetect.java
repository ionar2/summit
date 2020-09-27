package com.salhack.summit.module.combat;

import java.util.HashMap;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import com.salhack.summit.events.network.EventServerPacket;
import com.salhack.summit.events.player.EventPlayerUpdate;
import com.salhack.summit.events.render.RenderEvent;
import com.salhack.summit.module.Module;
import com.salhack.summit.module.Value;
import com.salhack.summit.events.MinecraftEvent.Stage;
import com.salhack.summit.events.bus.EventHandler;
import com.salhack.summit.events.bus.Listener;
import com.salhack.summit.util.MathUtil;
import com.salhack.summit.util.render.RenderUtil;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.play.server.SPacketAnimation;
import net.minecraft.network.play.server.SPacketBlockBreakAnim;
import net.minecraft.network.play.server.SPacketEntity.S15PacketEntityRelMove;
import net.minecraft.network.play.server.SPacketEntityHeadLook;
import net.minecraft.util.math.Vec3d;

public class BlinkDetect extends Module
{
    public final Value<Float> Scaling = new Value<Float>("Scaling", new String[] {""}, "Scaling", 3f, 1f, 10f, 1f);
    
    public BlinkDetect()
    {
        super("BlinkDetect", new String[] {"BlinkDetector"}, "Highlights players that are blinking", "NONE", -1, ModuleType.COMBAT);
    }
    
    private HashMap<EntityPlayer, Long> lastMovePacketTimes = new HashMap<>();
    private List<EntityPlayer> blinkers = new CopyOnWriteArrayList<>();
    
    @EventHandler
    private Listener<EventPlayerUpdate> onUpdate = new Listener<>(event ->
    {
        final long now = System.currentTimeMillis();
        
        blinkers.removeIf(p ->
        {
            return p == null || p.getDistance(mc.player) > 50;
        });
        
        lastMovePacketTimes.keySet().removeIf(p -> p == null);
        
        mc.world.playerEntities.forEach(p ->
        {
            if (!(p instanceof EntityPlayerSP))
            {
                if (lastMovePacketTimes.containsKey(p))
                {
                    long lastTime = lastMovePacketTimes.get(p);
                    long diff = now - lastTime;
                    
                    if (diff >= 1000)
                    {
                        if (!blinkers.contains(p))
                            blinkers.add(p);
                    }
                    else
                        blinkers.remove(p);
                }
                else
                    lastMovePacketTimes.put(p, now);
            }
        });
    });

    @EventHandler
    private Listener<RenderEvent> OnRenderGameOverlay = new Listener<>(event ->
    {
        if (mc.world == null || mc.renderEngine == null || mc.getRenderManager() == null
                || mc.getRenderManager().options == null)
            return;
        
        for (EntityPlayer player : blinkers)
        {
            
            final Entity entity2 = mc.getRenderViewEntity();

            Vec3d pos = MathUtil.interpolateEntityClose(player, event.getPartialTicks());
            
            double n = pos.x;
            double distance = pos.y + 0.65;
            double n2 = pos.z;
            
            final double n3 = distance + (player.isSneaking() ? 0.0 : 0.08f) - 0.3;
            
            pos = MathUtil.interpolateEntityClose(entity2, event.getPartialTicks());
            
            final double posX = entity2.posX;
            final double posY = entity2.posY;
            final double posZ = entity2.posZ;
            
            entity2.posX = pos.x;
            entity2.posY = pos.y;
            entity2.posZ = pos.z;
            
            distance = entity2.getDistance(n, distance, n2);

            double scale = 0.04;

            if (distance > 0.0)
                scale = 0.02 + (Scaling.getValue() / 1000f) * distance;

            GlStateManager.pushMatrix();
            RenderHelper.enableStandardItemLighting();
            GlStateManager.enablePolygonOffset();
            GlStateManager.doPolygonOffset(1.0f, -1500000.0f);
            GlStateManager.disableLighting();
            GlStateManager.translate((float)n, (float)n3 + 1.4f, (float)n2);
            final float n7 = -mc.getRenderManager().playerViewY;
            final float n8 = 1.0f;
            final float n9 = 0.0f;
            GlStateManager.rotate(n7, n9, n8, n9);
            GlStateManager.rotate(mc.getRenderManager().playerViewX,
                    (mc.gameSettings.thirdPersonView == 2) ? -1.0f : 1.0f, 0.0f, (float) 0);
            GlStateManager.scale(-scale, -scale, scale);
            GlStateManager.disableDepth();
            GlStateManager.enableBlend();

            String nameTag = "possibly blinking!";

            float width = RenderUtil.getStringWidth(nameTag) / 2;
            float height =RenderUtil.getStringHeight(nameTag);

            GlStateManager.enableBlend();
            GlStateManager.disableBlend();
            RenderUtil.drawStringWithShadow(nameTag, -width+1, -height+3, 0xFF0000);

            GlStateManager.pushMatrix();

            GlStateManager.popMatrix();

            GlStateManager.enableDepth();
            GlStateManager.disableBlend();
            GlStateManager.disablePolygonOffset();
            GlStateManager.doPolygonOffset(1.0f, 1500000.0f);
            GlStateManager.popMatrix();
            
            entity2.posX = posX;
            entity2.posY = posY;
            entity2.posZ = posZ;
        }
    });
    
    @EventHandler
    private Listener<EventServerPacket> onServerPacket = new Listener<>(event ->
    { 
        if (event.getStage() != Stage.Pre || mc.world == null)
            return;
        
        if (event.getPacket() instanceof S15PacketEntityRelMove)
        {
            S15PacketEntityRelMove packet = (S15PacketEntityRelMove) event.getPacket();
            
            Entity en = packet.getEntity(mc.world);
            
            if (en != null && en instanceof EntityPlayer)
            {
                /*SendMessage(new StringBuilder("RECV S15PacketEntityRelMove for ")
                        .append(en.getName())
                        .append(" X: ")
                        .append(packet.getX())
                        .append(" Y: ")
                        .append(packet.getY())
                        .append(" Z: ")
                        .append(packet.getZ())
                        .toString());*/
                
                if (packet.getX() == 0 && packet.getY() == 0 && packet.getZ() == 0 && packet.getPitch() == 0 && packet.getPitch() == 0)
                    return;
                
                lastMovePacketTimes.put((EntityPlayer)en, System.currentTimeMillis());
            }
        }
        else if (event.getPacket() instanceof SPacketEntityHeadLook)
        {
            SPacketEntityHeadLook packet = (SPacketEntityHeadLook) event.getPacket();
            
            Entity en = packet.getEntity(mc.world);
            
            if (en != null && en instanceof EntityPlayer)
                lastMovePacketTimes.put((EntityPlayer)en, System.currentTimeMillis());
        }
        else if (event.getPacket() instanceof SPacketAnimation)
        {
            SPacketAnimation packet = (SPacketAnimation) event.getPacket();
            
            Entity en = mc.world.getEntityByID(packet.getEntityID());
            
            if (en != null && en instanceof EntityPlayer)
                lastMovePacketTimes.put((EntityPlayer)en, System.currentTimeMillis());
        }
        else if (event.getPacket() instanceof SPacketBlockBreakAnim)
        {
            SPacketBlockBreakAnim packet = (SPacketBlockBreakAnim) event.getPacket();
            
            Entity en = mc.world.getEntityByID(packet.getBreakerId());
            
            if (en != null && en instanceof EntityPlayer)
                lastMovePacketTimes.put((EntityPlayer)en, System.currentTimeMillis());
        }
    });

}
