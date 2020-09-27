package com.salhack.summit.module.misc;

import com.salhack.summit.events.MinecraftEvent;
import com.salhack.summit.events.bus.EventHandler;
import com.salhack.summit.events.bus.Listener;
import com.salhack.summit.events.network.EventClientPacket;
import com.salhack.summit.module.Module;
import net.minecraft.network.play.client.CPacketPlayerTryUseItemOnBlock;
import net.minecraft.util.EnumFacing;

public final class BuildHeight extends Module
{
    public BuildHeight()
    {
        super("BuildHeight", new String[]
        { "BuildH", "BHeight" }, "Allows you to interact with blocks at build height", "NONE", 0xDB246D, ModuleType.MISC);
    }

    @EventHandler
    private Listener<EventClientPacket> onClientPacket = new Listener<>(event ->
    {
        if (event.getStage() != MinecraftEvent.Stage.Pre)
            return;
        
        if (event.getPacket() instanceof CPacketPlayerTryUseItemOnBlock)
        {
            final CPacketPlayerTryUseItemOnBlock packet = (CPacketPlayerTryUseItemOnBlock) event.getPacket();
            if (packet.getPos().getY() >= 255 && packet.getDirection() == EnumFacing.UP)
            {
                packet.placedBlockDirection = EnumFacing.DOWN;
            }
        }
    });

}
