package com.salhack.summit.events.player;

import com.salhack.summit.events.MinecraftEvent;

public class EventPlayerSendChatMessage extends MinecraftEvent
{
    public String Message;

    public EventPlayerSendChatMessage(String p_Message)
    {
        super();
        
        Message = p_Message;
    }

}
