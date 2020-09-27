package com.salhack.summit.module.render;

import com.salhack.summit.events.MinecraftEvent;
import com.salhack.summit.events.bus.EventHandler;
import com.salhack.summit.events.bus.Listener;
import com.salhack.summit.events.network.EventServerPacket;
import com.salhack.summit.events.player.EventPlayerIsPotionActive;
import com.salhack.summit.events.player.EventPlayerUpdate;
import com.salhack.summit.events.render.*;
import com.salhack.summit.module.Module;
import com.salhack.summit.module.Value;
import com.salhack.summit.util.Timer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.MobEffects;
import net.minecraft.network.play.server.SPacketEntityStatus;
import net.minecraftforge.client.event.RenderBlockOverlayEvent;
import net.minecraftforge.client.event.RenderBlockOverlayEvent.OverlayType;

import java.util.Iterator;

public class NoRender extends Module
{
    public final Value<String> NoItems = new Value<>("NoItemsMode", new String[] {"NoItems"}, "Prevents items from being rendered", "Off");
    public final Value<Boolean> Fire = new Value<Boolean>("Fire", new String[] {"Fire"}, "Doesn't render Fire overlay", true);
    public final Value<Boolean> NoHurtCam = new Value<Boolean>("HurtCamera", new String[] {"NHC"}, "Doesn't render the Hurt camera", true);
    public final Value<Boolean> PumpkinOverlay = new Value<Boolean>("PumpkinOverlay", new String[] {"PO"}, "Doesn't render the pumpkin overlay", false);
    public final Value<Boolean> Blindness = new Value<Boolean>("Blindness", new String[] {"Blindness"}, "Doesn't render the blindness effect", true);
    public final Value<Boolean> TotemAnimation = new Value<Boolean>("TotemAnimation", new String[] {"TotemAnimation"}, "Doesn't render the totem animation", false);
    public final Value<Boolean> Skylight = new Value<Boolean>("Skylight", new String[] {"Skylight"}, "Doesn't render skylight updates", false);
    public final Value<Boolean> SignText = new Value<Boolean>("SignText", new String[] {"SignText"}, "Doesn't render SignText", false);
    public final Value<Boolean> NoArmor = new Value<Boolean>("NoArmor", new String[] {"NoArmor"}, "Doesn't render armor", false);
    public final Value<Boolean> NoArmorPlayers = new Value<Boolean>("NoArmorPlayers", new String[] {"NoArmorPlayers"}, "Use in conjunction with the above mod", false);
    public final Value<Boolean> Maps = new Value<Boolean>("Maps", new String[] {"Maps"}, "Doesn't render maps", false);
    public final Value<Boolean> BossHealth = new Value<Boolean>("BossHealth", new String[] {"WitherNames"}, "Doesn't render wither names, and other boss health", false);
    
    public NoRender()
    {
        super("NoRender", new String[] {"NR"}, "Doesn't render certain things, if enabled", "NONE", -1, ModuleType.RENDER);
        
        NoItems.addString("Off");
        NoItems.addString("Remove");
        NoItems.addString("Hide");
    }
    
    private Timer timer = new Timer();
    
    @EventHandler
    private Listener<EventRenderEntity> OnRenderEntity = new Listener<>(event ->
    {
        if (mc.world == null)
            return;
        
        if (event.GetEntity() instanceof EntityItem && NoItems.getValue().equals("Hide"))
            event.cancel();
    });
    
    @EventHandler
    private Listener<EventPlayerUpdate> OnPlayerUpdate = new Listener<>(event ->
    {
        if (mc.world == null)
            return;
        
        switch (NoItems.getValue())
        {
            case "Remove":
                if (!timer.passed(5000))
                    return;
                
                timer.reset();
                
                Iterator<Entity> itr = mc.world.loadedEntityList.iterator();
                
                while (itr.hasNext())
                {
                    Entity entity = itr.next();
                    
                    if (entity != null)
                    {
                        if (entity instanceof EntityItem)
                            mc.world.removeEntity(entity);
                    }
                }
                break;
            default:
                break;
        }
    });

    @EventHandler
    private Listener<EventRenderHurtCameraEffect> OnHurtCameraEffect = new Listener<>(p_Event ->
    {
        if (mc.world == null)
            return;
        
        if (NoHurtCam.getValue())
            p_Event.cancel();
    });
    
    @EventHandler
    private Listener<RenderBlockOverlayEvent> OnBlockOverlayEvent = new Listener<>(p_Event ->
    {
        if (mc.world == null)
            return;
        
        if (Fire.getValue() && p_Event.getOverlayType() == OverlayType.FIRE)
            p_Event.setCanceled(true);
        if (PumpkinOverlay.getValue() && p_Event.getOverlayType() == OverlayType.BLOCK)
            p_Event.setCanceled(true);
    });

    @EventHandler
    private Listener<EventPlayerIsPotionActive> IsPotionActive = new Listener<>(p_Event ->
    {
        if (mc.world == null)
            return;
        
        if (p_Event.potion == MobEffects.BLINDNESS && Blindness.getValue())
            p_Event.cancel();
    });
    
    /*@EventHandler
    private Listener<EventParticleEmitParticleAtEntity> OnEmitParticleAtEntity = new Listener<>(p_Event ->
    {
        if (p_Event.Type == EnumParticleTypes.TOTEM && TotemAnimation.getValue())
            p_Event.cancel();
    });*/

    @EventHandler
    private Listener<EventServerPacket> onServerPacket = new Listener<>(event ->
    {
        if (mc.world == null)
            return;
        
        if (event.getStage() != MinecraftEvent.Stage.Pre)
            return;
        
        if (mc.world == null || mc.player == null)
            return;
        
        if (event.getPacket() instanceof SPacketEntityStatus && TotemAnimation.getValue())
        {
            SPacketEntityStatus l_Packet = (SPacketEntityStatus)event.getPacket();
            
            if (l_Packet.getOpCode() == 35)
            {
                event.cancel();
            }
        }
    });

    @EventHandler
    private Listener<EventRenderSign> OnRenderSign = new Listener<>(p_Event ->
    {
        if (mc.world == null)
            return;
        
        if (SignText.getValue())
            p_Event.cancel();
    });

    @EventHandler
    private Listener<EventRenderArmorLayer> OnRenderArmorLayer = new Listener<>(p_Event ->
    {
        if (mc.world == null)
            return;
        
        if (NoArmor.getValue())
        {
            if (!(p_Event.Entity instanceof EntityPlayer) && NoArmorPlayers.getValue())
                return;
            
            p_Event.cancel();
        }
    });

    @EventHandler
    private Listener<EventRenderMap> OnRenderMap = new Listener<>(p_Event ->
    {
        if (mc.world == null)
            return;
        
        if (Maps.getValue())
            p_Event.cancel();
    });
    
    @EventHandler
    private Listener<EventRenderBossHealth> OnRenderBossHealth = new Listener<>(p_Event ->
    {
        if (mc.world == null)
            return;
        
       if (BossHealth.getValue())
           p_Event.cancel();
    });

}
