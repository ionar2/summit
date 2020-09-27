package com.salhack.summit.events.player;

import com.salhack.summit.events.MinecraftEvent;
import net.minecraft.util.EnumHand;

public class EventPlayerSwingArm extends MinecraftEvent
{
    public EnumHand Hand;
    
    public EventPlayerSwingArm(EnumHand p_Hand)
    {
        super();
        Hand = p_Hand;
    }
}
