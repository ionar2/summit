package com.salhack.summit.module.render;

import com.mojang.realmsclient.gui.ChatFormatting;
import com.salhack.summit.events.bus.EventHandler;
import com.salhack.summit.events.bus.Listener;
import com.salhack.summit.events.render.EventRenderEntityName;
import com.salhack.summit.events.render.RenderEvent;
import com.salhack.summit.managers.FriendManager;
import com.salhack.summit.module.Module;
import com.salhack.summit.module.Value;
import com.salhack.summit.util.ColourUtilities;
import com.salhack.summit.util.MathUtil;
import com.salhack.summit.util.entity.EntityUtil;
import com.salhack.summit.util.render.RenderUtil;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.Vec3d;

import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class Nametags extends Module
{
    public final Value<Boolean> Armor = new Value<Boolean>("Armor", new String[]
    { "" }, "", true);
    public final Value<Boolean> Durability = new Value<Boolean>("Durability", new String[]
    { "" }, "", true);
    public final Value<Boolean> ItemName = new Value<Boolean>("ItemName", new String[] 
    { "" }, "", true);
    public final Value<Boolean> Health = new Value<Boolean>("Health", new String[]
    { "" }, "", true);
    public final Value<Boolean> Invisibles = new Value<Boolean>("Invisibles", new String[]
    { "" }, "", false);
    public final Value<Boolean> EntityID = new Value<Boolean>("EntityID", new String[]
    { "" }, "", false);
    public final Value<Boolean> GameMode = new Value<Boolean>("GameMode", new String[]
    { "" }, "", false);
    public final Value<Boolean> Ping = new Value<Boolean>("Ping", new String[]
    { "" }, "", true);
    public final Value<Float> Scaling = new Value<Float>("Scaling", new String[] {""}, "Scaling", 3f, 1f, 10f, 1f);

    public Nametags()
    {
        super("Nametags", new String[]
        { "Nametag" }, "Improves nametags of players around you", "NONE", 0x5B4DD1, ModuleType.RENDER);
    }

    @EventHandler
    private Listener<RenderEvent> OnRenderGameOverlay = new Listener<>(event ->
    {
        if (mc.world == null || mc.renderEngine == null || mc.getRenderManager() == null
                || mc.getRenderManager().options == null)
            return;

        List<EntityPlayer> players = new ArrayList<>();

        mc.world.playerEntities.stream().filter(entity -> entity instanceof EntityPlayer && EntityUtil.isLiving(entity)
                && entity != mc.getRenderViewEntity()).forEach(e ->
                {
                    RenderUtil.camera.setPosition(mc.getRenderViewEntity().posX, mc.getRenderViewEntity().posY, mc.getRenderViewEntity().posZ);
                    
                    if (RenderUtil.camera.isBoundingBoxInFrustum(e.getEntityBoundingBox()))
                        players.add(e);
                });

        // Sort by reverse distance (render close after far.)
        players.sort((p1, p2) -> Double.compare(p2.getDistance(mc.getRenderViewEntity()),
                p1.getDistance(mc.getRenderViewEntity())));

        for (EntityPlayer player : players)
        {
            
            final Entity entity2 = mc.getRenderViewEntity();

            Vec3d pos = MathUtil.interpolateEntityClose(player, event.getPartialTicks());
            
            double n = pos.x;
            double distance = pos.y + 0.65;
            double n2 = pos.z;
            
            final double n3 = distance + (player.isSneaking() ? 0.0 : 0.08f);
            
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

            String nameTag = generateNameTag(player);

            float width = RenderUtil.getStringWidth(nameTag) / 2;
            float height = mc.fontRenderer.FONT_HEIGHT;

            GlStateManager.enableBlend();
            RenderUtil.drawRect(-width - 1, -(height + 1), width + 2, 2, 0x5F0A0A0A);
            GlStateManager.disableBlend();
            RenderUtil.drawStringWithShadow(nameTag, -width+1, -height+3, getColorByHealth(player.getMaxHealth(), player.getHealth()));

            GlStateManager.pushMatrix();

            final Iterator<ItemStack> items = player.getArmorInventoryList().iterator();
            final ArrayList<ItemStack> stacks = new ArrayList<>();

            stacks.add(player.getHeldItemOffhand());

            while (items.hasNext())
            {
                final ItemStack stack = items.next();
                if (!stack.isEmpty())
                {
                    stacks.add(stack);
                }
            }
            stacks.add(player.getHeldItemMainhand());

            Collections.reverse(stacks);

            int x = (int) -width;
            int y = -32;
            int z = 0;

            for (ItemStack stack : stacks)
            {
                RenderItemStack(stack, x, y, z);
                RenderItemEnchantments(stack, x, -62);
                x += 16;
            }

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

    private String GetEnchantName(final Enchantment enchantment, final int n)
    {
        if (enchantment.getTranslatedName(n).contains("Vanish"))
            return ChatFormatting.RED + "Van";
        if (enchantment.getTranslatedName(n).contains("Bind"))
            return ChatFormatting.RED + "Bind";

        String substring = enchantment.getTranslatedName(n);
        final int n2 = (n > 1) ? 2 : 3;
        if (substring.length() > n2)
        {
            substring = substring.substring(0, n2);
        }
        final StringBuilder sb = new StringBuilder();
        final String s = substring;
        final int n3 = 0;
        String s2 = sb.insert(n3, s.substring(n3, 1).toUpperCase()).append(substring.substring(1)).toString();
        if (n > 1)
        {
            s2 = new StringBuilder().insert(0, s2).append(n).toString();
        }
        return s2;
    }

    private void RenderItemEnchantments(final ItemStack itemStack, final int n, int n2)
    {
        GlStateManager.scale(0.5f, 0.5f, 0.5f);
        final int n3 = -1;
        final Iterator<Enchantment> iterator2;
        Iterator<Enchantment> iterator = iterator2 = EnchantmentHelper.getEnchantments(itemStack).keySet().iterator();
        while (iterator.hasNext())
        {
            final Enchantment enchantment;
            if ((enchantment = iterator2.next()) == null)
            {
                iterator = iterator2;
            } else
            {

                RenderUtil.drawStringWithShadow(
                        GetEnchantName(enchantment, EnchantmentHelper.getEnchantmentLevel(enchantment, itemStack)),
                        (float) (n * 2), (float) n2, n3);

                n2 += 8;
                iterator = iterator2;
            }
        }
        if (itemStack.getItem().equals(Items.GOLDEN_APPLE) && itemStack.hasEffect())
        {
            RenderUtil.drawStringWithShadow(ChatFormatting.DARK_RED + "God", (float) (n * 2), (float) n2, -1);
        }
        GlStateManager.scale(2.0f, 2.0f, 2.0f);
    }

    private void RenderItemDamage(final ItemStack itemStack, final int n, int n2)
    {
        final float n3 = ((float) (itemStack.getMaxDamage() - itemStack.getItemDamage())
                / (float) itemStack.getMaxDamage()) * 100.0f;

        int color = 0x1FFF00;

        if (n3 > 30 && n3 < 70)
            color = 0xFFFF00;
        else if (n3 <= 30)
            color = 0xFF0000;

        GlStateManager.scale(0.5f, 0.5f, 0.5f);
        GlStateManager.disableDepth();

        RenderUtil.drawStringWithShadow(
                new StringBuilder().insert(0, String.valueOf((int) (n3))).append('%').toString(), (float) (n * 2),
                (float) n2, color);

        GlStateManager.enableDepth();
        GlStateManager.scale(2.0f, 2.0f, 2.0f);
    }

    private void RenderItemStack(final ItemStack itemStack, final int n, final int n2, final int n3)
    {
        GlStateManager.pushMatrix();
        GlStateManager.depthMask(true);
        GlStateManager.clear(256);
        RenderHelper.enableStandardItemLighting();
        mc.getRenderItem().zLevel = -150.0f;
        GlStateManager.disableAlpha();
        GlStateManager.enableDepth();
        GlStateManager.disableCull();
        final int n4 = (n3 > 4) ? ((n3 - 4) * 8 / 2) : 0;
        mc.getRenderItem().renderItemAndEffectIntoGUI(itemStack, n, n2 + n4);
        mc.getRenderItem().renderItemOverlays(mc.fontRenderer, itemStack, n, n2 + n4);
        mc.getRenderItem().zLevel = 0.0f;
        RenderHelper.disableStandardItemLighting();
        GlStateManager.enableCull();
        GlStateManager.enableAlpha();
        final float n5 = 0.5f;
        final float n6 = 0.5f;
        GlStateManager.scale(n6, n5, n6);
        GlStateManager.disableDepth();
        if (itemStack.getMaxDamage() > 1)
            this.RenderItemDamage(itemStack, n * 2, n2 - 100);
        GlStateManager.enableDepth();
        final float n7 = 2.0f;
        final int n8 = 2;
        GlStateManager.scale((float) n8, n7, (float) n8);
        GlStateManager.popMatrix();
    }

    @EventHandler
    private Listener<EventRenderEntityName> OnRenderEntityName = new Listener<>(event ->
    {
        event.cancel();
    });

    private int getColorByHealth(float maxHealth, float health)
    {
        Color green = new Color(72, 255, 94);
        Color yellow = new Color(255, 250, 57);
        Color red = new Color(255, 35, 40);

        float middleHealth = maxHealth / 2;

        if (health <= middleHealth)
        {
            return ColourUtilities.blend(yellow, red, (health / middleHealth)).getRGB();
        } else if (health <= (middleHealth * 2))
        {
            return ColourUtilities.blend(green, yellow, ((health - middleHealth) / middleHealth)).getRGB();
        }
        return green.getRGB();
    }
    
    private String generateNameTag(EntityPlayer player)
    {
        String string = player.getName();
        
        if (FriendManager.Get().IsFriend(player))
            string = ChatFormatting.AQUA + string + ChatFormatting.RESET;
        else
        {
            if (player.isSneaking())
                string = ChatFormatting.GOLD + string + ChatFormatting.RESET;
            else
                string = ChatFormatting.WHITE + string + ChatFormatting.RESET;
        }

        int responseTime = -1;
        try
        {
            responseTime = (int) MathUtil.clamp(mc.getConnection().getPlayerInfo(player.getUniqueID()).getResponseTime(), 0, 10000);
        }
        catch (NullPointerException np)
        {
        }
        
        if (responseTime > 200)
            string += ChatFormatting.RED;
        else if (responseTime <= 200 && responseTime >= 100)
            string += ChatFormatting.YELLOW;
        else if (responseTime < 100)
            string += ChatFormatting.GREEN;
        
        string += "  " + responseTime + "ms" + ChatFormatting.RESET + "  ";
        
        return string + Math.floor(player.getHealth() + player.getAbsorptionAmount());
    }
}
