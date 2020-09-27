package com.salhack.summit.module.misc;

import com.salhack.summit.events.blocks.EventCanPlaceCheck;
import com.salhack.summit.module.Module;
import com.salhack.summit.events.bus.EventHandler;
import com.salhack.summit.events.bus.Listener;
import net.minecraft.block.BlockShulkerBox;

public class AntiShulkerPlace extends Module
{
    public AntiShulkerPlace() {
        super("AntiShulkerPlace", new String[]
                { "NoShulkerPlace", "CancelShulkerPlace" }, "Prevents you from accidentally placing shulkers",
                "NONE", 0xDB24C4, ModuleType.MISC);
    }

    @EventHandler
    private Listener<EventCanPlaceCheck> CheckEvent = new Listener<>(event -> {
        if (event.Block.isAssignableFrom(BlockShulkerBox.class)) {
            event.cancel();
        }
    });
}
