package com.salhack.summit.module.movement;

import com.salhack.summit.events.MinecraftEvent;
import com.salhack.summit.events.bus.EventHandler;
import com.salhack.summit.events.bus.Listener;
import com.salhack.summit.events.network.EventClientPacket;
import com.salhack.summit.events.player.EventPlayerMotionUpdate;
import com.salhack.summit.module.Module;
import com.salhack.summit.module.Value;
import com.salhack.summit.util.Timer;
import com.salhack.summit.util.entity.PlayerUtil;
import net.minecraft.block.BlockLiquid;
import net.minecraft.init.Items;
import net.minecraft.network.play.client.CPacketEntityAction;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.network.play.client.CPacketPlayerTryUseItem;
import net.minecraft.network.play.client.CPacketPlayerTryUseItemOnBlock;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;

public final class NoFall extends Module
{
    public final Value<String> Mode = new Value<>("Mode", new String[] { "M" }, "Mode to perform on", "Packet");
    
    public NoFall()
    {
        super("NoFall", new String[] { "NoFallDamage" }, "Prevents fall damage", "NONE", 0x4BCA5C, ModuleType.MOVEMENT);
        
        Mode.addString("Packet");
        Mode.addString("Bucket");
        Mode.addString("Anti");
        Mode.addString("AAC");
        Mode.addString("NCP");
    }
    
    private Timer lastElytraFlyTimer = new Timer();

    @EventHandler
    private Listener<EventClientPacket> onClientPacket = new Listener<>(event ->
    {
        if (event.getStage() != MinecraftEvent.Stage.Pre)
            return;
        
        if (event.getPacket() instanceof CPacketEntityAction)
        {
            final CPacketEntityAction packet = (CPacketEntityAction) event.getPacket();
            
            if (packet.getAction() == CPacketEntityAction.Action.START_FALL_FLYING)
            {
                lastElytraFlyTimer.reset();
            }
        }
    });
    
    @EventHandler
    private Listener<EventPlayerMotionUpdate> onMotionUpdate = new Listener<>(event ->
    {
        if (event.getStage() != MinecraftEvent.Stage.Pre)
            return;
        
        setMetaData(String.valueOf(Mode.getValue()));
        
        if (mc.player.isElytraFlying() || !lastElytraFlyTimer.passed(1000))
            return;
        
        switch (Mode.getValue())
        {
            case "Packet":
                if (mc.player.fallDistance > 3.0f)
                {
                    event.cancel();
                    event.setOnGround(true);
                }
                break;
            case "Bucket":
                if (mc.player.fallDistance > 3.0f)
                {
                    BlockPos selectedPosition = null;
                    
                    for (int i = (int) mc.player.posY; i > (int) mc.player.posY - 5; --i)
                    {
                        BlockPos pos = new BlockPos(mc.player.posX, i, mc.player.posZ);
                        
                        if (mc.world.isAirBlock(pos))
                            continue;

                        if (mc.world.getBlockState(pos).getBlock() instanceof BlockLiquid)
                            continue;
                        
                        selectedPosition = pos;
                        break;
                    }
                    
                    if (selectedPosition != null)
                    {
                        boolean hasBucket = false;
                        
                        if (mc.player.getHeldItemMainhand().getItem() != Items.WATER_BUCKET)
                        {
                            int slot = PlayerUtil.GetItemInHotbar(Items.WATER_BUCKET);
                            
                            if (slot != -1)
                            {
                                hasBucket = true;
                                mc.player.inventory.currentItem = slot;
                                mc.playerController.updateController();
                            }
                        }
                        else
                            hasBucket = true;
                        
                        if (hasBucket)
                        {
                            event.cancel();
                            event.setPitch(90f);
                            mc.player.connection.sendPacket(new CPacketPlayerTryUseItemOnBlock(selectedPosition, EnumFacing.UP, EnumHand.MAIN_HAND, 0, 0, 0));
                            mc.player.connection.sendPacket(new CPacketPlayerTryUseItem(EnumHand.MAIN_HAND));
                        }
                    }
                }
                break;
            case "AAC":
                if (mc.player.fallDistance > 3.0f)
                {
                    mc.player.fallDistance = 0.0f;
                    mc.player.onGround = true;
                    mc.player.capabilities.isFlying = true;
                    mc.player.capabilities.allowFlying = true;
                    event.cancel();
                    event.setOnGround(false);
                    mc.player.velocityChanged = true;
                    mc.player.capabilities.isFlying = false;
                    mc.player.jump();
                }
                else
                {
                    event.cancel();
                    event.setOnGround(true);
                    mc.player.capabilities.isFlying = false;
                    mc.player.capabilities.allowFlying = false;
                }
                break;
            case "Anti":
                if (mc.player.fallDistance > 3.0f)
                    mc.player.connection.sendPacket(new CPacketPlayer.Position(mc.player.posX, mc.player.posY + 1, mc.player.posZ, true));
                break;
            case "NCP":
                if (mc.player.fallDistance > 4.0f)
                {
                    mc.player.fallDistance = 0;
                    mc.player.connection.sendPacket(new CPacketPlayer.Position(mc.player.posX +420420, mc.player.posY, mc.player.posZ, false));
                    mc.player.connection.sendPacket(new CPacketPlayer.Position(mc.player.posX, mc.player.posY + 1, mc.player.posZ, true));
                }
                break;
            default:
                break;
        }
    });
}
