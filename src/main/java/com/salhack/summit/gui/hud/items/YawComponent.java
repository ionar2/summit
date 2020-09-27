package com.salhack.summit.gui.hud.items;

import java.text.DecimalFormat;

import com.mojang.realmsclient.gui.ChatFormatting;

import com.salhack.summit.gui.hud.components.OptionalListHudComponent;
import com.salhack.summit.managers.HudManager;
import com.salhack.summit.module.Value;
import net.minecraft.util.math.MathHelper;

public class YawComponent extends OptionalListHudComponent
{
    public YawComponent()
    {
        super("Yaw", 2, 200);
        setCurrentCornerList(HudManager.Get().GetModList("BottomLeft"));
    }

    @Override
    public void onUpdate()
    {
        DecimalFormat l_Format = new DecimalFormat("#.##");
        float l_Yaw = MathHelper.wrapDegrees(mc.player.rotationYaw);
        
        String direction = "Yaw: " + ChatFormatting.WHITE + l_Format.format(l_Yaw);
        
        if (!direction.contains("."))
            direction += ".00";
        else
        {
            String[] l_Split = direction.split("\\.");
            
            if (l_Split != null && l_Split[1] != null && l_Split[1].length() != 2)
                direction += 0;
        }
        
        this.cornerItem.setName(direction);
    }

}
