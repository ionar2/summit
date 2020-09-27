package com.salhack.summit.module.combat;

import com.salhack.summit.events.bus.EventHandler;
import com.salhack.summit.events.bus.Listener;
import com.salhack.summit.events.client.EventClientTick;
import com.salhack.summit.module.Module;
import com.salhack.summit.module.Value;
import net.minecraft.network.play.client.CPacketPlayerDigging;
import net.minecraft.network.play.client.CPacketPlayerTryUseItem;
import net.minecraft.util.math.BlockPos;

public class BowSpam extends Module
{
    public final Value<Integer> Ticks = new Value<>("Ticks", new String[] {"Delay"}, "Number of ticks required between next bow release", 3, 0, 40, 1);
    
    public BowSpam()
    {
        super("BowSpam", new String[]
        { "BS" }, "Releases the bow as fast as possible", "NONE", 0xDB2424, ModuleType.COMBAT);
    }
    
    private int _ticks = 0;

    @EventHandler
    private Listener<EventClientTick> OnTick = new Listener<>(event ->
    {
        if (mc.player.getHeldItemMainhand().getItem() instanceof net.minecraft.item.ItemBow && mc.player.isHandActive() && mc.player.getItemInUseMaxCount() >= 3)
        {
            if (++_ticks >= Ticks.getValue())
            {
                _ticks = 0;
                mc.player.connection.sendPacket(new CPacketPlayerDigging(CPacketPlayerDigging.Action.RELEASE_USE_ITEM, BlockPos.ORIGIN, mc.player.getHorizontalFacing()));
                mc.player.connection.sendPacket(new CPacketPlayerTryUseItem(mc.player.getActiveHand()));
                mc.player.stopActiveHand();
            }
        }
    });
}
