package com.salhack.summit.command.impl;

import java.util.List;

import com.mojang.realmsclient.gui.ChatFormatting;

import com.salhack.summit.managers.CommandManager;
import com.salhack.summit.command.Command;

public class HelpCommand extends Command {
    public HelpCommand() {
        super("Help", "Gives you help for commands");
    }

    @Override
    public void processCommand(String input, String[] args) {
        if (args.length <= 1) {
            SendToChat(getHelp());
            return;
        }

        Command command = CommandManager.Get().GetCommandLike(args[1]);

        if (command == null)
            SendToChat(String.format("Couldn't find any command named like %s", args[1]));
        else
            SendToChat(command.getHelp());
    }

    @Override
    public String getHelp() {
        final List<Command> commandList = CommandManager.Get().GetCommands();

        StringBuilder commandString = new StringBuilder("Available commands: (" + commandList.size() + ")" + ChatFormatting.WHITE + " [");

        for (int i = 0; i < commandList.size(); ++i) {
            Command command = commandList.get(i);

            if (i == commandList.size() - 1)
                commandString.append(command.getName()).append("]");
            else
                commandString.append(command.getName()).append(", ");
        }

        return commandString.toString();
    }
}
