package com.salhack.summit.module.render;

import java.text.DecimalFormat;

import com.salhack.summit.events.bus.EventHandler;
import com.salhack.summit.events.bus.Listener;
import com.salhack.summit.events.render.EventRenderTooltip;
import com.salhack.summit.module.Module;
import com.salhack.summit.util.render.RenderUtil;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemElytra;
import net.minecraft.item.ItemShulkerBox;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;
import net.minecraft.util.text.translation.I18n;

public class WoWTooltips extends Module
{

    public WoWTooltips()
    {
        super("WoWTooltips", new String[] {""}, "Displays items like in WoW", "NONE", 0x3397B9, ModuleType.RENDER);
    }

    private DecimalFormat _formatter = new DecimalFormat("#");
    private float _lastWidth = 0;
    private float _lastHeight = 0;
    
    @EventHandler
    private Listener<EventRenderTooltip> OnRenderTooltip = new Listener<>(event ->
    {
        if (event.isCancelled())
            return;
            
        if (!event.getItemStack().isEmpty())
        {
            event.cancel();

            // store mouse/event coords
            int x = event.getX();
            int y = event.getY();

            // translate to mouse x, y
            GlStateManager.translate(x + 10, y - 5, 0);

            GlStateManager.disableLighting();
            GlStateManager.disableDepth();
            
            String title = event.getItemStack().getDisplayName();
            
            RenderUtil.drawRect(0, -2, _lastWidth, _lastHeight, 0xE1080E20);
            
            float prevWidth = _lastWidth;
            _lastWidth = 0;
            
            int newY = renderString(title, 3, 3, getColorFromItem(event.getItemStack()));
            
            final String itemNameDesc = getItemNameDescriptionString(event.getItemStack());
            if (itemNameDesc != null)
                newY = renderString(itemNameDesc, 3, newY, 0xFF0000);
            
            String typeString = null;
            String rightTypeString = null;

            if (event.getItemStack().getItem() instanceof ItemArmor)
            {
                ItemArmor armor = (ItemArmor) event.getItemStack().getItem();
                
                switch (armor.getEquipmentSlot())
                {
                    case CHEST:
                        typeString = "Chest";
                        break;
                    case FEET:
                        typeString = "Feet";
                        break;
                    case HEAD:
                        typeString = "Head";
                        break;
                    case LEGS:
                        typeString = "Leggings";
                        break;
                    default:
                        break;
                }
                
                switch (armor.getArmorMaterial())
                {
                    case CHAIN:
                        rightTypeString = "Chain";
                        break;
                    case DIAMOND:
                        rightTypeString = "Diamond";
                        break;
                    case GOLD:
                        rightTypeString = "Gold";
                        break;
                    case IRON:
                        rightTypeString = "Iron";
                        break;
                    case LEATHER:
                        rightTypeString = "Leather";
                        break;
                    default:
                        break;
                }
            }

            if (event.getItemStack().getItem() instanceof ItemElytra)
            {
                typeString = "Chest";
            }
            
            if (event.getItemStack().getItem() instanceof ItemSword)
            {
                typeString = "Mainhand";
                rightTypeString = "Sword";
            }
            
            if (typeString != null)
            {
                int prevY = newY;
                
                newY = renderString(typeString, 3, newY, -1);
                
                if (rightTypeString != null)
                {
                    renderString(rightTypeString, (int)(prevWidth - RenderUtil.getStringWidth(rightTypeString) - 3), prevY, -1);
                    _lastWidth = Math.max(16 * 3, prevWidth);
                }
            }
            
            if (event.getItemStack().getItem() instanceof ItemSword)
            {
                ItemSword sword = (ItemSword)event.getItemStack().getItem();
                
                int prevY = newY;
                
                newY = renderString(sword.getAttackDamage() + " - " + sword.getAttackDamage() + " Damage", 3, newY, -1);
                
                String speedString = "Speed 0.625";

                renderString(speedString, (int)(_lastWidth - RenderUtil.getStringWidth(speedString) - 3), prevY, -1);
            }
            
            for (Enchantment enchant : EnchantmentHelper.getEnchantments(event.getItemStack()).keySet())
            {
                String name = "+" + EnchantmentHelper.getEnchantmentLevel(enchant, event.getItemStack()) + " " + I18n.translateToLocal(enchant.getName());//enchant.getTranslatedName(EnchantmentHelper.getEnchantmentLevel(enchant, event.getItemStack()));
                
                if (name.contains("Vanish") || name.contains("Binding"))
                    continue;
                
                int color = -1;
                
                if (name.contains("Mending") || name.contains("Unbreaking"))
                    color = 0x00FF00;
                
                newY = renderString(name, 3, newY, color);
            }

            if (event.getItemStack().getMaxDamage() > 1)
            {
                float armorPct = ((float)(event.getItemStack().getMaxDamage()-event.getItemStack().getItemDamage()) /  (float)event.getItemStack().getMaxDamage())*100.0f;
    
                final String durability = String.format("Durability %s %s / %s", _formatter.format(armorPct) + "%", event.getItemStack().getMaxDamage()-event.getItemStack().getItemDamage(), event.getItemStack().getMaxDamage());
                
                newY = renderString(durability, 3, newY, -1);
            }
            
            GlStateManager.enableDepth();
            mc.getRenderItem().zLevel = 150.0F;
            RenderHelper.enableGUIStandardItemLighting();
            
            RenderHelper.disableStandardItemLighting();
            mc.getRenderItem().zLevel = 0.0F;
            GlStateManager.enableLighting();

            // reverse the translate
            GlStateManager.translate(-(x + 10), -(y - 5), 0);
            
            _lastHeight = newY + 1;
        }
    });
    
    private int renderString(String string, int x, int y, int color)
    {
        RenderUtil.drawStringWithShadow(string, x, y, color);
        _lastWidth = Math.max(_lastWidth, RenderUtil.getStringWidth(string) + x + 3);
        return y + 9;
    }
    
    private int getColorFromItem(ItemStack stack)
    {
        if (stack.getItem() instanceof ItemArmor)
        {
            ItemArmor armor = (ItemArmor) stack.getItem();
            
            switch (armor.getArmorMaterial())
            {
                case CHAIN:
                    return 0x0070dd;
                case DIAMOND:
                    return EnchantmentHelper.getEnchantments(stack).keySet().isEmpty() ? 0x1eff00 : 0xa335ee;
                case GOLD:
                case IRON:
                    return 0x1eff00;
                case LEATHER:
                    return 0x9d9d9d;
                default:
                    break;
            }
        }
        else if (stack.getItem().equals(Items.GOLDEN_APPLE))
        {
            if (stack.hasEffect())
                return 0xa335ee;
            
            return 0x00CDFF;
        }
        else if (stack.getItem() instanceof ItemSword)
        {
            ItemSword sword = (ItemSword)stack.getItem();
            
            final String material = sword.getToolMaterialName();
            
            if (material.equals("DIAMOND"))
                return 0xa335ee;
            if (material.equals("CHAIN"))
                return 0x0070dd;
            if (material.equals("GOLD"))
                return 0x1eff00;
            if (material.equals("IRON"))
                return 0x1eff00;
            if (material.equals("LEATHER"))
                return 0x9d9d9d;
            
            return -1;
        }
        else if (stack.getItem().equals(Items.TOTEM_OF_UNDYING))
            return 0xff8000;
        else if (stack.getItem().equals(Items.CHORUS_FRUIT))
            return 0x0070dd;
        else if (stack.getItem().equals(Items.ENDER_PEARL))
            return 0x0070dd;
        else if (stack.getItem().equals(Items.END_CRYSTAL))
            return 0xa335ee;
        else if (stack.getItem().equals(Items.EXPERIENCE_BOTTLE))
            return 0x1eff00;
        else if (stack.getItem().equals(Items.POTIONITEM))
            return 0x1eff00;
        else if (Item.getIdFromItem(stack.getItem()) == 130)
            return 0xa335ee;
        else if (stack.getItem() instanceof ItemShulkerBox)
            return 0xa335ee;
        
        return -1;
    }
    
    private final String getItemNameDescriptionString(ItemStack stack)
    {
        String result = "";
        
        for (Enchantment enchant : EnchantmentHelper.getEnchantments(stack).keySet())
        {
            if (enchant == null)
                continue;
            
            String name = enchant.getTranslatedName(EnchantmentHelper.getEnchantmentLevel(enchant, stack));
            
            if (name.contains("Vanish"))
                result += "Vanishing ";
            else if (name.contains("Binding"))
                result += "Binding ";
        }
        
        if (stack.getItem().equals(Items.GOLDEN_APPLE) && stack.hasEffect())
            return "God";
        
        return result.isEmpty() ? null : result;
    }
}
