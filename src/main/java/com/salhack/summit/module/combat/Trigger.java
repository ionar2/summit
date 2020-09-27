package com.salhack.summit.module.combat;

import com.salhack.summit.events.player.EventPlayerUpdate;
import com.salhack.summit.main.SummitStatic;
import com.salhack.summit.managers.FriendManager;
import com.salhack.summit.managers.TickRateManager;
import com.salhack.summit.module.Module;
import com.salhack.summit.module.Value;
import com.salhack.summit.events.bus.EventHandler;
import com.salhack.summit.events.bus.Listener;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.EnumHand;

public class Trigger extends Module
{
    public final Value<Boolean> HitDelay = new Value<>("Hit Delay", new String[] {"Hit Delay"}, "Use vanilla hit delay", true);
    public final Value<Boolean> TpsSync = new Value<>("TpsSync", new String[] {"TPS"}, "Tps syncs the HitDelay", false);
    
    public Trigger()
    {
        super("Trigger", new String[] {"AutoClicker"}, "Clicks for you when you are hovering over a target.", "NONE", -1, ModuleType.COMBAT);
    }
    
    @EventHandler
    private Listener<EventPlayerUpdate> onPlayerUpdate = new Listener<>(event ->
    {
        if (SummitStatic.AURA.isEnabled())
            return;
        
        if (mc.objectMouseOver != null && mc.objectMouseOver.entityHit != null)
        {
            Entity e = mc.objectMouseOver.entityHit;
            
            if (!(e instanceof EntityLivingBase) || FriendManager.Get().IsFriend(e) || e.isInvisible())
                return;
            
            if (isAttackReady())
            {
                mc.playerController.attackEntity(mc.player, e);
                mc.player.swingArm(EnumHand.MAIN_HAND);
            }
        }
    });
    
    private boolean isAttackReady()
    {
        final float ticks = 20.0f - TickRateManager.Get().getTickRate();

        return HitDelay.getValue() ? (mc.player.getCooledAttackStrength(TpsSync.getValue() ? -ticks : 0f) >= 1) : true;
    }
}
