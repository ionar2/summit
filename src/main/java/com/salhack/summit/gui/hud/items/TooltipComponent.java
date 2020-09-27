package com.salhack.summit.gui.hud.items;

import com.salhack.summit.gui.hud.components.DraggableHudComponent;

public final class TooltipComponent extends DraggableHudComponent
{
    public TooltipComponent()
    {
        super("Tooltip", 700, 600, 0, 0);
    }
    
    /*private EntityPlayer currentTarget = null;
    private static ItemStack Totem = new ItemStack(Items.TOTEM_OF_UNDYING);
    private static ItemStack Crystal = new ItemStack(Items.END_CRYSTAL);
    private static ItemStack Gapple = new ItemStack(Items.GOLDEN_APPLE, 1, 1);
    private static ItemStack Strength = new ItemStack(Items.POTIONITEM);
    
    @Override
    public void render(int mouseX, int mouseY, float partialTicks)
    {
        super.render(mouseX, mouseY, partialTicks);

        if (currentTarget != null)
        { 
            GlStateManager.pushMatrix();
            RenderUtil.drawRect(GetX(), GetY(), GetX() + GetWidth(), GetY() + GetHeight(), 0x75101010);

            GuiInventory.drawEntityOnScreen((int)GetX()+30, (int)GetY()+90, 40, -283, -3, currentTarget);
            
            StringBuilder builder = new StringBuilder("Health: ").append(MathHelper.floor(currentTarget.getHealth() + currentTarget.getAbsorptionAmount()));
            
            RenderUtil.drawStringWithShadow(currentTarget.getName(), GetX() + 2, GetY() + 95, -1);
            RenderUtil.drawStringWithShadow(builder.toString(), GetX() + 2, GetY() + 105, -1);
            RenderUtil.drawStringWithShadow("Matched", GetX() + 2, GetY() + 115, 0xFFE400);

            int responseTime = -1;
            try
            {
                responseTime = (int) MathUtil.clamp(mc.getConnection().getPlayerInfo(currentTarget.getUniqueID()).getResponseTime(), 0, 10000);
            }
            catch (Exception e)
            {
                
            }

            builder.setLength(0);
            builder.append("Ping: ").append(responseTime).append(" ms");
            RenderUtil.drawStringWithShadow(builder.toString(), GetX() + 2, GetY() + 125, -1);
            builder.setLength(0);
            builder.append("Distance: ").append(Math.floor(currentTarget.getDistance(mc.player))).append(" m");
            RenderUtil.drawStringWithShadow(builder.toString(), GetX() + 2, GetY() + 135, -1);

            final Iterator<ItemStack> items = currentTarget.getArmorInventoryList().iterator();
            final ArrayList<ItemStack> stacks = new ArrayList<>();

            while (items.hasNext())
            {
                final ItemStack stack = items.next();
                if (!stack.isEmpty())
                {
                    stacks.add(stack);
                }
            }
            
            Collections.reverse(stacks);

            stacks.add(currentTarget.getHeldItemMainhand());
            stacks.add(currentTarget.getHeldItemOffhand());
            
            int currY = 12;
            
            for (ItemStack stack : stacks)
            {
                if (stack.isEmpty())
                    continue;

                GlStateManager.pushMatrix();
                mc.getRenderItem().renderItemAndEffectIntoGUI(stack, (int)GetX() + 70, (int)GetY() + currY);
                mc.getRenderItem().renderItemOverlays(mc.fontRenderer, stack, (int)GetX() + 70, (int)GetY() + currY);
                GlStateManager.popMatrix();
                
                if (!(stack.getItem() instanceof ItemArmor))
                {
                    RenderUtil.drawStringWithShadow(stack.getDisplayName(), GetX() + 88, GetY() + currY, -1);
                    currY += 11;
                }
                
                RenderUtil.drawStringWithShadow(getItemEnchants(stack), GetX() + 88, GetY() + currY, -1);
                currY += 22;
            }
            
            Totem.setCount(69);
            Crystal.setCount(69);
            Gapple.setCount(69);
            Strength.setCount(69);
            GlStateManager.pushMatrix();
            mc.getRenderItem().renderItemAndEffectIntoGUI(Totem, (int)GetX() + (int)GetWidth() - 20, (int)GetY());
            mc.getRenderItem().renderItemOverlays(mc.fontRenderer, Totem, (int)GetX() + (int)GetWidth() - 20, (int)GetY());
            mc.getRenderItem().renderItemAndEffectIntoGUI(Crystal, (int)GetX() + (int)GetWidth() - 20, (int)GetY() + 20);
            mc.getRenderItem().renderItemOverlays(mc.fontRenderer, Crystal, (int)GetX() + (int)GetWidth() - 20, (int)GetY() + 20);
            mc.getRenderItem().renderItemAndEffectIntoGUI(Gapple, (int)GetX() + (int)GetWidth() - 20, (int)GetY() + 40);
            mc.getRenderItem().renderItemOverlays(mc.fontRenderer, Gapple, (int)GetX() + (int)GetWidth() - 20, (int)GetY() + 40);
            mc.getRenderItem().renderItemAndEffectIntoGUI(Strength, (int)GetX() + (int)GetWidth() - 20, (int)GetY() + 60);
            mc.getRenderItem().renderItemOverlays(mc.fontRenderer, Strength, (int)GetX() + (int)GetWidth() - 20, (int)GetY() + 60);
            GlStateManager.popMatrix();

            SetWidth(260);
            SetHeight(150);
            GlStateManager.pushMatrix();
        }
    }
    
    private String getItemEnchants(ItemStack stack)
    {
        StringBuilder builder = new StringBuilder();
        boolean hasVanishing = false;
        boolean hasBinding = false;
        
        int enchants = 0;

        for (Enchantment enchant : EnchantmentHelper.getEnchantments(stack).keySet())
        {
            if (enchant == null)
                continue;

            final String name = I18n.translateToLocal(enchant.getName());
            
            if (name.contains("Vanishing"))
            {
                hasVanishing = true;
                continue;
            }
            
            if (name.contains("Binding"))
            {
                hasBinding = true;
                continue;
            }
            
            builder.append(name.substring(0, 2).toLowerCase()).append(" ").append(EnchantmentHelper.getEnchantmentLevel(enchant, stack)).append(" ");
            if (++enchants >= 7)
                break;
        }
        
        if (hasVanishing)
            builder.append(ChatFormatting.RED).append("cv");
        if (hasBinding)
            builder.append(ChatFormatting.RED).append("cb");
        
        return builder.toString();
    }
    
    @EventHandler
    private Listener<EventPlayerUpdate> onPlayerUpdate = new Listener<>(event ->
    {
        EntityPlayer target = null;
        float lastDist = 100f;
        
        for (EntityPlayer player : mc.world.playerEntities)
        {
            if (player instanceof EntityPlayerSP || FriendManager.Get().IsFriend(player))
                continue;
            
            float dist = mc.getRenderViewEntity().getDistance(player);
            
            if (dist < lastDist)
            {
                target = player;
                lastDist = dist;
            }
        }
        
        currentTarget = target;
    });*/
}
