package com.salhack.summit.module.render;

import com.salhack.summit.events.bus.EventHandler;
import com.salhack.summit.events.bus.Listener;
import com.salhack.summit.events.player.EventPlayerUpdate;
import com.salhack.summit.events.render.RenderEvent;
import com.salhack.summit.module.Module;
import com.salhack.summit.module.Value;
import com.salhack.summit.util.BlockInteractionHelper;
import com.salhack.summit.util.Timer;
import com.salhack.summit.util.entity.PlayerUtil;
import com.salhack.summit.util.render.RenderUtil;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;

public class FarmESP extends Module
{
    public final Value<Integer> Radius = new Value<Integer>("Radius", new String[]
    { "Radius", "Range", "Distance" }, "Radius in blocks to scan for blocks.", 32, 0, 100, 1);
    public final Value<Float> Red = new Value<Float>("Red", new String[]
    { "oRed" }, "Red for rendering", 0f, 0f, 1.0f, 0.1f);
    public final Value<Float> Green = new Value<Float>("Green", new String[]
    { "oGreen" }, "Green for rendering", 1f, 0f, 1.0f, 0.1f);
    public final Value<Float> Blue = new Value<Float>("Blue", new String[]
    { "oBlue" }, "Blue for rendering", 0f, 0f, 1.0f, 0.1f);
    public final Value<Float> Alpha = new Value<Float>("Alpha", new String[]
    { "oAlpha" }, "Alpha for rendering", 0.5f, 0f, 1.0f, 0.1f);

    public final Value<String> Mode = new Value<>("Mode", new String[]
    { "M" }, "Mode for rendering around blocks", "Full");
    public final Value<Float> Delay = new Value<Float>("Delay", new String[]
    { "D" }, "Delay for updating, Higher this if you experience fps drops.", 1.0f, 0.0f, 10.0f, 1.0f);


    public FarmESP()
    {
        super("FarmESP", new String[]
        { "FarmlandESP" }, "Various rendering tools for farms", "NONE", 0xDB24AB, ModuleType.RENDER);
        
        Mode.addString("FlatOutline");
        Mode.addString("Flat");
        Mode.addString("Outline");
        Mode.addString("Full");
    }

    private ArrayList<BlockPos> PositionsToHighlight = new ArrayList<BlockPos>();
    private Timer timer = new Timer();

    @EventHandler
    private Listener<EventPlayerUpdate> OnPlayerUpdate = new Listener<>(p_Event ->
    {
        if (timer.passed(Delay.getValue() * 1000))
        {
            timer.reset();
            new Thread(() -> 
            {
                PositionsToHighlight.clear();
                
                BlockInteractionHelper.getSphere(PlayerUtil.GetLocalPlayerPosFloored(),
                        Radius.getValue(), Radius.getValue(), false, true, 0).forEach(p_Pos ->
                {
                    IBlockState l_State = mc.world.getBlockState(p_Pos);
        
                    if (l_State != null && l_State.getBlock() == Blocks.FARMLAND)
                    {
                        if (!hasWater(mc.world, p_Pos) && !mc.world.isRainingAt(p_Pos.up()))
                            PositionsToHighlight.add(p_Pos);
                    }
                });
            }).start();
        }
    });

    @EventHandler
    private Listener<RenderEvent> OnRenderEvent = new Listener<>(p_Event ->
    {
        if (mc.getRenderManager() == null || mc.getRenderManager().options == null)
            return;

        /// avoid ConcurrentModificationException, copy this list
        new ArrayList<BlockPos>(PositionsToHighlight).forEach(p_Pos ->
        {
            final AxisAlignedBB bb = new AxisAlignedBB(p_Pos.getX() - mc.getRenderManager().viewerPosX,
                    p_Pos.getY() - mc.getRenderManager().viewerPosY, p_Pos.getZ() - mc.getRenderManager().viewerPosZ,
                    p_Pos.getX() + 1 - mc.getRenderManager().viewerPosX,
                    p_Pos.getY() + 1 - mc.getRenderManager().viewerPosY,
                    p_Pos.getZ() + 1 - mc.getRenderManager().viewerPosZ);

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
                GL11.glEnable(GL11.GL_LINE_SMOOTH);
                GL11.glHint(GL11.GL_LINE_SMOOTH_HINT, GL11.GL_NICEST);
                GL11.glLineWidth(1.5f);

                Render(bb, Red.getValue(), Green.getValue(), Blue.getValue(),
                        Alpha.getValue());

                GL11.glDisable(GL11.GL_LINE_SMOOTH);
                GlStateManager.depthMask(true);
                GlStateManager.enableDepth();
                GlStateManager.enableTexture2D();
                GlStateManager.disableBlend();
                GlStateManager.popMatrix();
            }
        });
    });

    private void Render(final AxisAlignedBB bb, float p_Red, float p_Green, float p_Blue, float p_Alpha)
    {
        switch (Mode.getValue())
        {
            case "Flat":
                RenderGlobal.renderFilledBox(bb.minX, bb.minY, bb.minZ, bb.maxX, bb.minY, bb.maxZ, p_Red, p_Green, p_Blue,
                        p_Alpha);
                break;
            case "FlatOutline":
                RenderGlobal.drawBoundingBox(bb.minX, bb.minY, bb.minZ, bb.maxX, bb.minY, bb.maxZ, p_Red, p_Green, p_Blue,
                        p_Alpha);
                break;
            case "Full":
                RenderGlobal.drawBoundingBox(bb.minX, bb.minY, bb.minZ, bb.maxX, bb.maxY, bb.maxZ, p_Red, p_Green, p_Blue,
                        p_Alpha);
                RenderGlobal.renderFilledBox(bb.minX, bb.minY, bb.minZ, bb.maxX, bb.maxY, bb.maxZ, p_Red, p_Green, p_Blue,
                        p_Alpha);
                break;
            case "Outline":
                RenderGlobal.drawBoundingBox(bb.minX, bb.minY, bb.minZ, bb.maxX, bb.maxY, bb.maxZ, p_Red, p_Green, p_Blue,
                        p_Alpha);
                break;
            default:
                break;
        }
    }

    /// Copy from BlockFarmland
    private boolean hasWater(World worldIn, BlockPos pos)
    {
        for (BlockPos.MutableBlockPos blockpos$mutableblockpos : BlockPos.getAllInBoxMutable(pos.add(-4, 0, -4),
                pos.add(4, 1, 4)))
        {
            if (worldIn.getBlockState(blockpos$mutableblockpos).getMaterial() == Material.WATER)
            {
                return true;
            }
        }

        return false;
    }
}
