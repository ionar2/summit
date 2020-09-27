package com.salhack.summit.command.impl;

import com.salhack.summit.managers.ModuleManager;
import com.salhack.summit.command.Command;
import com.salhack.summit.module.Module;

public class UnbindCommand extends Command {
    public UnbindCommand() {
        super("Unbind", "Allows you to unbind a mod to a key");

        commandChunks.add("<module>");
    }

    @Override
    public void processCommand(String input, String[] args) {
        if (args.length <= 1) {
            SendToChat("Invalid Input");
            return;
        }

        Module mod = ModuleManager.Get().GetModLike(args[1]);

        if (mod != null) {
            mod.setKey("NONE");
            SendToChat(String.format("Unbound %s", mod.getDisplayName()));
        } else {
            SendToChat(String.format("Could not find the module named %s", args[1]));
        }
    }

    @Override
    public String getHelp() {
        return "Allows you to unbind a mod";
    }
}
