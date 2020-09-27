package com.salhack.summit.gui.hud.items;

import com.mojang.realmsclient.gui.ChatFormatting;

import com.salhack.summit.gui.hud.components.OptionalListHudComponent;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;
import net.minecraft.item.ItemTool;

public class TrueDurabilityComponent extends OptionalListHudComponent
{
    public TrueDurabilityComponent()
    {
        super("TrueDurability", 2, 260);
    }

    @Override
    public void onUpdate()
    {
        ItemStack l_Stack = mc.player.getHeldItemMainhand();

        if (!l_Stack.isEmpty() && (l_Stack.getItem() instanceof ItemTool || l_Stack.getItem() instanceof ItemArmor || l_Stack.getItem() instanceof ItemSword))
        {
            this.cornerItem.setName(new StringBuilder("Durability: ").append(ChatFormatting.GREEN).append(l_Stack.getMaxDamage()-l_Stack.getItemDamage()).toString());
        }
    }
}
