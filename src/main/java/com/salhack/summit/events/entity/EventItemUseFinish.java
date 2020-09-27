package com.salhack.summit.events.entity;

import com.salhack.summit.events.MinecraftEvent;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;

public class EventItemUseFinish extends MinecraftEvent
{
    private EntityLivingBase entity;
    private ItemStack active;
    
    public EventItemUseFinish(EntityLivingBase e, ItemStack activeItemStack)
    {
        entity = e;
        active = activeItemStack;
    }

    public EntityLivingBase getEntity()
    {
        return entity;
    }

    public ItemStack getActive()
    {
        return active;
    }
}
