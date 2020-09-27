package com.salhack.summit.gui.hud.items;

import com.mojang.realmsclient.gui.ChatFormatting;
import com.salhack.summit.gui.hud.components.OptionalListHudComponent;
import com.salhack.summit.main.SummitStatic;

import java.util.concurrent.TimeUnit;

public class StopwatchComponent extends OptionalListHudComponent
{
    public StopwatchComponent()
    {
        super("Stopwatch", 2, 275, 0, 0);
    }

    @Override
    public void onUpdate()
    {
        this.currentSide.setName(new StringBuilder("Seconds ").append(ChatFormatting.WHITE).append(TimeUnit.MILLISECONDS.toSeconds(SummitStatic.STOPWATCH.ElapsedMS - SummitStatic.STOPWATCH.StartMS)).toString());
    }
}
