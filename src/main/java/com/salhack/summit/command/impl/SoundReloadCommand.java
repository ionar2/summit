package com.salhack.summit.command.impl;

import com.mojang.realmsclient.gui.ChatFormatting;

import com.salhack.summit.command.Command;

public class SoundReloadCommand extends Command {
    public SoundReloadCommand() {
        super("SoundReload", "Reloads the sound system");
    }

    @Override
    public void processCommand(String input, String[] args) {
        mc.getSoundHandler().sndManager.reloadSoundSystem();
        SendToChat(ChatFormatting.GREEN + "Reloaded the SoundSystem!");
    }

    @Override
    public String getHelp() {
        return "Reloads the sound manager sound system";
    }
}
