package com.salhack.summit.events.player;

import com.salhack.summit.events.MinecraftEvent;

public class EventPlayerMotionUpdateCancelled extends EventPlayerMotionUpdate
{
    public EventPlayerMotionUpdateCancelled(MinecraftEvent.Stage stage, float pitch, float yaw)
    {
        super(stage, 0, 0, 0, false);
        _pitch = pitch;
        _yaw = yaw;
    }
}
