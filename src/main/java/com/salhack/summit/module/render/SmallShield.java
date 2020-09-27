package com.salhack.summit.module.render;

import com.salhack.summit.events.bus.EventHandler;
import com.salhack.summit.events.bus.Listener;
import com.salhack.summit.events.player.EventPlayerUpdate;
import com.salhack.summit.events.render.EventRenderUpdateEquippedItem;
import com.salhack.summit.module.Module;
import com.salhack.summit.module.Value;
import net.minecraft.util.EnumHand;

public class SmallShield extends Module
{
    public final Value<Float> MainProgress = new Value<Float>("MainProgress", new String[] {""}, "Mainhand progress", 0.5f, 0.0f, 1.0f, 0.1f);
    public final Value<Float> OffProgress = new Value<Float>("OffProgress", new String[] {""}, "Offhand progress", 0.5f, 0.0f, 1.0f, 0.1f);
    
    public SmallShield()
    {
        super("SmallShield", new String[]
        { "SmallShield", "SS", "HandProgress" }, "Smaller view of mainhand/offhand, smallshield", "NONE", 0x89D3B3, ModuleType.RENDER);
    }
    
    @EventHandler
    private Listener<EventPlayerUpdate> OnPlayerUpdate = new Listener<>(event ->
    {
        mc.entityRenderer.itemRenderer.equippedProgressMainHand = MainProgress.getValue();
        mc.entityRenderer.itemRenderer.equippedProgressOffHand = OffProgress.getValue();
    });

    @EventHandler
    private Listener<EventRenderUpdateEquippedItem> OnUpdateEquippedItem = new Listener<>(event ->
    {
        mc.entityRenderer.itemRenderer.itemStackMainHand = mc.player.getHeldItem(EnumHand.MAIN_HAND);
        mc.entityRenderer.itemRenderer.itemStackOffHand = mc.player.getHeldItem(EnumHand.OFF_HAND);
    });
}
