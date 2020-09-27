package com.salhack.summit.module.world;

import com.salhack.summit.events.bus.EventHandler;
import com.salhack.summit.events.bus.Listener;
import com.salhack.summit.events.player.EventPlayerUpdate;
import com.salhack.summit.module.Module;
import net.minecraft.client.settings.KeyBinding;

public class AutoMine extends Module
{
    public AutoMine()
    {
        super("AutoMine", new String[] {"AM"}, "Holds down your left click.", "NONE", -1, ModuleType.WORLD);
    }
    
    @Override
    public void onDisable()
    {
        super.onDisable();
        KeyBinding.setKeyBindState(mc.gameSettings.keyBindAttack.getKeyCode(), false);
    }
    
    @EventHandler
    private Listener<EventPlayerUpdate> onPlayerUpdate = new Listener<>(event ->
    {
        KeyBinding.setKeyBindState(mc.gameSettings.keyBindAttack.getKeyCode(), true);
    });
}
