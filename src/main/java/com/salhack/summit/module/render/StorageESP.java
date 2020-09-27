package com.salhack.summit.module.render;

import com.salhack.summit.module.Module;
import com.salhack.summit.module.Value;
import com.salhack.summit.util.render.RenderUtil;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.*;
import net.minecraft.util.math.AxisAlignedBB;

import org.lwjgl.opengl.GL11;

import java.awt.*;

// credit: https://github.com/SerenityEnterprises/SerenityCE/blob/b54889f1b74c8810dec9af50699335daa7a015e4/src/main/java/host/serenity/serenity/modules/render/StorageESP.java
public class StorageESP extends Module
{
    // These are the colours in Energetic that were chosen to look like the specific
    // TileEntity. Feel free to change any of them.
    private Color chestColor = new Color(255, 252, 99);
    private Color enderChestColor = new Color(166, 0, 238);
    private Color dispenserColor = new Color(192, 192, 192);
    private Color furnaceColor = new Color(192, 192, 192);
    private Color hopperColor = new Color(167, 167, 167);
    private Color shulkerColor = new Color(255, 0, 151);

    private Color genericColor = new Color(30, 255, 40);

    public final Value<Boolean> chest = new Value<Boolean>("Chests", new String[]
    { "S" }, "Highlights Chests", true);
    public final Value<Boolean> trappedChest = new Value<Boolean>("TrappedChests", new String[]
    { "S" }, "Highlights Chests", true);
    public final Value<Boolean> enderChest = new Value<Boolean>("EnderChests", new String[]
    { "S" }, "Highlights EnderChests", true);
    public final Value<Boolean> dispenser = new Value<Boolean>("Dispenser", new String[]
    { "S" }, "Highlights Chests", true);
    public final Value<Boolean> dropper = new Value<Boolean>("Dropper", new String[]
    { "S" }, "Highlights Chests", true);
    public final Value<Boolean> hopper = new Value<Boolean>("Hopper", new String[]
    { "S" }, "Highlights Chests", true);
    public final Value<Boolean> furnace = new Value<Boolean>("Furnace", new String[]
    { "S" }, "Highlights Chests", true);
    public final Value<Boolean> Shulkers = new Value<Boolean>("Shulkers", new String[]
    { "S" }, "Highlights Shulkers", true);
    public final Value<Float> Width = new Value<Float>("Width", new String[] { "Width" }, "Highlights Width", 3.0f, 0.0f, 10.0f, 1.0f);

    public StorageESP()
    {
        super("StorageESP", new String[]
        { "" }, "Highlights different kind of storages", "NONE", -1, ModuleType.RENDER);
    }

    public void render(TileEntitySpecialRenderer<TileEntity> tileentityspecialrenderer, TileEntity tileEntityIn, double x, double y, double z, float partialTicks, int destroyStage,
            float p_192854_10_)
    {
        final AxisAlignedBB bb = new AxisAlignedBB(tileEntityIn.getPos().getX() - mc.getRenderManager().viewerPosX,
                tileEntityIn.getPos().getY() - mc.getRenderManager().viewerPosY,
                tileEntityIn.getPos().getZ() - mc.getRenderManager().viewerPosZ,
                tileEntityIn.getPos().getX() + 1 - mc.getRenderManager().viewerPosX,
                tileEntityIn.getPos().getY() + 1 - mc.getRenderManager().viewerPosY,
                tileEntityIn.getPos().getZ() + 1 - mc.getRenderManager().viewerPosZ);
        
        RenderUtil.camera.setPosition(mc.getRenderViewEntity().posX, mc.getRenderViewEntity().posY,
                mc.getRenderViewEntity().posZ);

        if (!RenderUtil.camera.isBoundingBoxInFrustum(new AxisAlignedBB(bb.minX + mc.getRenderManager().viewerPosX,
                bb.minY + mc.getRenderManager().viewerPosY, bb.minZ + mc.getRenderManager().viewerPosZ,
                bb.maxX + mc.getRenderManager().viewerPosX, bb.maxY + mc.getRenderManager().viewerPosY,
                bb.maxZ + mc.getRenderManager().viewerPosZ)))
            return;
        
        if ((tileEntityIn instanceof TileEntityChest && chest.getValue())
                || (tileEntityIn instanceof TileEntityShulkerBox && Shulkers.getValue())
                || (tileEntityIn instanceof TileEntityEnderChest && enderChest.getValue())
                || (tileEntityIn instanceof TileEntityChest && chest.getValue())
                || (tileEntityIn instanceof TileEntityDispenser && dispenser.getValue())
                || (tileEntityIn instanceof TileEntityDropper && dropper.getValue())
                || (tileEntityIn instanceof TileEntityHopper && hopper.getValue())
                || (tileEntityIn instanceof TileEntityFurnace && furnace.getValue())
                )
        {
            Color n = getColor(tileEntityIn);
            
            GlStateManager.pushMatrix();
            RenderUtil.setColor(n);
            tileentityspecialrenderer.render(tileEntityIn, x, y, z, partialTicks, destroyStage, p_192854_10_);
            RenderUtil.renderOne(Width.getValue());
            tileentityspecialrenderer.render(tileEntityIn, x, y, z, partialTicks, destroyStage, p_192854_10_);
            RenderUtil.renderTwo();
            tileentityspecialrenderer.render(tileEntityIn, x, y, z, partialTicks, destroyStage, p_192854_10_);
            RenderUtil.renderThree();
            RenderUtil.renderFour();
            RenderUtil.setColor(n);
            tileentityspecialrenderer.render(tileEntityIn, x, y, z, partialTicks, destroyStage, p_192854_10_);
            RenderUtil.renderFive();
            RenderUtil.setColor(Color.WHITE);
            GL11.glColor4f(1f, 1f, 1f, 1f);
            GlStateManager.popMatrix();
        }
    }
    
    private Color getColor(TileEntity en)
    {
        if (en instanceof TileEntityShulkerBox)
            return shulkerColor;
        if (en instanceof TileEntityChest)
            return chestColor;
        if (en instanceof TileEntityEnderChest)
            return enderChestColor;
        if (en instanceof TileEntityFurnace)
            return furnaceColor;
        if (en instanceof TileEntityHopper)
            return hopperColor;
        if (en instanceof TileEntityDispenser)
            return dispenserColor;
        if (en instanceof TileEntityDropper)
            return dispenserColor;
        
        return genericColor;
    }
}
