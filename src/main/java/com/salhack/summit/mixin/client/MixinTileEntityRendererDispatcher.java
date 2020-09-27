package com.salhack.summit.mixin.client;

import com.salhack.summit.main.SummitStatic;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.crash.CrashReport;
import net.minecraft.crash.CrashReportCategory;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ReportedException;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(TileEntityRendererDispatcher.class)
public class MixinTileEntityRendererDispatcher
{
    @Shadow
    private net.minecraft.client.renderer.Tessellator batchBuffer;
    @Shadow
    private boolean drawingBatch;
    
    @Overwrite
    public void render(TileEntity tileEntityIn, double x, double y, double z, float partialTicks, int destroyStage, float p_192854_10_)
    {
        TileEntitySpecialRenderer<TileEntity> tileentityspecialrenderer = ((TileEntityRendererDispatcher)(Object)this).<TileEntity>getRenderer(tileEntityIn);

        if (tileentityspecialrenderer != null)
        {
            try
            {
                if (SummitStatic.STORAGEESP != null && SummitStatic.STORAGEESP.isEnabled())
                    SummitStatic.STORAGEESP.render(tileentityspecialrenderer, tileEntityIn, x, y, z, partialTicks, destroyStage, p_192854_10_);
                
                if (drawingBatch && tileEntityIn.hasFastRenderer())
                    tileentityspecialrenderer.renderTileEntityFast(tileEntityIn, x, y, z, partialTicks, destroyStage, p_192854_10_, batchBuffer.getBuffer());
                else
                    tileentityspecialrenderer.render(tileEntityIn, x, y, z, partialTicks, destroyStage, p_192854_10_);
            }
            catch (Throwable throwable)
            {
                CrashReport crashreport = CrashReport.makeCrashReport(throwable, "Rendering Block Entity");
                CrashReportCategory crashreportcategory = crashreport.makeCategory("Block Entity Details");
                tileEntityIn.addInfoToCrashReport(crashreportcategory);
                throw new ReportedException(crashreport);
            }
        }
    }
}
