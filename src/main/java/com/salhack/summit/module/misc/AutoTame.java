package com.salhack.summit.module.misc;

import com.salhack.summit.events.MinecraftEvent;
import com.salhack.summit.events.bus.EventHandler;
import com.salhack.summit.events.bus.Listener;
import com.salhack.summit.events.network.EventClientPacket;
import com.salhack.summit.events.player.EventPlayerUpdate;
import com.salhack.summit.module.Module;
import com.salhack.summit.module.Value;
import com.salhack.summit.util.Timer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.passive.AbstractHorse;
import net.minecraft.network.play.client.CPacketUseEntity;
import net.minecraft.util.EnumHand;

public class AutoTame extends Module
{
    public final Value<Float> Delay = new Value<Float>("Delay", new String[] {"D"}, "Delay to remount", 0.1f, 0.0f, 1.0f, 0.1f);
    
    public AutoTame()
    {
        super("AutoTame", new String[] {""}, "Automatically tames the animal you click", "NONE", 0xDB24C4, ModuleType.MISC);
    }
    
    private AbstractHorse EntityToTame = null;
    private Timer timer = new Timer();

    @Override
    public void toggleNoSave()
    {
        
    }
    
    @Override
    public void onEnable()
    {
        super.onEnable();
        
        SendMessage("Right click an animal you want to tame");
        
        EntityToTame = null;
    }

    @EventHandler
    private Listener<EventClientPacket> onClientPacket = new Listener<>(event ->
    {
        if (event.getStage() != MinecraftEvent.Stage.Pre)
            return;
        
        if (event.getPacket() instanceof CPacketUseEntity)
        {
            if (EntityToTame != null)
                return;
            
            final CPacketUseEntity l_Packet = (CPacketUseEntity) event.getPacket();
            
            Entity l_Entity = l_Packet.getEntityFromWorld(mc.world);
            
            if (l_Entity instanceof AbstractHorse)
            {
                if (!((AbstractHorse) l_Entity).isTame())
                {
                    EntityToTame = (AbstractHorse)l_Entity;
                    SendMessage("Will try to tame " + l_Entity.getName());
                    setMetaData(l_Entity.getName());
                }
            }
        }
    });
    
    @EventHandler
    private Listener<EventPlayerUpdate> OnPlayerUpdate = new Listener<>(p_Event ->
    {
        if (EntityToTame == null)
            return;
        
        if (EntityToTame.isTame())
        {
            SendMessage("Successfully tamed " + EntityToTame.getName() + ", disabling.");
            toggle();
            return;
        }
        
        if (mc.player.isRiding())
            return;
        
        if (mc.player.getDistance(EntityToTame) > 5.0f)
            return;
        
        if (!timer.passed(Delay.getValue() * 1000))
            return;
        
        timer.reset();
        mc.getConnection().sendPacket(new CPacketUseEntity(EntityToTame, EnumHand.MAIN_HAND));
    });
}
