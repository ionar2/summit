package com.salhack.summit.command.impl;

import com.salhack.summit.command.Command;
import com.salhack.summit.util.MathUtil;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.Vec3d;

public class HClipCommand extends Command {
    public HClipCommand() {
        super("HClip", "Allows you to horizontally clip x blocks");
    }

    @Override
    public void processCommand(String input, String[] args) {
        if (args.length <= 1) {
            SendToChat("Invalid Input");
            return;
        }

        final double blocksForward = Double.parseDouble(args[1]);

        final Vec3d direction = MathUtil.direction(mc.player.rotationYaw);

        Entity entity = mc.player.isRiding() ? mc.player.getRidingEntity() : mc.player;

        if (entity != null) {
            entity.setPosition(mc.player.posX + direction.x * blocksForward, mc.player.posY, mc.player.posZ + direction.z * blocksForward);
        }

        SendToChat(String.format("Teleported you %s blocks forward", blocksForward));
    }

    @Override
    public String getHelp() {
        return "Allows you teleport forward x amount of blocks.";
    }
}
