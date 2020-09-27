package com.salhack.summit.module.movement;

import com.salhack.summit.events.MinecraftEvent;
import com.salhack.summit.events.bus.EventHandler;
import com.salhack.summit.events.bus.Listener;
import com.salhack.summit.events.network.EventClientPacket;
import com.salhack.summit.events.player.EventPlayerMotionUpdate;
import com.salhack.summit.events.player.EventPlayerUpdate;
import com.salhack.summit.module.Module;
import com.salhack.summit.module.Value;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.network.play.client.CPacketEntityAction;
import net.minecraft.network.play.client.CPacketPlayerTryUseItemOnBlock;

public final class Sneak extends Module
{
    public final Value<String> mode = new Value<>("Mode", new String[]
    { "Mode", "M" }, "The sneak mode to use.", "NCP");

    public Sneak()
    {
        super("Sneak", new String[]
        { "Sneek" }, "Allows you to sneak at full speed", "NONE", 0xDB2493, ModuleType.MOVEMENT);
        setMetaData(getMetaData());
        
        mode.addString("Vanilla");
        mode.addString("NCP");
        mode.addString("Always");
    }

    @Override
    public void onDisable()
    {
        super.onDisable();
        if (mc.world != null && !mc.player.isSneaking())
        {
            mc.player.connection.sendPacket(new CPacketEntityAction(mc.player, CPacketEntityAction.Action.STOP_SNEAKING));
        }
    }

    public String getMetaData()
    {
        return this.mode.getValue();
    }

    @EventHandler
    private Listener<EventPlayerUpdate> onPlayerUpdate  = new Listener<>(event ->
    {
        setMetaData(getMetaData());
    });
    
    @EventHandler
    private Listener<EventPlayerMotionUpdate> OnPlayerUpdate = new Listener<>(p_Event ->
    {
        if (p_Event.getStage() != MinecraftEvent.Stage.Pre)
            return;
        
        switch (this.mode.getValue())
        {
            case "Vanilla":
                mc.player.connection.sendPacket(new CPacketEntityAction(mc.player, CPacketEntityAction.Action.START_SNEAKING));
                break;
            case "NCP":
                if (!mc.player.isSneaking())
                {
                    if (this.isMoving())
                    {
                        mc.player.connection.sendPacket(new CPacketEntityAction(mc.player, CPacketEntityAction.Action.START_SNEAKING));
                        mc.player.connection.sendPacket(new CPacketEntityAction(mc.player, CPacketEntityAction.Action.STOP_SNEAKING));
                    }
                    else
                    {
                        mc.player.connection.sendPacket(new CPacketEntityAction(mc.player, CPacketEntityAction.Action.START_SNEAKING));
                    }
                }
                break;
            case "Always":
                mc.gameSettings.keyBindSneak.pressed = true;
                break;
        }
    });

    @EventHandler
    private Listener<EventClientPacket> onClientPacket = new Listener<>(event ->
    {
        if (event.getStage() != MinecraftEvent.Stage.Pre)
            return;
        
        if (!mode.getValue().equals("Always"))
        {
            if (event.getPacket() instanceof CPacketPlayerTryUseItemOnBlock && !mc.player.isSneaking())
            {
                mc.player.connection.sendPacket(new CPacketEntityAction(mc.player, CPacketEntityAction.Action.STOP_SNEAKING));
            }
        }
    });

    private boolean isMoving()
    {
        return GameSettings.isKeyDown(mc.gameSettings.keyBindForward) || GameSettings.isKeyDown(mc.gameSettings.keyBindLeft) || GameSettings.isKeyDown(mc.gameSettings.keyBindRight)
                || GameSettings.isKeyDown(mc.gameSettings.keyBindBack);
    }

}
