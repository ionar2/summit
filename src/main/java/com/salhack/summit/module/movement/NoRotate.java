package com.salhack.summit.module.movement;

import com.salhack.summit.events.MinecraftEvent.Stage;
import com.salhack.summit.events.bus.EventHandler;
import com.salhack.summit.events.bus.Listener;
import com.salhack.summit.events.network.EventServerPacket;
import com.salhack.summit.module.Module;

import net.minecraft.network.play.server.SPacketPlayerPosLook;

public final class NoRotate extends Module
{
    public NoRotate()
    {
        super("NoRotate", new String[]
        { "NoRot", "AntiRotate" }, "Prevents you from processing server rotations", "NONE", 0x24B2DB, ModuleType.MOVEMENT);
    }

    @EventHandler
    private Listener<EventServerPacket> onPlayerPosLook = new Listener<>(event ->
    {
        if (event.getStage() == Stage.Pre && event.getPacket() instanceof SPacketPlayerPosLook)
        {
            SPacketPlayerPosLook packet = (SPacketPlayerPosLook) event.getPacket();
            
            packet.pitch = mc.player.rotationPitch;
            packet.yaw = mc.player.rotationYaw;
        }
    });
}
