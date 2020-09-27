package com.salhack.summit.module.combat;

import com.salhack.summit.managers.FriendManager;
import com.salhack.summit.events.MinecraftEvent.Stage;
import com.salhack.summit.events.bus.EventHandler;
import com.salhack.summit.events.bus.Listener;
import com.salhack.summit.events.player.EventPlayerMotionUpdate;
import com.salhack.summit.module.Module;
import com.salhack.summit.util.MathUtil;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBow;
import net.minecraft.util.math.Vec3d;

public class BowAim extends Module
{
    public BowAim()
    {
        super("BowAim", new String[] {"BA"}, "Predicts enemies movement and aims at them when using a Bow.", "NONE", 0xC2B692, ModuleType.COMBAT);
    }

    @EventHandler
    private Listener<EventPlayerMotionUpdate> onMotionUpdate = new Listener<>(event ->
    {
        if (event.getStage() != Stage.Pre || event.isCancelled())
            return;
        
        if (mc.player.getHeldItemMainhand().getItem() instanceof ItemBow && mc.player.isHandActive() && mc.player.getItemInUseMaxCount() >= 3)
        {
            EntityPlayer target = null;
            float lastDistance = 100f;
            
            for (EntityPlayer p : mc.world.playerEntities)
            {
                if (p instanceof EntityPlayerSP || FriendManager.Get().IsFriend(p))
                    continue;
                
                float dist = p.getDistance(mc.player);
                
                if (dist < lastDistance)
                {
                    lastDistance = dist;
                    target = p;
                }
            }
            
            if (target != null)
            {
                Vec3d pos = MathUtil.interpolateEntity(target, mc.getRenderPartialTicks());
                float[] angles = MathUtil.calcAngle(MathUtil.interpolateEntity(mc.player, mc.getRenderPartialTicks()), pos);
                
                event.cancel();
                event.setYaw(angles[0]);
                event.setPitch(angles[1]);
            }
        }
    });
}
