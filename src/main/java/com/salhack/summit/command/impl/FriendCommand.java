package com.salhack.summit.command.impl;

import com.google.gson.internal.LinkedTreeMap;

import com.salhack.summit.command.Command;
import com.salhack.summit.managers.FriendManager;
import com.salhack.summit.friend.Friend;

public class FriendCommand extends Command {
    public FriendCommand() {
        super("Friend", "Allows you to communicate with the friend manager, allowing for adding/removing/updating friends");

        commandChunks.add("add <username>");
        commandChunks.add("remove <username>");
        commandChunks.add("list");
    }

    @Override
    public void processCommand(String input, String[] args) {
        if (args.length <= 1) {
            SendToChat("Invalid Input");
            return;
        }

        if (args[1].toLowerCase().startsWith("a")) {
            if (FriendManager.Get().AddFriend(args[2].toLowerCase()))
                SendToChat(String.format("Added %s as a friend.", args[2]));
            else
                SendToChat(String.format("%s is already a friend.", args[2]));
        } else if (args[1].toLowerCase().startsWith("r")) {
            if (FriendManager.Get().RemoveFriend(args[2].toLowerCase()))
                SendToChat(String.format("Removed %s as a friend.", args[2]));
            else
                SendToChat(String.format("%s is not a friend.", args[2]));
        } else if (args[1].toLowerCase().startsWith("l")) {
            final LinkedTreeMap<String, Friend> friendMap = FriendManager.Get().GetFriends();

            friendMap.forEach((k, v) -> SendToChat(String.format("F: %s A: %s", v.GetName(), v.GetAlias())));

            if (friendMap.isEmpty()) {
                SendToChat("You don't have any friends...");
            }
        }
    }

    @Override
    public String getHelp() {
        return "Allows you to add friends, or remove friends or list friends..\nfriend add <name>\nfriend remove<name>\nfriend list";
    }
}
