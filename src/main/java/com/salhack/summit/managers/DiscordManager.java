package com.salhack.summit.managers;

import club.minnced.discord.rpc.DiscordEventHandlers;
import club.minnced.discord.rpc.DiscordRichPresence;
import com.salhack.summit.main.Summit;
import com.salhack.summit.main.SummitStatic;

public class DiscordManager
{
    private Thread _thread = null;

    public void enable()
    {
        club.minnced.discord.rpc.DiscordRPC lib = club.minnced.discord.rpc.DiscordRPC.INSTANCE;
        // New Summit Discord RPC ID
        String applicationId = "719958382274019350";
        String steamId = "";
        DiscordEventHandlers handlers = new DiscordEventHandlers();
        handlers.ready = (user) -> System.out.println("Ready!");
        lib.Discord_Initialize(applicationId, handlers, true, steamId);
        DiscordRichPresence presence = new DiscordRichPresence();
        presence.startTimestamp = System.currentTimeMillis() / 1000; // epoch second
        lib.Discord_UpdatePresence(presence);
        // in a worker thread
        presence.largeImageKey = "discordrpc2";
        presence.largeImageText = "Summit Premium Deluxe++++ Exclusive Gold Founders++ Edition";
        _thread = new Thread(() ->
        {
            while (!Thread.currentThread().isInterrupted())
            {
                lib.Discord_RunCallbacks();
                presence.details = SummitStatic.DISCORDRPC.generateDetails();
                presence.state = SummitStatic.DISCORDRPC.generateState();
                lib.Discord_UpdatePresence(presence);

                try
                {
                    Thread.sleep(2000);
                } catch (InterruptedException ignored)
                {
                }
            }
        }, "RPC-Callback-Handler");
        
        _thread.start();
    }
    
    public void disable() throws InterruptedException
    {
        if (_thread != null)
            _thread.interrupt();
    }
    
    public static DiscordManager Get()
    {
        return Summit.GetDiscordManager();
    }
}
