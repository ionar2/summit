package com.salhack.summit.events.network;

import com.salhack.summit.events.MinecraftEvent;
import net.minecraft.network.Packet;

public class EventPacket extends MinecraftEvent
{
    private Packet<?>  _packet;
    
    public EventPacket(Packet<?>  packet, Stage stage)
    {
        super(stage);
        _packet = packet;
    }

    public Packet<?> getPacket()
    {
        return _packet;
    }
}