package com.salhack.summit.gui.hud.items;

import com.mojang.realmsclient.gui.ChatFormatting;

import com.salhack.summit.gui.hud.components.OptionalListHudComponent;
import com.salhack.summit.util.entity.PlayerUtil;
import net.minecraft.init.Items;

public class TotemCountComponent extends OptionalListHudComponent
{
    public TotemCountComponent()
    {
        super("TotemCount", 2, 215);
    }

    @Override
    public void onUpdate()
    {
        this.cornerItem.setName(new StringBuilder("Totems: ").append(ChatFormatting.WHITE).append(PlayerUtil.GetItemCount(Items.TOTEM_OF_UNDYING)).toString());
    }
}
