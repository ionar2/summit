package com.salhack.summit.gui.hud.items;

import com.mojang.realmsclient.gui.ChatFormatting;

import com.salhack.summit.gui.hud.components.DraggableHudComponent;
import com.salhack.summit.main.SummitStatic;
import com.salhack.summit.managers.HudManager;
import com.salhack.summit.module.Value;
import com.salhack.summit.util.render.RenderUtil;
import net.minecraft.client.gui.ScaledResolution;

public class PvPInfoComponent extends DraggableHudComponent
{
    public final Value<Boolean> RainbowVal = new Value<Boolean>("Rainbow", new String[]{""}, "Makes a dynamic rainbow", true);

    public PvPInfoComponent()
    {
        super("PvPInfo", 2, 290, 100, 100);
    }

    @Override
    public void onRender(ScaledResolution res, float p_MouseX, float p_MouseY, float p_PartialTicks)
    {
        super.onRender(res, p_MouseX, p_MouseY, p_PartialTicks);

        final String aura = "KA " + ChatFormatting.WHITE + (SummitStatic.AURA.isEnabled() ? ChatFormatting.GREEN + "ON" : ChatFormatting.RED + "OFF");
        final String crystal = "CA " + ChatFormatting.WHITE + (SummitStatic.AUTOCRYSTAL.isEnabled() ? ChatFormatting.GREEN + "ON" : ChatFormatting.RED + "OFF");
        final String autoTrap = "AT " + ChatFormatting.WHITE + (SummitStatic.AUTOTRAP.isEnabled() ? ChatFormatting.GREEN + "ON" : ChatFormatting.RED + "OFF");
        final String surround = "SU " + ChatFormatting.WHITE + (SummitStatic.SURROUND.isEnabled() ? ChatFormatting.GREEN + "ON" : ChatFormatting.RED + "OFF");

        RenderUtil.drawStringWithShadow(aura, getX(), getY(), RainbowVal.getValue() ? HudManager.Get().rainbow.GetRainbowColorAt(0) : 0xAAAAAA);
        RenderUtil.drawStringWithShadow(crystal, getX(), getY() + 12, RainbowVal.getValue() ? HudManager.Get().rainbow.GetRainbowColorAt(0) : 0xAAAAAA);
        RenderUtil.drawStringWithShadow(autoTrap, getX(), getY() + 24, RainbowVal.getValue() ? HudManager.Get().rainbow.GetRainbowColorAt(0) : 0xAAAAAA);
        RenderUtil.drawStringWithShadow(surround, getX(), getY() + 36, RainbowVal.getValue() ? HudManager.Get().rainbow.GetRainbowColorAt(0) : 0xAAAAAA);

        setWidth(RenderUtil.getStringWidth(aura));
        setHeight(RenderUtil.getStringHeight(crystal)+RenderUtil.getStringHeight(aura)+RenderUtil.getStringHeight(autoTrap)+RenderUtil.getStringHeight(surround));
    }
}

