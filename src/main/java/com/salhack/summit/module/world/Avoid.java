package com.salhack.summit.module.world;

import com.salhack.summit.events.blocks.EventBlockCollisionBoundingBox;
import com.salhack.summit.module.Module;
import com.salhack.summit.module.Value;
import com.salhack.summit.events.bus.EventHandler;
import com.salhack.summit.events.bus.Listener;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;

public class Avoid extends Module
{
    public final Value<Boolean> Fire = new Value<Boolean>("Fire", new String[] {"f"}, "Prevents going into fire.", true);
    public final Value<Boolean> Cactus = new Value<Boolean>("Cactus", new String[] {"c"}, "Prevents going into cactus.", true);
    public final Value<Boolean> Unloaded = new Value<Boolean>("Unloaded", new String[] { "Void", "AntiVoid"}, "Prevents from going into unloaded chunks.", true);

    public Avoid()
    {
        super("Avoid", new String[] { "Avoid", "AntiVoid" }, "Avoids fire, cactus and optionally unloaded chunks", "NONE", -1, ModuleType.WORLD);
    }
    
    @EventHandler
    private Listener<EventBlockCollisionBoundingBox> onCollisionBoundingBox = new Listener<>(event ->
    {
        if (mc.world == null)
            return;
        
        final Block block = mc.world.getBlockState(event.getPos()).getBlock();
        
        if ((block.equals(Blocks.FIRE) && Fire.getValue()) || (block.equals(Blocks.CACTUS) && Cactus.getValue()) || ((!mc.world.isBlockLoaded(event.getPos(), false) || event.getPos().getY() < 0) && Unloaded.getValue()))
        {
            event.cancel();
            event.setBoundingBox(Block.FULL_BLOCK_AABB);
        }
    });
}
