package com.salhack.summit.command.impl;

import com.salhack.summit.command.Command;
import com.salhack.summit.managers.MacroManager;

public class MacroCommand extends Command {
    public MacroCommand() {
        super("Macro", "create macros");

        commandChunks.add("add <macro> <input>");
        commandChunks.add("remove <macro>");
        commandChunks.add("list");
    }

    @Override
    public void processCommand(String input, String[] args) {
        if (args.length <= 1) {
            SendToChat("Invalid Input");
            return;
        }

        if (args[1].toLowerCase().startsWith("a") || args[1].toLowerCase().startsWith("c"))
        {
            if (args.length >= 4)
            { 
                MacroManager.Get().addMacro(args[2].toUpperCase(), args[3]);
                SendToChat("added a macro named " + args[2] + " with args " + args[3]);
            }
        }
        else if (args[1].toLowerCase().startsWith("r"))
        {
            if (args.length >= 3)
            { 
                MacroManager.Get().removeMacro(args[2].toUpperCase());
                SendToChat("Removed a macro at key " + args[2].toUpperCase());
            }
        }
        else if (args[1].toLowerCase().startsWith("l"))
        {
            // todo..
        }
    }

    @Override
    public String getHelp() {
        return "Allows you to create macros to execute commands right away";
    }
}
