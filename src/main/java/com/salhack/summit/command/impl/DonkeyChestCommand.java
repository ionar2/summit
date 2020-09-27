package com.salhack.summit.command.impl;

import com.salhack.summit.command.Command;
import com.salhack.summit.main.Summit;
import com.salhack.summit.main.Wrapper;

import net.minecraft.entity.Entity;
import net.minecraft.entity.passive.AbstractChestHorse;
import net.minecraft.network.play.client.CPacketEntityAction;
import net.minecraft.network.play.client.CPacketUseEntity;
import net.minecraft.network.play.client.CPacketUseEntity.Action;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.Vec3d;

public class DonkeyChestCommand extends Command {
    public DonkeyChestCommand() {
        super("DonkeyChest", "Opens an entity chest");
    }

    @Override
    public void processCommand(String input, String[] args) {
        if (Wrapper.GetPlayer() != null) {
            Summit.SendMessage("Sent the packet to open horse inventory");
            
            for (Entity e : mc.world.loadedEntityList)
            {
                if (e instanceof AbstractChestHorse && e.getDistance(mc.player) < 10.0f)
                {
                    mc.player.connection.sendPacket(new CPacketEntityAction(mc.player, CPacketEntityAction.Action.START_SNEAKING));
                    mc.player.connection.sendPacket(new CPacketUseEntity(e, EnumHand.MAIN_HAND, new Vec3d(Math.random(), Math.random(), Math.random())));
                    mc.player.connection.sendPacket(new CPacketUseEntity(e, EnumHand.MAIN_HAND));
                    mc.player.connection.sendPacket(new CPacketEntityAction(mc.player, CPacketEntityAction.Action.STOP_SNEAKING));
                    break;
                }
            }
        }
    }

    @Override
    public String getHelp() {
        return "Opens an entity's chest";
    }
}
