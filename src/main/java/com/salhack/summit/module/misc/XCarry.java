package com.salhack.summit.module.misc;

import com.salhack.summit.events.MinecraftEvent;
import com.salhack.summit.events.bus.EventHandler;
import com.salhack.summit.events.bus.Listener;
import com.salhack.summit.events.network.EventClientPacket;
import com.salhack.summit.module.Module;
import com.salhack.summit.module.Value;
import net.minecraft.network.play.client.CPacketCloseWindow;

public final class XCarry extends Module
{
    public final Value<Boolean> ForceCancel = new Value<Boolean>("ForceCancel", new String[]
            { "" }, "Forces canceling of all CPacketCloseWindow packets", false);

    public XCarry()
    {
        super("XCarry", new String[]
        { "XCarry", "MoreInventory" }, "Allows you to carry items in your crafting and dragging slot", "NONE", 0xB30C30, ModuleType.MISC);
    }

    @Override
    public void onDisable()
    {
        super.onDisable();
        if (mc.world != null)
        {
            mc.player.connection.sendPacket(new CPacketCloseWindow(mc.player.inventoryContainer.windowId));
        }
    }

    @EventHandler
    private Listener<EventClientPacket> onClientPacket = new Listener<>(event ->
    {
        if (event.getStage() != MinecraftEvent.Stage.Pre)
            return;
        
        if (event.getPacket() instanceof CPacketCloseWindow)
        {
            final CPacketCloseWindow packet = (CPacketCloseWindow) event.getPacket();
            if (packet.windowId == mc.player.inventoryContainer.windowId || ForceCancel.getValue())
                event.cancel();
        }
    });
}
