package com.salhack.summit.command;

import java.util.ArrayList;
import java.util.List;

import com.mojang.realmsclient.gui.ChatFormatting;

import com.salhack.summit.main.Summit;
import com.salhack.summit.main.Wrapper;
import net.minecraft.client.Minecraft;

public abstract class Command {
    private final String name;
    private final String description;

    protected final Minecraft mc = Wrapper.GetMC();
    protected final List<String> commandChunks = new ArrayList<>();

    public Command(String name, String description) {
        this.name = name;
        this.description = description;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public abstract void processCommand(String input, String[] args);

    protected void SendToChat(String desc) {
        Summit.SendMessage(ChatFormatting.LIGHT_PURPLE + "[" + getName() + "]: " + ChatFormatting.YELLOW + desc);
    }

    public List<String> getChunks() {
        return commandChunks;
    }

    public String getHelp() {
        return description;
    }
}
