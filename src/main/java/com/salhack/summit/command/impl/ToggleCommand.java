package com.salhack.summit.command.impl;

import com.mojang.realmsclient.gui.ChatFormatting;

import com.salhack.summit.managers.ModuleManager;
import com.salhack.summit.command.Command;
import com.salhack.summit.module.Module;

public class ToggleCommand extends Command {
    public ToggleCommand() {
        super("Toggle", "Allows you to toggle a mod");
    }

    @Override
    public void processCommand(String input, String[] args) {
        if (args.length <= 1) {
            SendToChat("Invalid Input");
            return;
        }

        Module mod = ModuleManager.Get().GetModLike(args[1]);

        if (mod != null) {
            mod.toggle();

            SendToChat(String.format("%sToggled %s", mod.isEnabled() ? ChatFormatting.GREEN : ChatFormatting.RED, mod.GetArrayListDisplayName()));
        } else {
            SendToChat(String.format("Could not find the module named %s", args[1]));
        }
    }

    @Override
    public String getHelp() {
        return "Allows you to toggle a mod";
    }
}
