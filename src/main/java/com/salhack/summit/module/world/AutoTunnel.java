package com.salhack.summit.module.world;

import static org.lwjgl.opengl.GL11.GL_LINE_SMOOTH;
import static org.lwjgl.opengl.GL11.GL_LINE_SMOOTH_HINT;
import static org.lwjgl.opengl.GL11.GL_NICEST;
import static org.lwjgl.opengl.GL11.glDisable;
import static org.lwjgl.opengl.GL11.glEnable;
import static org.lwjgl.opengl.GL11.glHint;
import static org.lwjgl.opengl.GL11.glLineWidth;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import com.salhack.summit.events.MinecraftEvent;
import com.salhack.summit.events.bus.EventHandler;
import com.salhack.summit.events.bus.Listener;
import com.salhack.summit.events.player.EventPlayerMotionUpdate;
import com.salhack.summit.events.render.RenderEvent;
import com.salhack.summit.module.Module;
import com.salhack.summit.module.Value;
import com.salhack.summit.util.MathUtil;
import com.salhack.summit.util.render.RenderUtil;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.init.Blocks;
import net.minecraft.network.play.client.CPacketAnimation;
import net.minecraft.network.play.client.CPacketPlayerDigging;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;

public class AutoTunnel extends Module
{
    public final Value<String> Mode = new Value<>("Mode", new String[] {""}, "Mode", "Tunnel1x2");
    public final Value<String> MiningMode = new Value<>("MiningMode", new String[] {""}, "Mode of mining to use", "Normal");
    
    public AutoTunnel()
    {
        super("AutoTunnel", new String[] {""}, "Automatically mines different kind of 2d tunnels, in the direction you're facing", "NONE", -1, ModuleType.WORLD);
        setMetaData(getMetaData());
        
        Mode.addString("Tunnel1x2");
        Mode.addString("Tunnel2x2");
        Mode.addString("Tunnel2x3");
        Mode.addString("Tunnel3x3");
        
        MiningMode.addString("Normal");
        MiningMode.addString("Packet");
    }

    private List<BlockPos> blocksToMine = new CopyOnWriteArrayList<>();
    
    @EventHandler
    private Listener<EventPlayerMotionUpdate> onMotionUpdates = new Listener<>(event ->
    {
        if (event.getStage() != MinecraftEvent.Stage.Pre || event.isCancelled())
            return;
        
        int distance = 3;
        int width = 1;
        int height = 1;
        
        switch (Mode.getValue())
        {
            case "Tunnel1x2":
                height = 2;
                break;
            case "Tunnel2x2":
                width = 2;
                height = 2;
                break;
            case "Tunnel2x3":
                width = 2;
                height = 3;
                break;
            case "Tunnel3x3":
                width = 3;
                height = 3;
                break;
        }
    });
    
    @EventHandler
    private Listener<RenderEvent> OnRenderEvent = new Listener<>(p_Event ->
    {
        blocksToMine.forEach(l_Pos ->
        {
            final AxisAlignedBB bb = new AxisAlignedBB(l_Pos.getX() - mc.getRenderManager().viewerPosX,
                    l_Pos.getY() - mc.getRenderManager().viewerPosY, l_Pos.getZ() - mc.getRenderManager().viewerPosZ,
                    l_Pos.getX() + 1 - mc.getRenderManager().viewerPosX,
                    l_Pos.getY() + (1) - mc.getRenderManager().viewerPosY,
                    l_Pos.getZ() + 1 - mc.getRenderManager().viewerPosZ);
        
            RenderUtil.camera.setPosition(mc.getRenderViewEntity().posX, mc.getRenderViewEntity().posY,
                    mc.getRenderViewEntity().posZ);
        
            if (RenderUtil.camera.isBoundingBoxInFrustum(new AxisAlignedBB(bb.minX + mc.getRenderManager().viewerPosX,
                    bb.minY + mc.getRenderManager().viewerPosY, bb.minZ + mc.getRenderManager().viewerPosZ,
                    bb.maxX + mc.getRenderManager().viewerPosX, bb.maxY + mc.getRenderManager().viewerPosY,
                    bb.maxZ + mc.getRenderManager().viewerPosZ)))
            {
                GlStateManager.pushMatrix();
                GlStateManager.enableBlend();
                GlStateManager.disableDepth();
                GlStateManager.tryBlendFuncSeparate(770, 771, 0, 1);
                GlStateManager.disableTexture2D();
                GlStateManager.depthMask(false);
                glEnable(GL_LINE_SMOOTH);
                glHint(GL_LINE_SMOOTH_HINT, GL_NICEST);
                glLineWidth(1.5f);
        
                final double dist = mc.player.getDistance(l_Pos.getX() + 0.5f, l_Pos.getY() + 0.5f, l_Pos.getZ() + 0.5f)
                        * 0.75f;
        
                MathUtil.clamp((float) (dist * 255.0f / 5.0f / 255.0f), 0.0f, 0.3f);
        
                //  public static void drawBoundingBox(AxisAlignedBB bb, float width, int color)
                
                
                int l_Color = 0x9000FFFF;
                
                RenderUtil.drawBoundingBox(bb, 1.0f, l_Color);
                RenderUtil.drawFilledBox(bb, l_Color);
                glDisable(GL_LINE_SMOOTH);
                GlStateManager.depthMask(true);
                GlStateManager.enableDepth();
                GlStateManager.enableTexture2D();
                GlStateManager.disableBlend();
                GlStateManager.popMatrix();
            }
        });
    });
}
