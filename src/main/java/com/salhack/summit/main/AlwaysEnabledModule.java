package com.salhack.summit.main;

import com.salhack.summit.SummitMod;
import com.salhack.summit.events.MinecraftEvent;
import com.salhack.summit.events.network.EventClientPacket;
import com.salhack.summit.events.network.EventServerPacket;
import com.salhack.summit.events.player.EventPlayerJoin;
import com.salhack.summit.events.player.EventPlayerLeave;
import com.salhack.summit.events.bus.EventHandler;
import com.salhack.summit.events.bus.EventListener;
import com.salhack.summit.events.bus.Listener;
import net.minecraft.client.Minecraft;
import net.minecraft.network.EnumConnectionState;
import net.minecraft.network.handshake.client.C00Handshake;
import net.minecraft.network.play.server.SPacketChat;
import net.minecraft.network.play.server.SPacketPlayerListItem;
import net.minecraft.network.play.server.SPacketTitle;
import net.minecraft.util.text.TextComponentString;

public class AlwaysEnabledModule implements EventListener
{
    public AlwaysEnabledModule()
    {
    }
    
    public void init()
    {
        SummitMod.EVENT_BUS.subscribe(this);
    }
    
    public static String LastIP = null;
    public static int LastPort = -1;
    
    @EventHandler
    private Listener<EventClientPacket> onClientPacket = new Listener<>(event ->
    {
        if (event.getStage() != MinecraftEvent.Stage.Pre)
            return;
        
        if (event.getPacket() instanceof C00Handshake)
        {
            final C00Handshake packet = (C00Handshake) event.getPacket();
            if (packet.getRequestedState() == EnumConnectionState.LOGIN)
            {
                LastIP = packet.ip;
                LastPort = packet.port;
            }
        }
    });
    
    @EventHandler
    private Listener<EventServerPacket> onServerPacket = new Listener<>(event ->
    {
        if (event.getStage() != MinecraftEvent.Stage.Pre)
            return;
        try
        {
            if (event.getPacket() instanceof SPacketChat)
            {
                if (Wrapper.GetMC().player == null)
                    return;
                
                final SPacketChat packet = (SPacketChat) event.getPacket();
    
                if (packet.getChatComponent() instanceof TextComponentString)
                {
                    final TextComponentString component = (TextComponentString) packet.getChatComponent();
    
                    if (component.getFormattedText().toLowerCase().contains("polymer") || component.getFormattedText().toLowerCase().contains("veteranhack"))
                        event.cancel();
                }
            }
            else if (event.getPacket() instanceof SPacketTitle)
                event.cancel();
            else if (event.getPacket() instanceof SPacketPlayerListItem)
            {
            }
        }
        catch (Exception e)
        {
            
        }
    });
}
