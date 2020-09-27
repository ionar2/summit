package com.salhack.summit.command.impl;

import com.salhack.summit.command.Command;
import net.minecraft.entity.Entity;

public class VClipCommand extends Command {
    public VClipCommand() {
        super("VClip", "Allows you to vclip x blocks");
    }

    @Override
    public void processCommand(String input, String[] args) {
        if (args.length <= 1) {
            SendToChat("Invalid Input");
            return;
        }

        final double distance = Double.parseDouble(args[1]);

        Entity entity = mc.player.isRiding() ? mc.player.getRidingEntity() : mc.player;

        if (entity != null) {
            entity.setPosition(mc.player.posX, mc.player.posY + distance, mc.player.posZ);
        }

        SendToChat(String.format("Teleported you %s blocks up", distance));
    }

    @Override
    public String getHelp() {
        return "Allows you teleport up x amount of blocks.";
    }
}
