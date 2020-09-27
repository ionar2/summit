package com.salhack.summit.command.impl;

import com.salhack.summit.managers.PresetsManager;
import com.salhack.summit.command.Command;

public class PresetsCommand extends Command {
    public PresetsCommand() {
        super("Presets", "Allows you to create custom presets");

        commandChunks.add("create <name>");
        commandChunks.add("delete <name>");
        commandChunks.add("list");
    }

    @Override
    public void processCommand(String input, String[] args) {
        if (args.length <= 1) {
            SendToChat("Invalid Input");
            return;
        }

        if (args[1].toLowerCase().startsWith("c")) {
            String presetName = args[2].toLowerCase();

            if (!presetName.equalsIgnoreCase("Default")) {
                PresetsManager.Get().CreatePreset(presetName);
                SendToChat("Created a preset named " + presetName);
            } else
                SendToChat("Default preset is reserved!");

        } else if (args[1].toLowerCase().startsWith("d")) {
            String presetName = args[2].toLowerCase();

            if (!presetName.equalsIgnoreCase("Default")) {
                PresetsManager.Get().RemovePreset(presetName);
                SendToChat("Removed a preset named " + presetName);
            } else
                SendToChat("Default preset is reserved!");

        } else if (args[1].toLowerCase().startsWith("l")) {
            PresetsManager.Get().GetItems().forEach(p -> SendToChat(p.getName()));
        }
    }

    @Override
    public String getHelp() {
        return "Allows you to create, remove and list the presets";
    }
}
