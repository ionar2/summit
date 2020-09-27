package com.salhack.summit.module.render;

import com.salhack.summit.events.bus.EventHandler;
import com.salhack.summit.events.bus.Listener;
import com.salhack.summit.events.player.EventPlayerUpdate;
import com.salhack.summit.events.render.RenderEvent;
import com.salhack.summit.module.Module;
import com.salhack.summit.module.Value;
import com.salhack.summit.util.Hole;
import com.salhack.summit.util.render.ESPUtil;
import com.salhack.summit.util.render.RenderUtil;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;
import org.lwjgl.opengl.GL11;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class HoleESP extends Module
{
    public final Value<String> HoleMode = new Value<>("Mode", new String[] {"HM"}, "Mode for rendering holes", "FlatOutline");
    public final Value<Integer> Radius = new Value<Integer>("Radius", new String[] { "Radius", "Range", "Distance" }, "Radius in blocks to scan for holes.", 8, 0, 32, 1);
    public final Value<Boolean> IgnoreOwnHole = new Value<Boolean>("IgnoreOwnHole", new String[] {"NoSelfHole"}, "Doesn't render the hole you're standing in", false);
    
    /// Colors
    public final Value<Float> ObsidianRed = new Value<Float>("ObsidianRed", new String[] {"oRed"}, "Red for rendering", 0f, 0f, 1.0f, 0.1f);
    public final Value<Float> ObsidianGreen = new Value<Float>("ObsidianGreen", new String[] {"oGreen"}, "Green for rendering", 1f, 0f, 1.0f, 0.1f);
    public final Value<Float> ObsidianBlue = new Value<Float>("ObsidianBlue", new String[] {"oBlue"}, "Blue for rendering", 0f, 0f, 1.0f, 0.1f);
    public final Value<Float> ObsidianAlpha = new Value<Float>("ObsidianAlpha", new String[] {"oAlpha"}, "Alpha for rendering", 0.5f, 0f, 1.0f, 0.1f);

    public final Value<Float> BedrockRed = new Value<Float>("BedrockRed", new String[] {"bRed"}, "Red for rendering", 0f, 0f, 1.0f, 0.1f);
    public final Value<Float> BedrockGreen = new Value<Float>("BedrockGreen", new String[] {"bGreen"}, "Green for rendering", 1f, 0f, 1.0f, 0.1f);
    public final Value<Float> BedrockBlue = new Value<Float>("BedrockBlue", new String[] {"bBlue"}, "Blue for rendering", 0.8f, 0f, 1.0f, 0.1f);
    public final Value<Float> BedrockAlpha = new Value<Float>("BedrockAlpha", new String[] {"bAlpha"}, "Alpha for rendering", 0.5f, 0f, 1.0f, 0.1f);

    public HoleESP()
    {
        super("HoleESP", new String[] {""}, "Highlights holes for crystal pvp", "NONE", -1, ModuleType.RENDER);

        HoleMode.addString("None");
        HoleMode.addString("FlatOutline");
        HoleMode.addString("Flat");
        HoleMode.addString("Outline");
        HoleMode.addString("Full");
    }

    public final List<Hole> holes = new CopyOnWriteArrayList<>();
    
    @EventHandler
    private Listener<EventPlayerUpdate> OnPlayerUpdate = new Listener<>(p_Event ->
    {
        this.holes.clear();

        final Vec3i playerPos = new Vec3i(mc.player.posX, mc.player.posY, mc.player.posZ);

        for (int x = playerPos.getX() - Radius.getValue(); x < playerPos.getX() + Radius.getValue(); x++)
        {
            for (int z = playerPos.getZ() - Radius.getValue(); z < playerPos.getZ() + Radius.getValue(); z++)
            {
                for (int y = playerPos.getY() + Radius.getValue(); y > playerPos.getY() - Radius.getValue(); y--)
                {
                    if (!HoleMode.getValue().equals("None"))
                    {
                        final BlockPos blockPos = new BlockPos(x, y, z);

                        if (IgnoreOwnHole.getValue() && mc.player.getDistanceSq(blockPos) <= 1)
                            continue;

                        final IBlockState blockState = mc.world.getBlockState(blockPos);
    
                        Hole.HoleTypes l_Type = ESPUtil.isBlockValid(blockState, blockPos);
    
                        if (l_Type != Hole.HoleTypes.None)
                        {
                            final IBlockState downBlockState = mc.world.getBlockState(blockPos.down());
                            if (downBlockState.getBlock() == Blocks.AIR)
                            {
                                final BlockPos downPos = blockPos.down();
    
                                l_Type = ESPUtil.isBlockValid(downBlockState, blockPos);
    
                                if (l_Type != Hole.HoleTypes.None)
                                {
                                    this.holes.add(new Hole(downPos.getX(), downPos.getY(), downPos.getZ(), downPos, l_Type, true));
                                }
                            }
                            else
                            {
                                this.holes.add(new Hole(blockPos.getX(), blockPos.getY(), blockPos.getZ(), blockPos, l_Type));
                            }
                        }
                    }
                }
            }
        }
    });

    @EventHandler
    private Listener<RenderEvent> OnRenderEvent = new Listener<>(p_Event ->
    {
        if (mc.getRenderManager() == null || mc.getRenderManager().options == null)
            return;

        if (!HoleMode.getValue().equals("None"))
        {
            holes.forEach(p_Hole ->
            {
                final AxisAlignedBB bb = new AxisAlignedBB(p_Hole.getX() - mc.getRenderManager().viewerPosX, p_Hole.getY() - mc.getRenderManager().viewerPosY,
                        p_Hole.getZ() - mc.getRenderManager().viewerPosZ, p_Hole.getX() + 1 - mc.getRenderManager().viewerPosX, p_Hole.getY() + (p_Hole.isTall() ? 2 : 1) - mc.getRenderManager().viewerPosY,
                        p_Hole.getZ() + 1 - mc.getRenderManager().viewerPosZ);

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

                    switch (p_Hole.GetHoleType())
                    {
                        case Bedrock:
                            ESPUtil.Render(HoleMode.getValue(), bb, BedrockRed.getValue(), BedrockGreen.getValue(), BedrockBlue.getValue(), BedrockAlpha.getValue());
                            break;
                        case Obsidian:
                            ESPUtil.Render(HoleMode.getValue(), bb, ObsidianRed.getValue(), ObsidianGreen.getValue(), ObsidianBlue.getValue(), ObsidianAlpha.getValue());
                            break;
                        default:
                            break;
                    }

                    GL11.glDisable(GL11.GL_LINE_SMOOTH);
                    GlStateManager.depthMask(true);
                    GlStateManager.enableDepth();
                    GlStateManager.enableTexture2D();
                    GlStateManager.disableBlend();
                    GlStateManager.popMatrix();
                }
            });
        }
    });
}
