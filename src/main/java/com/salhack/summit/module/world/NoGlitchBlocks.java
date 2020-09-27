package com.salhack.summit.module.world;

import com.salhack.summit.events.blocks.EventPlaceBlockAt;
import com.salhack.summit.events.bus.EventHandler;
import com.salhack.summit.events.bus.Listener;
import com.salhack.summit.module.Module;
import com.salhack.summit.module.Value;

public class NoGlitchBlocks extends Module
{
    public final Value<Boolean> Destroy = new Value<Boolean>("Destroy", new String[]
            { "destroy" }, "Syncs Destroying", true);
    public final Value<Boolean> Place = new Value<Boolean>("Place", new String[]
            { "placement" }, "Syncs placement.", true);

    public NoGlitchBlocks()
    {
        super("NoGlitchBlocks", new String[]
                { "AntiGhostBlocks" }, "Synchronizes client and server communication by canceling clientside destroy/place for blocks", "NONE", 0xBF6B23, ModuleType.WORLD);
    }

    /*@EventHandler
    private Listener<EventNetworkPacketEvent> PacketEvent = new Listener<>(p_Event ->
    {
        if (p_Event.getPacket() instanceof SPacketBlockChange)
        {
            SPacketBlockChange l_Packet = (SPacketBlockChange)p_Event.getPacket();

            SendMessage(String.format("%s %s", l_Packet.getBlockPosition().toString(), l_Packet.getBlockState().toString()));
        }
    });*/

    // handled in mixin
    /*@EventHandler
    private Listener<EventPlayerDestroyBlock> OnPlayerDestroyBlock = new Listener<>(event ->
    {
        if (!Destroy.getValue())
            return;
        // Wait for server to process this, and send back a packet later.
        event.cancel();
    });*/

    @EventHandler
    private Listener<EventPlaceBlockAt> OnSetBlockState = new Listener<>(event ->
    {
        if (!Place.getValue())
            return;
        
        event.cancel();
    });
}
