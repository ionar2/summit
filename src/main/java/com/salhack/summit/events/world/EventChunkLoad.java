package com.salhack.summit.events.world;

import com.salhack.summit.events.MinecraftEvent;
import net.minecraft.world.chunk.Chunk;

public class EventChunkLoad extends MinecraftEvent
{
    public EventChunkLoad(Type type, Chunk chunk)
    {
        _type = type;
        _chunk = chunk;
    }
    
    private Chunk _chunk;
    
    public Chunk getChunk()
    {
        return _chunk;
    }

    public enum Type
    {
        LOAD,
        UNLOAD,
    }
    
    private Type _type;
    
    public Type getType()
    {
        return _type;
    }

}
