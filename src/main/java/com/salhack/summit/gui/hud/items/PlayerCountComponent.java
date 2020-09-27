package com.salhack.summit.gui.hud.items;

import com.mojang.realmsclient.gui.ChatFormatting;

import com.salhack.summit.gui.hud.components.OptionalListHudComponent;

public class PlayerCountComponent extends OptionalListHudComponent
{
    public PlayerCountComponent()
    {
        super("PlayerCount", 2, 185);
    }

    @Override
    public void onUpdate()
    {
        this.cornerItem.setName(new StringBuilder("Players ").append(ChatFormatting.WHITE).append(mc.player.connection.getPlayerInfoMap().size()).toString());
    }

}
