package com.salhack.summit.gui.hud.items;

import java.text.DecimalFormat;

import com.salhack.summit.gui.hud.components.DraggableHudComponent;
import com.salhack.summit.util.render.RenderUtil;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;

public class PlayerFrameComponent extends DraggableHudComponent
{
    public PlayerFrameComponent()
    {
        super("PlayerFrame", 200, 2, 100, 100);
    }

    @Override
    public void onRender(ScaledResolution res, float mouseX, float mouseY, float partialTicks)
    {
        if (mc.world == null)
            return;
        
        super.onRender(res, mouseX, mouseY, partialTicks);
        
        RenderUtil.drawRect(getX(), getY(), getX()+getWidth(), getY()+getHeight(), 0x990C0C0C);
        //RenderUtil.drawStringWithShadow(mc.getSession().getUsername(), getX(), getY(), 0xFFEC00);
        
        float l_HealthPct = ((mc.player.getHealth()+mc.player.getAbsorptionAmount())/mc.player.getMaxHealth())*100.0f ;
        float l_HealthBarPct = Math.min(l_HealthPct, 100.0f);
        
        float l_HungerPct = (((float)mc.player.getFoodStats().getFoodLevel()+ mc.player.getFoodStats().getSaturationLevel())/20)*100.0f ;
        float l_HungerBarPct = Math.min(l_HungerPct, 100.0f);
        
        DecimalFormat l_Format = new DecimalFormat("#.#");
        
        GlStateManager.disableRescaleNormal();
        GlStateManager.setActiveTexture(OpenGlHelper.lightmapTexUnit);
        GlStateManager.disableTexture2D();
        GlStateManager.setActiveTexture(OpenGlHelper.defaultTexUnit);
        
        GuiInventory.drawEntityOnScreen((int) getX()+10, (int)getY()+30, 15, mouseX, mouseY, mc.player);

        GlStateManager.enableRescaleNormal();
        GlStateManager.enableTexture2D();
        GlStateManager.enableBlend();

        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        
        RenderUtil.drawStringWithShadow(mc.getSession().getUsername(), getX()+20, getY()+4, 0xFFFFFF);
        RenderUtil.drawGradientRect(getX()+20, getY()+11, getX()+20+l_HealthBarPct, getY()+22, 0x999FF365, 0x9913FF00);
        RenderUtil.drawGradientRect(getX()+20, getY()+22, getX()+20+l_HungerBarPct, getY()+33, 0x99F9AC05, 0x99F9AC05);
        RenderUtil.drawStringWithShadow(String.format("(%s) %s / %s", l_Format.format(l_HealthPct) + "%", l_Format.format(mc.player.getHealth()+mc.player.getAbsorptionAmount()), l_Format.format(mc.player.getMaxHealth())), getX()+20, getY()+13, 0xFFFFFF);
        RenderUtil.drawStringWithShadow(String.format("(%s) %s / %s", l_Format.format(l_HungerPct) + "%", l_Format.format(mc.player.getFoodStats().getFoodLevel()+ mc.player.getFoodStats().getSaturationLevel()), "20"), getX()+20, getY()+24, 0xFFFFFF);
        
        this.setWidth(120);
        this.setHeight(33);
    }

}
