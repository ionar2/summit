package com.salhack.summit.command.impl;

import com.salhack.summit.managers.ModuleManager;
import com.salhack.summit.command.Command;
import com.salhack.summit.module.Module;

public class BindCommand extends Command {
    public BindCommand() {
        super("Bind", "Allows you to bind a mod to a key");

        commandChunks.add("<module>");
        commandChunks.add("<module> <key>");
    }

    @Override
    public void processCommand(String input, String[] args) {
        if (args.length <= 1) {
            SendToChat("Invalid Input");
            return;
        }

        Module searchedMod = ModuleManager.Get().GetModLike(args[1]);

        if (searchedMod != null) {
            if (args.length <= 2) {
                SendToChat(String.format("The key of %s is %s", searchedMod.getDisplayName(), searchedMod.getKey()));
                return;
            }

            searchedMod.setKey(args[2].toUpperCase());
            SendToChat(String.format("Set the key of %s to %s", searchedMod.getDisplayName(), searchedMod.getKey()));
        } else {
            SendToChat(String.format("Could not find the module named %s", args[1]));
        }
    }

    @Override
    public String getHelp() {
        return "Allows you to Bind a mod";
    }
}
