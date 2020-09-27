package com.salhack.summit.gui.hud.items;

import com.mojang.realmsclient.gui.ChatFormatting;

import com.salhack.summit.gui.hud.components.OptionalListHudComponent;
import net.minecraft.client.network.NetworkPlayerInfo;

public class PingComponent extends OptionalListHudComponent
{
    public PingComponent()
    {
        super("Ping", 2, 230, 100, 100);
        setEnabled(true);
    }

    @Override
    public void onUpdate()
    {
        if (mc.world == null || mc.player == null || mc.player.getUniqueID() == null)
            return;

        final NetworkPlayerInfo playerInfo = mc.getConnection().getPlayerInfo(mc.player.getUniqueID());

        if (playerInfo == null)
            return;

        cornerItem.setName(new StringBuilder("Ping ").append(ChatFormatting.WHITE).append(playerInfo.getResponseTime()).append("ms").toString());
    }
}
