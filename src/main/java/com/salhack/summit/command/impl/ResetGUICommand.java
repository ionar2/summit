package com.salhack.summit.command.impl;

import com.salhack.summit.command.Command;
import com.salhack.summit.main.SummitStatic;

public class ResetGUICommand extends Command {
    public ResetGUICommand() {
        super("ResetGUI", "Reset the ClickGUI positions to default");
    }

    @Override
    public void processCommand(String input, String[] args) {
        SummitStatic.CLICKGUI.ResetToDefaults();
        SendToChat("Reset the ClickGUI");
    }

    @Override
    public String getHelp() {
        return "Resets the positions of the ClickGUI";
    }
}
