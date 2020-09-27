package com.salhack.summit.gui.hud.items;

import com.mojang.realmsclient.gui.ChatFormatting;

import com.salhack.summit.gui.hud.components.OptionalListHudComponent;
import com.salhack.summit.managers.HudManager;
import net.minecraft.client.Minecraft;
import net.minecraft.util.math.MathHelper;

public class DirectionComponent extends OptionalListHudComponent
{
    public DirectionComponent()
    {
        super("Direction", 2, 155);
        setCurrentCornerList(HudManager.Get().GetModList("BottomLeft"));
        setEnabled(true);
    }

    @Override
    public void onUpdate()
    {
        this.cornerItem.setName(new StringBuilder(getFacing()).append(" ").append(ChatFormatting.GRAY).append(getTowards()).toString());
    }

    private String getFacing()
    {
        switch (MathHelper.floor((double) (Minecraft.getMinecraft().player.rotationYaw * 8.0F / 360.0F) + 0.5D) & 7)
        {
            case 0:
                return "South";
            case 1:
                return "South West";
            case 2:
                return "West";
            case 3:
                return "North West";
            case 4:
                return "North";
            case 5:
                return "North East";
            case 6:
                return "East";
            case 7:
                return "South East";
        }
        return "Invalid";
    }

    private String getTowards()
    {
        switch (MathHelper.floor((double) (Minecraft.getMinecraft().player.rotationYaw * 8.0F / 360.0F) + 0.5D) & 7)
        {
            case 0:
                return "+Z";
            case 1:
                return "-X +Z";
            case 2:
                return "-X";
            case 3:
                return "-X -Z";
            case 4:
                return "-Z";
            case 5:
                return "+X -Z";
            case 6:
                return "+X";
            case 7:
                return "+X +Z";
        }
        return "Invalid";
    }
}
