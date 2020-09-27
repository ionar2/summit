package com.salhack.summit.module.misc;

import com.salhack.summit.events.MinecraftEvent;
import com.salhack.summit.events.bus.EventHandler;
import com.salhack.summit.events.bus.Listener;
import com.salhack.summit.events.network.EventServerPacket;
import com.salhack.summit.events.player.EventPlayerUpdate;
import com.salhack.summit.managers.NotificationManager;
import com.salhack.summit.module.Module;
import com.salhack.summit.module.Value;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.play.server.SPacketEntityStatus;

import java.util.HashMap;

public class TotemPopNotifier extends Module
{
    public final Value<Boolean> ChatMessages = new Value<Boolean>("ChatMessages", new String[] {"ChatMessages"}, "Send Chat Messages.", true);
    private HashMap<String, Integer> TotemPopContainer = new HashMap<String, Integer>();
    
    public TotemPopNotifier()
    {
        super("TotemPopNotifier", new String[] {"TPN"}, "Notifys when someone pops a totem!", "NONE", 0x2482DB, ModuleType.MISC);
    }
    
    @EventHandler
    private Listener<EventServerPacket> onServerPacket = new Listener<>(event ->
    {
        if (event.getStage() != MinecraftEvent.Stage.Pre)
            return;
        
        if (event.getPacket() instanceof SPacketEntityStatus)
        {
            SPacketEntityStatus l_Packet = (SPacketEntityStatus) event.getPacket();
            
            if (l_Packet.getOpCode() == 35) ///< Opcode check the packet 35 is totem, thxmojang
            {
                Entity l_Entity = l_Packet.getEntity(mc.world);
                
                if (l_Entity == null)
                    return;
                
                int l_Count = 1;
                
                if (TotemPopContainer.containsKey(l_Entity.getName()))
                {
                    l_Count = TotemPopContainer.get(l_Entity.getName()).intValue();
                    TotemPopContainer.put(l_Entity.getName(), ++l_Count);
                }
                else
                {
                    TotemPopContainer.put(l_Entity.getName(), l_Count);
                }
                
                NotificationManager.Get().AddNotification("TotemPop", l_Entity.getName() + " popped " + l_Count + " totem(s)!");
                if(ChatMessages.getValue())
                {
                    SendMessage(l_Entity.getName() + " popped " + l_Count + " totem(s)!");
                }
            }
        }
    });

    @EventHandler
    private Listener<EventPlayerUpdate> OnPlayerUpdate = new Listener<>(p_Event ->
    {
        for (EntityPlayer l_Player : mc.world.playerEntities)
        {
            if (!TotemPopContainer.containsKey(l_Player.getName()))
                continue;
            
            if (l_Player.isDead || l_Player.getHealth() <= 0.0f)
            {
                int l_Count = TotemPopContainer.get(l_Player.getName()).intValue();
                
                TotemPopContainer.remove(l_Player.getName());

                    NotificationManager.Get().AddNotification("TotemPop", l_Player.getName() + " died after popping " + l_Count + " totem(s)!");
                if(ChatMessages.getValue())
                {
                    SendMessage(l_Player.getName() + " died after popping " + l_Count + " totem(s)!");
                }
            }
        }
    });
}
