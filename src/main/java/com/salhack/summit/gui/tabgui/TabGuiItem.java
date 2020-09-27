package com.salhack.summit.gui.tabgui;

import java.util.ArrayList;

import com.salhack.summit.module.Module;

public class TabGuiItem
{
    private final String displayName;
    private final ArrayList<TabGuiItem> addons = new ArrayList<>();
    private boolean isOpened = false;
    private boolean isHovered = false;
    private Module Mod = null;
    
    public TabGuiItem(final String displayName)
    {
        this.displayName = displayName;
    }

    public TabGuiItem(final String displayName, Module mod)
    {
        this(displayName);
        Mod = mod;
    }
    
    public String getDisplayName()
    {
        return displayName;
    }

    public ArrayList<TabGuiItem> getAddons()
    {
        return addons;
    }

    public boolean isOpened()
    {
        return isOpened;
    }

    public void setOpened(boolean isOpened)
    {
        this.isOpened = isOpened;
    }

    public boolean isHovered()
    {
        return isHovered;
    }

    public void setHovered(boolean isHovered)
    {
        this.isHovered = isHovered;
    }

    public void toggle()
    {
        if (Mod != null)
            Mod.toggle();
    }

    public int getColor()
    {
        return Mod != null && Mod.isEnabled() ? 0x00FFE4 : -1;
    }
}
