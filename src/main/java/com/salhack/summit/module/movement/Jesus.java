package com.salhack.summit.module.movement;

import com.salhack.summit.events.MinecraftEvent;
import com.salhack.summit.events.MinecraftEvent.Stage;
import com.salhack.summit.events.blocks.EventBlockCollisionBoundingBox;
import com.salhack.summit.events.bus.EventHandler;
import com.salhack.summit.events.bus.Listener;
import com.salhack.summit.events.network.EventClientPacket;
import com.salhack.summit.events.network.EventServerPacket;
import com.salhack.summit.events.player.EventPlayerMotionUpdate;
import com.salhack.summit.events.player.EventPlayerUpdate;
import com.salhack.summit.module.Module;
import com.salhack.summit.module.Value;
import com.salhack.summit.util.entity.PlayerUtil;
import net.minecraft.block.Block;
import net.minecraft.block.BlockLiquid;
import net.minecraft.block.state.IBlockState;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.network.play.client.CPacketVehicleMove;
import net.minecraft.network.play.server.SPacketPlayerPosLook;
import net.minecraft.util.math.AxisAlignedBB;

public final class Jesus extends Module
{
    public final Value<String> mode = new Value<>("Mode", new String[]
    { "Mode", "M" }, "The current Jesus/WaterWalk mode to use.", "Solid");
    public final Value<Boolean> Boost = new Value<>("Boost", new String[] {"B"}, "Allows you to boost while jesusing", true);

    public Jesus()
    {
        super("Jesus", new String[]
        { "LiquidWalk", "WaterWalk" }, "Allows you to walk on water", "NONE", 0x88DDEB, ModuleType.MOVEMENT);
        setMetaData(getMetaData());
        
        mode.addString("Solid");
        mode.addString("Dolphin");
    }

    private boolean wasWater = false;
    private int ticks = 0;
    private int boostTicks = 0;
    private float prevYOffset;
    
    public String getMetaData()
    {
        if (mc.player != null)
        {
            if (mc.player.isRiding())
                return null;
        }

        return this.mode.getValue();
    }

    @EventHandler
    private Listener<EventPlayerUpdate> onPlayerUpdate = new Listener<>(event ->
    {
        setMetaData(getMetaData());
        
        if (mode.getValue().equals("Dolphin"))
        {
            if (!mc.player.movementInput.sneak && !mc.player.movementInput.jump && PlayerUtil.isInLiquid())
                mc.player.motionY = 0.131f;
        }
        else if (mode.getValue().equals("Solid"))
        {
            if (!mc.player.movementInput.sneak && !mc.player.movementInput.jump && PlayerUtil.isInLiquid())
                mc.player.motionY = 0.1;

            if (PlayerUtil.isOnLiquid() && canJesus() && Boost.getValue())
            {
                switch (boostTicks)
                {
                    case 0:
                        mc.player.motionX *= 1.1;
                        mc.player.motionZ *= 1.1;
                        break;
                    case 1:
                        mc.player.motionX *= 1.27;
                        mc.player.motionZ *= 1.27;
                        break;
                    case 2:
                        mc.player.motionX *= 1.51;
                        mc.player.motionZ *= 1.51;
                        break;
                    case 3:
                        mc.player.motionX *= 1.15;
                        mc.player.motionZ *= 1.15;
                        break;
                    case 4:
                        mc.player.motionX *= 1.23;
                        mc.player.motionZ *= 1.23;
                        break;
                }
                
                ++boostTicks;
                
                if (boostTicks > 4)
                    boostTicks = 0;
            }
        }
    });

    @EventHandler
    private Listener<EventServerPacket> onPlayerPosLook = new Listener<>(event ->
    {
        if (event.getStage() == Stage.Pre && event.getPacket() instanceof SPacketPlayerPosLook)
            boostTicks = 0;
    });
    
    @EventHandler
    private Listener<EventPlayerMotionUpdate> onMotionUpdate = new Listener<>(event ->
    {
        if (event.getStage() != MinecraftEvent.Stage.Pre)
            return;
        
        // it's fine if it's cancelled.
        if (mode.getValue().equals("Solid"))
        {
            if (mc.player.isRowingBoat())
                return;

            /*if (PlayerUtil.isOnLiquid() && canJesus())
            {
                event.cancel();
                event.setOnGround(false);
                
                if (NCPStrict.getValue())
                {
                    if (prevYOffset >= 0.5f)
                        prevYOffset = 0;
                    
                    prevYOffset += Math.random() / 2;
                    
                    SendMessage("YOffset is " + prevYOffset);
                    
                    event.setY(event.getY() - prevYOffset);
                }
                else
                {
                    if (mc.player.ticksExisted % 2 == 0)
                        event.setY(event.getY() - 0.05);
                }
            }*/
        }
    });

    @EventHandler
    private Listener<EventClientPacket> onClientPacket = new Listener<>(event ->
    {
        if (event.getStage() != MinecraftEvent.Stage.Pre)
            return;
        
        try
        {
            if (!mode.getValue().equals("Solid") || mc.world == null || mc.player == null && !mc.player.isRowingBoat())
                return;
            
            if (event.getPacket() instanceof CPacketPlayer)
            {
                CPacketPlayer packet = (CPacketPlayer) event.getPacket();
    
                if (PlayerUtil.isOnLiquid() && canJesus())
                {
                    packet.onGround = false;
                    packet.y = mc.player.ticksExisted % 2 == 0 ? packet.y - 0.05 : packet.y;
                }
            }
        }
        catch (Exception e)
        {
            
        }
    });

    @EventHandler
    private Listener<EventBlockCollisionBoundingBox> OnLiquidCollisionBB = new Listener<>(event ->
    {
        if (mc.world == null || mc.player == null)
            return;
        
        if (mode.getValue().equals("Dolphin"))
            return;
        
        if (mode.getValue().equals("Solid") && !PlayerUtil.isOnLiquid())
            return;

        if (PlayerUtil.isInLiquid())
            return;

        IBlockState state = mc.world.getBlockState(event.getPos());
        
        if (state.getBlock() instanceof BlockLiquid && !mc.player.isRowingBoat())
        {
            event.cancel();
            event.setBoundingBox(Block.FULL_BLOCK_AABB);
            
            if (mc.player.getRidingEntity() != null)
                event.setBoundingBox(new AxisAlignedBB(0, 0, 0, 1, 1 - 0.05f, 1));
            /*else if (mode.getValue() == Mode.Dolphin)
                event.setBoundingBox(new AxisAlignedBB(0, 0, 0, 1, 0.9f, 1));*/
            else if (mode.getValue().equals("Dolphin"))
            {
                if (canJesus())
                    event.setBoundingBox(new AxisAlignedBB(event.getPos().getX(), event.getPos().getY(), event.getPos().getY(), event.getPos().getX(), event.getPos().getY() + (mc.player.movementInput.jump ? 0.95 : 0.99), event.getPos().getZ()));
            }
        }
    });

    private boolean canJesus()
    {
        return (this.mc.player.fallDistance < 3.0F && !this.mc.player.movementInput.jump && !PlayerUtil.isInLiquid() && !mc.player.isSneaking());
    }
}
