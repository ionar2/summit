package com.salhack.summit.module.movement;

import com.salhack.summit.events.player.EventPlayerUpdate;
import com.salhack.summit.module.Module;
import com.salhack.summit.events.bus.EventHandler;
import com.salhack.summit.events.bus.Listener;
import net.minecraft.init.Blocks;

public class IceSpeed extends Module
{
    public IceSpeed()
    {
        super("IceSpeed", new String[] {"IceSpeed", "IceSped", "OiceSped", "OiceSpeed", "Isped", "Ispeed"}, "Allows you to move faster on ice", "NONE", 0x00F7FF, ModuleType.MOVEMENT);
    }
    
    @Override
    public void onDisable()
    {
        super.onDisable();
        Blocks.ICE.setDefaultSlipperiness(0.98f);
        Blocks.PACKED_ICE.setDefaultSlipperiness(0.98f);
        Blocks.FROSTED_ICE.setDefaultSlipperiness(0.98f);
    }

    @EventHandler
    private Listener<EventPlayerUpdate> onPlayerUpdate = new Listener<>(event ->
    {
        Blocks.ICE.setDefaultSlipperiness(0.4f);
        Blocks.PACKED_ICE.setDefaultSlipperiness(0.4f);
        Blocks.FROSTED_ICE.setDefaultSlipperiness(0.4f);
    });
}
