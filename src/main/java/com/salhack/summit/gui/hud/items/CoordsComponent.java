package com.salhack.summit.gui.hud.items;

import java.text.DecimalFormat;

import com.mojang.realmsclient.gui.ChatFormatting;

import com.salhack.summit.gui.hud.components.OptionalListHudComponent;
import com.salhack.summit.managers.HudManager;
import com.salhack.summit.module.Value;

public class CoordsComponent extends OptionalListHudComponent
{
    public final Value<Boolean> NetherCoords = new Value<Boolean>("NetherCoords", new String[]
    { "NC" }, "Displays nether coords.", true);
    public final Value<Modes> Mode = new Value<Modes>("Mode", new String[]
    { "Mode" }, "Mode of displaying coordinates", Modes.Inline);

    public enum Modes
    {
        Inline, NextLine,
    }
    
    final DecimalFormat Formatter = new DecimalFormat("#.#");

    public CoordsComponent()
    {
        super("Coords", 2, 245, 100, 100);
        setCurrentCornerList(HudManager.Get().GetModList("BottomLeft"));
        setEnabled(true);
    }

    public String format(double p_Input)
    {
        String l_Result = Formatter.format(p_Input);

        if (!l_Result.contains("."))
            l_Result += ".0";

        return l_Result;
    }

    @Override
    public void onUpdate()
    {
        StringBuilder builder = new StringBuilder();
        builder = new StringBuilder("XYZ ").append("(").append(ChatFormatting.WHITE).append(format(mc.player.posX)).append(", ")
                .append(ChatFormatting.WHITE).append(format(mc.player.posY)).append(", ")
                .append(ChatFormatting.WHITE).append(format(mc.player.posZ)).append(ChatFormatting.RESET).append(") ");

        boolean shouldAppend = mc.player.dimension != 1 && NetherCoords.getValue();
        
        double x = mc.player.dimension == 0 ? mc.player.posX / 8 : mc.player.posX * 8;
        double y = mc.player.posY;
        double z = mc.player.dimension == 0 ? mc.player.posZ / 8 : mc.player.posZ * 8;
        
        if (shouldAppend)
        {
            builder.append("[").append(ChatFormatting.WHITE).append(format(x)).append(", ").append(format(y)).append(", ").append(format(z)).append(ChatFormatting.RESET).append("]");
        }
        
        cornerItem.setName(builder.toString());
    }
}
