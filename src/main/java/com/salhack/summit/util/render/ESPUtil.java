package com.salhack.summit.util.render;

import com.salhack.summit.events.render.RenderEvent;
import com.salhack.summit.main.Wrapper;
import com.salhack.summit.module.render.EntityESP;
import com.salhack.summit.util.Hole.HoleTypes;
import com.salhack.summit.util.entity.EntityUtil;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.client.renderer.culling.ICamera;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityEnderCrystal;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import org.lwjgl.opengl.GL11;

import java.awt.*;

public class ESPUtil
{
    public static void ColorToGL(final Color color)
    {
        GL11.glColor4f(color.getRed() / 255.0f, color.getGreen() / 255.0f, color.getBlue() / 255.0f, color.getAlpha() / 255.0f);
    }

    public static void RenderBoundingBox(final double n, final double n2, final double n3, final double n4, final int n5)
    {
        final float n6 = (n5 >> 24 & 0xFF) / 255.0f;
        final float n7 = (n5 >> 16 & 0xFF) / 255.0f;
        final float n8 = (n5 >> 8 & 0xFF) / 255.0f;
        final float n9 = (n5 & 0xFF) / 255.0f;
        GL11.glEnable(3042);
        GL11.glDisable(3553);
        GL11.glBlendFunc(770, 771);
        GL11.glEnable(2848);
        GL11.glPushMatrix();
        GL11.glColor4f(n7, n8, n9, n6);
        GL11.glBegin(7);
        GL11.glVertex2d(n, n4);
        GL11.glVertex2d(n3, n4);
        GL11.glVertex2d(n3, n2);
        GL11.glVertex2d(n, n2);
        GL11.glEnd();
        GL11.glPopMatrix();
        GL11.glEnable(3553);
        GL11.glDisable(3042);
        GL11.glDisable(2848);
    }
    
    public static boolean IsVoidHole(BlockPos blockPos, IBlockState blockState)
    {
        if (blockPos.getY() > 4 || blockPos.getY() <= 0)
            return false;

        BlockPos l_Pos = blockPos;

        for (int l_I = blockPos.getY(); l_I >= 0; --l_I)
        {
            if (Wrapper.GetMC().world.getBlockState(l_Pos).getBlock() != Blocks.AIR)
                return false;

            l_Pos = l_Pos.down();
        }

        return true;
    }

    public static HoleTypes isBlockValid(IBlockState blockState, BlockPos blockPos)
    {
        if (blockState.getBlock() != Blocks.AIR)
            return HoleTypes.None;

        if (Wrapper.GetMC().world.getBlockState(blockPos.up()).getBlock() != Blocks.AIR)
            return HoleTypes.None;

        if (Wrapper.GetMC().world.getBlockState(blockPos.up(2)).getBlock() != Blocks.AIR) // ensure the area is
                                                                             // tall enough for
                                                                             // the player
            return HoleTypes.None;

        if (Wrapper.GetMC().world.getBlockState(blockPos.down()).getBlock() == Blocks.AIR)
            return HoleTypes.None;

        final BlockPos[] touchingBlocks = new BlockPos[]
        { blockPos.north(), blockPos.south(), blockPos.east(), blockPos.west() };

        boolean l_Bedrock = true;
        boolean l_Obsidian = true;

        int validHorizontalBlocks = 0;
        for (BlockPos touching : touchingBlocks)
        {
            final IBlockState touchingState = Wrapper.GetMC().world.getBlockState(touching);
            if ((touchingState.getBlock() != Blocks.AIR) && touchingState.isFullBlock())
            {
                validHorizontalBlocks++;

                if (touchingState.getBlock() != Blocks.BEDROCK && l_Bedrock)
                    l_Bedrock = false;

                if (!l_Bedrock)
                {
                    if (touchingState.getBlock() != Blocks.OBSIDIAN && touchingState.getBlock() != Blocks.BEDROCK)
                        l_Obsidian = false;
                }
            }
        }

        if (validHorizontalBlocks < 4)
            return HoleTypes.None;

        if (l_Bedrock)
            return HoleTypes.Bedrock;
        if (l_Obsidian)
            return HoleTypes.Obsidian;

        return HoleTypes.Normal;
    }
    
    public static void Render(String p_Mode, final AxisAlignedBB bb, float p_Red, float p_Green, float p_Blue, float p_Alpha)
    {
        switch (p_Mode)
        {
            case "Flat":
                RenderGlobal.renderFilledBox(bb.minX, bb.minY, bb.minZ, bb.maxX, bb.minY, bb.maxZ, p_Red, p_Green, p_Blue, p_Alpha);
                break;
            case "FlatOutline":
                RenderGlobal.drawBoundingBox(bb.minX, bb.minY, bb.minZ, bb.maxX, bb.minY, bb.maxZ, p_Red, p_Green, p_Blue, p_Alpha);
                break;
            case "Full":
                RenderGlobal.drawBoundingBox(bb.minX, bb.minY, bb.minZ, bb.maxX, bb.maxY, bb.maxZ, p_Red, p_Green, p_Blue, p_Alpha);
                RenderGlobal.renderFilledBox(bb.minX, bb.minY, bb.minZ, bb.maxX, bb.maxY, bb.maxZ, p_Red, p_Green, p_Blue, p_Alpha);
                break;
            case "Outline":
                RenderGlobal.drawBoundingBox(bb.minX, bb.minY, bb.minZ, bb.maxX, bb.maxY, bb.maxZ, p_Red, p_Green, p_Blue, p_Alpha);
                break;
            default:
                break;
        }
    }
    
    public static void RenderOutline(RenderEvent p_Event, BlockPos p_Pos, float red, float green, float blue, float alpha)
    {
    }
}
