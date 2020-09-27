package com.salhack.summit.module.render;

import com.salhack.summit.events.bus.EventHandler;
import com.salhack.summit.events.bus.Listener;
import com.salhack.summit.events.render.RenderEvent;
import com.salhack.summit.main.Wrapper;
import com.salhack.summit.managers.FriendManager;
import com.salhack.summit.module.Module;
import com.salhack.summit.module.Value;
import com.salhack.summit.util.CrystalUtils;
import com.salhack.summit.util.Pair;
import com.salhack.summit.util.entity.EntityUtil;
import com.salhack.summit.util.render.ESPUtil;
import com.salhack.summit.util.render.RenderUtil;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.stream.Collectors;

// @author: polymer
// ported over to salhack + code improved by ionar2

public class CityESP extends Module
{
    public final Value<String> HoleMode = new Value<>("Mode", new String[] {"HM"}, "Mode for rendering the blocks", "Full");

    /// Colors
    public final Value<Float> ObsidianRed = new Value<Float>("ObsidianRed", new String[] {"oRed"}, "Red for rendering", 1f, 0f, 1.0f, 0.1f);
    public final Value<Float> ObsidianGreen = new Value<Float>("ObsidianGreen", new String[] {"oGreen"}, "Green for rendering", 0f, 0f, 1.0f, 0.1f);
    public final Value<Float> ObsidianBlue = new Value<Float>("ObsidianBlue", new String[] {"oBlue"}, "Blue for rendering", 0f, 0f, 1.0f, 0.1f);
    public final Value<Float> ObsidianAlpha = new Value<Float>("ObsidianAlpha", new String[] {"oAlpha"}, "Alpha for rendering", 0.5f, 0f, 1.0f, 0.1f);

    public CityESP()
    {
        super("CityESP", new String[] {"CityESP"}, "Renders the blocks that if broken, allow you to city someone", "NONE", -1, ModuleType.RENDER);
    }

    private static final BlockPos[] surroundOffset =
    {
            new BlockPos(0, 0, -1), // north
            new BlockPos(1, 0, 0), // east
            new BlockPos(0, 0, 1), // south
            new BlockPos(-1, 0, 0) // west
    };
    
    public static ArrayList<Pair<EntityPlayer, ArrayList<BlockPos>>> GetPlayersReadyToBeCitied()
    {
        ArrayList<Pair<EntityPlayer, ArrayList<BlockPos>>> players = new ArrayList<Pair<EntityPlayer, ArrayList<BlockPos>>>();
        
        for (Entity entity : Wrapper.GetMC().world.playerEntities.stream().filter(entityPlayer -> !FriendManager.Get().IsFriend(entityPlayer)).collect(Collectors.toList()))
        {
            ArrayList<BlockPos> positions = new ArrayList<BlockPos>();
            
            for (int i = 0; i < 4; ++i)
            {
                BlockPos o = EntityUtil.GetPositionVectorBlockPos(entity, surroundOffset[i]);
                
                // ignore if the surrounding block is not obsidian
                if (Wrapper.GetMC().world.getBlockState(o).getBlock() != Blocks.OBSIDIAN)
                    continue;
                
                boolean passCheck = false;
                
                switch (i)
                {
                    case 0:
                        passCheck = CrystalUtils.canPlaceCrystal(o.north(1).down())
                            || CrystalUtils.canPlaceCrystal(o.north(1).down().east())
                            || CrystalUtils.canPlaceCrystal(o.north(1).down().west());
                        break;
                    case 1:
                        passCheck = CrystalUtils.canPlaceCrystal(o.east(1).down())
                            || CrystalUtils.canPlaceCrystal(o.east(1).down().north())
                            || CrystalUtils.canPlaceCrystal(o.east(1).down().south());
                        break;
                    case 2:
                        passCheck = CrystalUtils.canPlaceCrystal(o.south(1).down())
                            || CrystalUtils.canPlaceCrystal(o.south(1).down().west())
                            || CrystalUtils.canPlaceCrystal(o.south(1).down().east());
                        break;
                    case 3:
                        passCheck = CrystalUtils.canPlaceCrystal(o.west(1).down())
                            || CrystalUtils.canPlaceCrystal(o.west(1).down().north())
                            || CrystalUtils.canPlaceCrystal(o.west(1).down().south());
                        break;
                }

                if (passCheck)
                    positions.add(o);
            }
            
            if (!positions.isEmpty())
                players.add(new Pair<EntityPlayer, ArrayList<BlockPos>>((EntityPlayer)entity, positions));
        }
        
        return players;
    }
    
    @EventHandler
    private Listener<RenderEvent> OnRenderEvent = new Listener<>(p_Event ->
    {
        if (mc.getRenderManager() == null || mc.getRenderManager().options == null)
            return;
        
        GetPlayersReadyToBeCitied().forEach(pair ->
        {
            pair.getSecond().forEach(o ->
            {
                final AxisAlignedBB bb = new AxisAlignedBB(o.getX() - mc.getRenderManager().viewerPosX, o.getY() - mc.getRenderManager().viewerPosY,
                        o.getZ() - mc.getRenderManager().viewerPosZ, o.getX() + 1 - mc.getRenderManager().viewerPosX, o.getY() + 1 - mc.getRenderManager().viewerPosY,
                        o.getZ() + 1 - mc.getRenderManager().viewerPosZ);

                RenderUtil.camera.setPosition(mc.getRenderViewEntity().posX, mc.getRenderViewEntity().posY, mc.getRenderViewEntity().posZ);

                if (RenderUtil.camera.isBoundingBoxInFrustum(new AxisAlignedBB(bb.minX + mc.getRenderManager().viewerPosX, bb.minY + mc.getRenderManager().viewerPosY, bb.minZ + mc.getRenderManager().viewerPosZ,
                        bb.maxX + mc.getRenderManager().viewerPosX, bb.maxY + mc.getRenderManager().viewerPosY, bb.maxZ + mc.getRenderManager().viewerPosZ)))
                {
                    GlStateManager.pushMatrix();
                    GlStateManager.enableBlend();
                    GlStateManager.disableDepth();
                    GlStateManager.tryBlendFuncSeparate(770, 771, 0, 1);
                    GlStateManager.disableTexture2D();
                    GlStateManager.depthMask(false);
                    GL11.glEnable(GL11.GL_LINE_SMOOTH);
                    GL11.glHint(GL11.GL_LINE_SMOOTH_HINT, GL11.GL_NICEST);
                    GL11.glLineWidth(1.5f);
                    
                    ESPUtil.Render(HoleMode.getValue(), bb, ObsidianRed.getValue(), ObsidianGreen.getValue(), ObsidianBlue.getValue(), ObsidianAlpha.getValue());
                   
                    GL11.glDisable(GL11.GL_LINE_SMOOTH);
                    GlStateManager.depthMask(true);
                    GlStateManager.enableDepth();
                    GlStateManager.enableTexture2D();
                    GlStateManager.disableBlend();
                    GlStateManager.popMatrix();
                }
            });
        });
    });
}
