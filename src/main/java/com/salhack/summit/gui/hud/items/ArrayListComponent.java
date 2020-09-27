package com.salhack.summit.gui.hud.items;

import java.util.ArrayList;
import java.util.HashMap;

import com.salhack.summit.gui.hud.components.HudComponentFlags;
import com.salhack.summit.gui.hud.components.OptionalListHudComponent;
import com.salhack.summit.gui.hud.components.util.CornerItem;
import com.salhack.summit.gui.hud.components.util.CornerList;
import com.salhack.summit.managers.HudManager;
import com.salhack.summit.managers.ModuleManager;
import com.salhack.summit.module.Module;
import com.salhack.summit.module.Value;
import net.minecraft.client.gui.ScaledResolution;

public class ArrayListComponent extends OptionalListHudComponent
{
    public final Value<Boolean> NoBackground = new Value<Boolean>("NoBackground", new String[]
    { "" }, "NoBackground on arraylist", false);
    public final Value<Integer> RainbowIncrease = new Value<Integer>("RainbowIncrease", new String[] {""}, "How much color array should iterate each mod", 20, 0, 50, 1);
    public final Value<Boolean> Reorder = new Value<Boolean>("Reorder", new String[]
    { "" }, "Reorders the rainbow", true);
    
    private ArrayList<CornerItem> currentModItems;
    private HashMap<Module, CornerItem> modToCornerItems;
    
    public ArrayListComponent()
    {
        super("ArrayList", 0, 0, 0, 0, "TopRight");
        this.enabled = true;
    }
    
    @Override
    public void setEnabled(boolean enabled)
    {
        clear();
        super.setEnabled(enabled);
        initList();
    }
    
    @Override
    public void setCurrentCornerList(CornerList list)
    {
        clear();
        super.setCurrentCornerList(list);
        initList();
    }
    
    public void clear()
    {
        if (getCurrentCornerList() != null)
        {
            currentModItems.forEach(mod ->
            {
                getCurrentCornerList().removeCornerItem(mod);
            });
        }
        
        if (currentModItems != null)
            currentModItems.clear();
    }
    
    public void initList()
    {
        currentModItems = new ArrayList<>();
        modToCornerItems = new HashMap<>();
        if (getCurrentCornerList() != null)
        {
            ModuleManager.Get().GetModuleList().forEach(mod ->
            {
                if (mod.isEnabled() && !mod.isHidden())
                {
                    CornerItem item = new CornerItem(mod.getDisplayName(), mod.getMetaData(), mod.getColor(), true);
                    currentModItems.add(item);
                    
                    getCurrentCornerList().addCornerItem(item);
                    
                    modToCornerItems.put(mod, item);
                }
            });
        }
    }
    
    @Override
    public void onRender(ScaledResolution res, float mouseX, float mouseY, float partialTicks)
    {
        this.setWidth(100f);
        if (getCurrentCornerList() != null)
            this.setHeight(getCurrentCornerList().getHeight());
    }

    public void addMod(Module mod)
    {
        CornerItem item = new CornerItem(mod.getDisplayName(), mod.getMetaData(), mod.getColor(), true);
        currentModItems.add(item);
        
        if (getCurrentCornerList() != null)
            getCurrentCornerList().addCornerItem(item);
        modToCornerItems.put(mod, item);
    }

    public void removeMod(Module mod)
    {
        final CornerItem item = modToCornerItems.get(mod);
        
        if (item != null)
        {
            currentModItems.remove(item);
            
            if (getCurrentCornerList() != null)
                getCurrentCornerList().removeCornerWithAnimation(item);
        }
    }

}
