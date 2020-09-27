package com.salhack.summit.gui.hud.items;

import com.mojang.realmsclient.gui.ChatFormatting;

import com.salhack.summit.gui.hud.components.OptionalListHudComponent;
import net.minecraft.tileentity.TileEntityChest;

/// @todo: Needs enum options

public class ChestCountComponent extends OptionalListHudComponent
{
    public ChestCountComponent()
    {
        super("ChestCount", 2, 245, 0, 0);
        setEnabled(true);
    }
    
    @Override
    public void onUpdate()
    {
        this.cornerItem.setName(new StringBuilder("Chests ").append(ChatFormatting.WHITE).append(mc.world.loadedTileEntityList.stream().filter(e -> e instanceof TileEntityChest).count()).toString());
    }
}
