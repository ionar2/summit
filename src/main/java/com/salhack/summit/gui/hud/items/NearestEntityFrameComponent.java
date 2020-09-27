package com.salhack.summit.gui.hud.items;

import java.text.DecimalFormat;
import java.util.Comparator;

import com.salhack.summit.gui.hud.components.DraggableHudComponent;
import com.salhack.summit.managers.FriendManager;
import com.salhack.summit.module.Value;
import com.salhack.summit.util.entity.EntityUtil;
import com.salhack.summit.util.render.RenderUtil;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.EntityPigZombie;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.entity.player.EntityPlayer;

public class NearestEntityFrameComponent extends DraggableHudComponent
{
    public final Value<Boolean> Players = new Value<Boolean>("Players", new String[] {"P"}, "Displays players", true);
    public final Value<Boolean> Friends = new Value<Boolean>("Friends", new String[] {"F"}, "Displays Friends", false);
    public final Value<Boolean> Mobs = new Value<Boolean>("Mobs", new String[] {"M"}, "Displays Mobs", true);
    public final Value<Boolean> Animals = new Value<Boolean>("Animals", new String[] {"A"}, "Displays Animals", true);
    
    public NearestEntityFrameComponent()
    {
        super("NearestEntityFrame", 400, 2, 0, 0);
    }

    @Override
    public void onRender(ScaledResolution res, float p_MouseX, float p_MouseY, float p_PartialTicks)
    {
        if (mc.world == null)
            return;
        
        super.onRender(res, p_MouseX, p_MouseY, p_PartialTicks);

        //RenderUtil.drawStringWithShadow(mc.getSession().getUsername(), getX(), getY(), 0xFFFFFF);
        
        RenderUtil.drawRect(getX(), getY(), getX()+getWidth(), getY()+getHeight(), 0x990C0C0C);
        //RenderUtil.drawStringWithShadow(mc.getSession().getUsername(), getX(), getY(), 0xFFEC00);
        
        EntityLivingBase l_Entity = mc.world.loadedEntityList.stream()
                .filter(entity -> IsValidEntity(entity))
                .map(entity -> (EntityLivingBase) entity)
                .min(Comparator.comparing(c -> mc.player.getDistance(c)))
                .orElse(null);
        
        if (l_Entity == null)
            return;
        
        float l_HealthPct = ((l_Entity.getHealth()+l_Entity.getAbsorptionAmount())/l_Entity.getMaxHealth())*100.0f ;
        float l_HealthBarPct = Math.min(l_HealthPct, 100.0f);
        
        DecimalFormat l_Format = new DecimalFormat("#.#");
        
        GlStateManager.disableRescaleNormal();
        GlStateManager.setActiveTexture(OpenGlHelper.lightmapTexUnit);
        GlStateManager.disableTexture2D();
        GlStateManager.setActiveTexture(OpenGlHelper.defaultTexUnit);
        
        GuiInventory.drawEntityOnScreen((int) getX()+10, (int)getY()+30, 15, p_MouseX, p_MouseY, l_Entity);

        GlStateManager.enableRescaleNormal();
        GlStateManager.enableTexture2D();
        GlStateManager.enableBlend();

        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        
        RenderUtil.drawStringWithShadow(l_Entity.getName(), getX()+20, getY()+1, 0xFFFFFF);
        RenderUtil.drawGradientRect(getX()+20, getY()+11, getX()+20+l_HealthBarPct, getY()+22, 0x999FF365, 0x9913FF00);
        RenderUtil.drawStringWithShadow(String.format("(%s) %s / %s", l_Format.format(l_HealthPct) + "%", l_Format.format(l_Entity.getHealth()+l_Entity.getAbsorptionAmount()), l_Format.format(l_Entity.getMaxHealth())), getX()+20, getY()+11, 0xFFFFFF);
        
        this.setWidth(120);
        this.setHeight(33);
    }

    private boolean IsValidEntity(Entity p_Entity)
    {
        if (!(p_Entity instanceof EntityLivingBase))
            return false;
        
        if (p_Entity instanceof EntityPlayer)
        {
            if (p_Entity == mc.player)
                return false;
            
            if (!Players.getValue())
                return false;
            
            if (FriendManager.Get().IsFriend(p_Entity) && !Friends.getValue())
                return false;
        }
        
        if (EntityUtil.isHostileMob(p_Entity) && !Mobs.getValue() || (p_Entity instanceof EntityPigZombie && !Mobs.getValue()))
            return false;

        if (p_Entity instanceof EntityAnimal && !Animals.getValue())
            return false;
        
        return true;
    }
}
