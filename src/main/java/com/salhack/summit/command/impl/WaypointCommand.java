package com.salhack.summit.command.impl;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import com.salhack.summit.waypoints.Waypoint;
import com.salhack.summit.waypoints.WaypointManager;
import com.salhack.summit.command.Command;
import com.salhack.summit.util.SalVec3d;

public class WaypointCommand extends Command {
    public WaypointCommand() {
        super("Waypoint", "Allows you to create waypoints, remove them, or edit them, if no args are put in, the last created waypoint is used");

        commandChunks.add("add <optional: name> x y z");
        commandChunks.add("remove <optional: name>");
        commandChunks.add("edit <optional: name> x y z");
    }

    @Override
    public void processCommand(String input, String[] args) {
        if (args.length <= 1) {
            SendToChat(getHelp());
            return;
        }

        String name = null;
        SalVec3d pos = null;

        if (args.length >= 3)
            name = args[2];

        if (args.length > 3) {
            pos = new SalVec3d(0, -420, 0);

            pos.x = Double.parseDouble(args[3]);

            if (args.length == 4)
                pos.z = Double.parseDouble(args[4]);
            else pos.y = Double.parseDouble(args[4]);

            if (args.length > 5)
                pos.z = Double.parseDouble(args[5]);

            if (pos.y == -420)
                pos.y = 100;
        }

        if (pos == null)
            pos = new SalVec3d(mc.player.posX, mc.player.posY, mc.player.posZ);

        if (args[1].startsWith("a")) {
            if (name == null) {
                DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
                LocalDateTime now = LocalDateTime.now();
                name = dtf.format(now);
            }

            WaypointManager.Get().AddWaypoint(Waypoint.Type.Normal, name, pos, mc.player.dimension);
        } else if (args[1].startsWith("r")) {
            if (WaypointManager.Get().RemoveWaypoint(name))
                SendToChat("Successfully removed the waypoint named " + (name == null ? "last" : name));
            else
                SendToChat("Fail!");
        } else if (args[1].startsWith("e")) {
            if (WaypointManager.Get().EditWaypoint(name, pos))
                SendToChat("Successfully edited the waypoint");
            else
                SendToChat("Fail!");
        }
    }
}
