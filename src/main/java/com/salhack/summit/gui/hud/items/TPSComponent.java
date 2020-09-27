package com.salhack.summit.gui.hud.items;

import com.mojang.realmsclient.gui.ChatFormatting;

import com.salhack.summit.managers.TickRateManager;
import com.salhack.summit.gui.hud.components.OptionalListHudComponent;

public class TPSComponent extends OptionalListHudComponent
{
    public TPSComponent()
    {
        super("TPS", 2, 125);
        setEnabled(true);
    }

    @Override
    public void onUpdate()
    {
        this.cornerItem.setName(new StringBuilder("TPS ").append(ChatFormatting.WHITE).append(String.format("%.2f", TickRateManager.Get().getTickRate())).toString());
    }
}
