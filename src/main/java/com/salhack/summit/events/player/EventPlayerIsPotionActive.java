package com.salhack.summit.events.player;

import com.salhack.summit.events.MinecraftEvent;
import net.minecraft.potion.Potion;

public class EventPlayerIsPotionActive extends MinecraftEvent
{
    public Potion potion;
    
    public EventPlayerIsPotionActive(Potion p_Potion)
    {
        super();
        
        potion = p_Potion;
    }
}
