package com.salhack.summit.module.world;

import com.salhack.summit.events.player.EventPlayerClickBlock;
import com.salhack.summit.events.player.EventPlayerDamageBlock;
import com.salhack.summit.events.player.EventPlayerResetBlockRemoving;
import com.salhack.summit.events.player.EventPlayerUpdate;
import com.salhack.summit.module.Module;
import com.salhack.summit.module.Value;
import com.salhack.summit.events.bus.EventHandler;
import com.salhack.summit.events.bus.Listener;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.network.play.client.CPacketPlayerDigging;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;

public final class SpeedyGonzales extends Module
{
    public final Value<String> mode = new Value<>("Mode", new String[]
    { "Mode", "M" }, "The speed-mine mode to use.", "Instant");
    public final Value<Float> Speed = new Value<Float>("Speed", new String[]
    { "S" }, "Speed for Bypass Mode", 1.0f, 0.0f, 1.0f, 0.1f);

    private enum Mode
    {
        Packet, Damage, Instant, Bypass, NoBreakDelay
    }

    public final Value<Boolean> reset = new Value<Boolean>("Reset", new String[]
    { "Res" }, "Stops current block destroy damage from resetting if enabled.", true);
    public final Value<Boolean> doubleBreak = new Value<Boolean>("DoubleBreak", new String[]
    { "DoubleBreak", "Double", "DB" }, "Mining a block will also mine the block above it, if enabled.", false);
    public final Value<Boolean> FastFall = new Value<Boolean>("FastFall", new String[]
    { "FF" }, "Makes it so you fall faster.", false);

    public SpeedyGonzales()
    {
        super("SpeedyGonzales", new String[]
        { "Speedy Gonzales" }, "Allows you to break blocks faster", "NONE", 0x24DB60, ModuleType.WORLD);
        setMetaData(getMetaData());
        
        mode.addString("Packet");
        mode.addString("Damage");
        mode.addString("Instant");
        mode.addString("Bypass");
        mode.addString("NoBreakDelay");
    }

    public String getMetaData()
    {
        return this.mode.getValue();
    }

    @EventHandler
    private Listener<EventPlayerUpdate> OnPlayerUpdate = new Listener<>(p_Event ->
    {
        setMetaData(getMetaData());
        mc.playerController.blockHitDelay = 0;

        if (this.reset.getValue() && Minecraft.getMinecraft().gameSettings.keyBindUseItem.isKeyDown())
        {
            mc.playerController.isHittingBlock = false;
        }

        if (FastFall.getValue())
        {
            if (mc.player.onGround)
                --mc.player.motionY;
        }
    });

    @EventHandler
    private Listener<EventPlayerResetBlockRemoving> ResetBlock = new Listener<>(p_Event ->
    {
        if (this.reset.getValue())
        {
            p_Event.cancel();
        }
    });

    @EventHandler
    private Listener<EventPlayerClickBlock> ClickBlock = new Listener<>(p_Event ->
    {
        if (this.reset.getValue())
        {
            if (mc.playerController.curBlockDamageMP > 0.1f)
            {
                mc.playerController.isHittingBlock = true;
            }
        }
    });

    @EventHandler
    private Listener<EventPlayerDamageBlock> OnDamageBlock = new Listener<>(p_Event ->
    {
        if (canBreak(p_Event.getPos()))
        {
            if (this.reset.getValue())
            {
                mc.playerController.isHittingBlock = false;
            }

            switch (this.mode.getValue())
            {
            case "Packet":
                mc.player.swingArm(EnumHand.MAIN_HAND);
                mc.player.connection.sendPacket(new CPacketPlayerDigging(
                        CPacketPlayerDigging.Action.START_DESTROY_BLOCK, p_Event.getPos(), p_Event.getDirection()));
                mc.player.connection.sendPacket(new CPacketPlayerDigging(CPacketPlayerDigging.Action.STOP_DESTROY_BLOCK,
                        p_Event.getPos(), p_Event.getDirection()));
                p_Event.cancel();
                break;
            case "Damage":
                if (mc.playerController.curBlockDamageMP >= 0.7f)
                {
                    mc.playerController.curBlockDamageMP = 1.0f;
                }
                break;
            case "Instant":
                mc.player.swingArm(EnumHand.MAIN_HAND);
                mc.player.connection.sendPacket(new CPacketPlayerDigging(
                        CPacketPlayerDigging.Action.START_DESTROY_BLOCK, p_Event.getPos(), p_Event.getDirection()));
                mc.player.connection.sendPacket(new CPacketPlayerDigging(CPacketPlayerDigging.Action.STOP_DESTROY_BLOCK,
                        p_Event.getPos(), p_Event.getDirection()));
                mc.playerController.onPlayerDestroyBlock(p_Event.getPos());
                mc.world.setBlockToAir(p_Event.getPos());
                break;
            case "Bypass":

                final IBlockState blockState = Minecraft.getMinecraft().world.getBlockState(p_Event.getPos());
                
                SendMessage("" + mc.playerController.curBlockDamageMP);

                mc.playerController.curBlockDamageMP += Speed.getValue();

                break;
            case "NoBreakDelay":
                break;
            }
        }

        if (this.doubleBreak.getValue())
        {
            final BlockPos above = p_Event.getPos().add(0, 1, 0);

            if (canBreak(above) && mc.player.getDistance(above.getX(), above.getY(), above.getZ()) <= 5f)
            {
                mc.player.swingArm(EnumHand.MAIN_HAND);
                mc.player.connection.sendPacket(new CPacketPlayerDigging(
                        CPacketPlayerDigging.Action.START_DESTROY_BLOCK, above, p_Event.getDirection()));
                mc.player.connection.sendPacket(new CPacketPlayerDigging(CPacketPlayerDigging.Action.STOP_DESTROY_BLOCK,
                        above, p_Event.getDirection()));
                mc.playerController.onPlayerDestroyBlock(above);
                mc.world.setBlockToAir(above);
            }
        }
    });

    private boolean canBreak(BlockPos pos)
    {
        final IBlockState blockState = mc.world.getBlockState(pos);
        final Block block = blockState.getBlock();

        return block.getBlockHardness(blockState, Minecraft.getMinecraft().world, pos) != -1;
    }

}
