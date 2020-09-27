package com.salhack.summit.module.movement;

import com.salhack.summit.events.blocks.EventBlockCollisionBoundingBox;
import com.salhack.summit.events.bus.EventHandler;
import com.salhack.summit.events.bus.Listener;
import com.salhack.summit.events.player.EventPlayerUpdate;
import com.salhack.summit.module.Module;
import com.salhack.summit.module.Value;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;

public class WebSpeed extends Module
{
    public final Value<Boolean> BoundingBox = new Value<>("BoundingBox", new String[] {""}, "Allows you to walk on the web", false);
    public final Value<Float> BBOffset = new Value<>("BBOffset", new String[] {""}, "How much Y to subtract from the BB", 0.25f);

    public WebSpeed()
    {
        super("WebSpeed", new String[] {"WebSpeed"}, "Speed hax in web", "NONE", -1, ModuleType.MOVEMENT);
    }
    
    @EventHandler
    private Listener<EventPlayerUpdate> onUpdate = new Listener<>(event ->
    {
        if (mc.player.isInWeb)
        {
            mc.player.onGround = false;
            mc.player.isInWeb = false;
            mc.player.motionX *= 0.84;
            mc.player.motionZ *= 0.84;
        }
    });

    @EventHandler
    private Listener<EventBlockCollisionBoundingBox> onCollisionBoundingBox = new Listener<>(event ->
    {
        if (mc.world == null || !BoundingBox.getValue())
            return;
        
        final Block block = mc.world.getBlockState(event.getPos()).getBlock();
        
        if (block.equals(Blocks.WEB))
        {
            event.cancel();
            event.setBoundingBox(Block.FULL_BLOCK_AABB.contract(0, BBOffset.getValue(), 0));
        }
    });
}
