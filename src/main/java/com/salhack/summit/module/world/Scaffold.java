package com.salhack.summit.module.world;

import com.salhack.summit.events.MinecraftEvent;
import com.salhack.summit.events.MinecraftEvent.Stage;
import com.salhack.summit.events.bus.EventHandler;
import com.salhack.summit.events.bus.Listener;
import com.salhack.summit.events.network.EventClientPacket;
import com.salhack.summit.events.network.EventServerPacket;
import com.salhack.summit.events.player.EventPlayerMotionUpdate;
import com.salhack.summit.events.player.EventPlayerMove;
import com.salhack.summit.events.player.EventPlayerUpdate;
import com.salhack.summit.module.Module;
import com.salhack.summit.module.Value;
import com.salhack.summit.util.BlockInteractionHelper;
import com.salhack.summit.util.Timer;
import com.salhack.summit.util.entity.PlayerUtil;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.network.play.client.CPacketPlayer.Rotation;
import net.minecraft.network.play.server.SPacketPlayerPosLook;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;

import java.util.function.Consumer;

public class Scaffold extends Module
{
    public final Value<String> Mode = new Value<>("Mode", new String[]
    { "" }, "Tower lets you go up fast when holding space and placing blocks, normal will disable that", "Tower");
    public final Value<Boolean> StopMotion = new Value<Boolean>("StopMotion", new String[] {""}, "Stops you from moving if the block isn't placed yet", true);
    public final Value<Float> Delay = new Value<Float>("Delay", new String[]
    { "Delay" }, "Delay of the place", 0f, 0.0f, 1.0f, 0.1f);

    public Scaffold()
    {
        super("Scaffold", new String[]
        { "Scaffold" }, "Places blocks under you", "NONE", 0x94A253, ModuleType.WORLD);
        setMetaData(getMetaData());
        
        Mode.addString("Tower");
        Mode.addString("Normal");
    }
    
    private Timer _timer = new Timer();
    private Timer _towerPauseTimer = new Timer();
    private Timer _towerTimer = new Timer();
    private float[] _rotations = null;
    
    public String getMetaData()
    {
        return String.valueOf(Mode.getValue());
    }
    
    @Override
    public void toggleNoSave()
    {
        
    }
    
    @EventHandler
    private Listener<EventPlayerUpdate> OnPlayerUpdate = new Listener<>(event ->
    {
        setMetaData(getMetaData());
        Entity en = mc.player.getRidingEntity();
        
        if (en == null)
            return;
        
        mc.player.horseJumpPower = 0f;
        
        if (!_timer.passed(Delay.getValue() * 1000))
            return;
        
        if (mc.player.movementInput.jump && en.onGround)
            en.motionY = 0.65;
        
        // verify we have a block in our hand
        ItemStack stack = mc.player.getHeldItemMainhand();
        
        int prevSlot = -1;
        
        if (!verifyStack(stack))
        {
            for (int i = 0; i < 9; ++i)
            {
                stack = mc.player.inventory.getStackInSlot(i);
                
                if (verifyStack(stack))
                {
                    prevSlot = mc.player.inventory.currentItem;
                    mc.player.inventory.currentItem = i;
                    mc.playerController.updateController();
                    break;
                }
            }
        }
        
        if (!verifyStack(stack))
            return;
        
        _timer.reset();
        
        BlockPos toPlaceAt = null;
        
        BlockPos feetBlock = PlayerUtil.GetLocalPlayerPosFloored().down();
         
        boolean placeAtFeet = feetBlock != BlockPos.ORIGIN ? isValidPlaceBlockState(feetBlock) : false;
        
        // verify we are on tower mode, feet block is valid to be placed at, and 
        if (Mode.getValue().equals("Tower") && placeAtFeet && mc.player.movementInput.jump && _towerTimer.passed(250) && !mc.player.isElytraFlying())
        {
            // todo: this can be moved to only do it on an SPacketPlayerPosLook?
            if (_towerPauseTimer.passed(1500))
            {
                _towerPauseTimer.reset();
                en.motionY = -0.28f;
            }
            else
            {
                final float towerMotion = 0.41999998688f;
                
                en.setVelocity(0, towerMotion, 0);
            }
        }
        
        if (placeAtFeet)
            toPlaceAt = feetBlock;
        else // find a supporting position for feet block
        {
            BlockInteractionHelper.ValidResult result = BlockInteractionHelper.valid(feetBlock);
            
            // find a supporting block
            if (result != BlockInteractionHelper.ValidResult.Ok && result != BlockInteractionHelper.ValidResult.AlreadyBlockThere)
            {
                BlockPos[] array = { feetBlock.north(), feetBlock.south(), feetBlock.east(), feetBlock.west() };
                
                BlockPos toSelect = null;
                double lastDistance = 420.0;
                
                for (BlockPos pos : array)
                {
                    if (!isValidPlaceBlockState(pos))
                        continue;
                    
                    double dist = pos.getDistance((int)mc.player.posX, (int)mc.player.posY, (int)mc.player.posZ);
                    if (lastDistance > dist)
                    {
                        lastDistance = dist;
                        toSelect = pos;
                    }
                }
                
                // if we found a position, that's our selection
                if (toSelect != null)
                    toPlaceAt = toSelect;
            }
        
        }
        
        if (toPlaceAt != null)
        {
            // PositionRotation
            // CPacketPlayerTryUseItemOnBlock
            // CPacketAnimation

            final Vec3d eyesPos = new Vec3d(mc.player.posX, mc.player.posY + mc.player.getEyeHeight(), mc.player.posZ);
            
            for (final EnumFacing side : EnumFacing.values())
            {
                final BlockPos neighbor = toPlaceAt.offset(side);
                final EnumFacing side2 = side.getOpposite();

                if (mc.world.getBlockState(neighbor).getBlock().canCollideCheck(mc.world.getBlockState(neighbor), false))
                {
                    final Vec3d hitVec = new Vec3d((Vec3i) neighbor).add(0.5, 0.5, 0.5).add(new Vec3d(side2.getDirectionVec()).scale(0.5));
                    if (eyesPos.distanceTo(hitVec) <= 5.0f)
                    {
                        _rotations = BlockInteractionHelper.getFacingRotations(toPlaceAt.getX(), toPlaceAt.getY(), toPlaceAt.getZ(), side);
                        
                        // handled in a packet listener
                        //PlayerUtil.PacketFacePitchAndYaw(rotations[1], rotations[0]);
                        break;
                    }
                }
            }
            
            if (BlockInteractionHelper.place(toPlaceAt, 5.0f, false, false, true) == BlockInteractionHelper.PlaceResult.Placed)
            {
                // swinging is already in the place function.
            }
        }
        else
        {
            _rotations = null;
            _towerPauseTimer.reset();
        }
        
        // set back our previous slot
        if (prevSlot != -1)
        {
            mc.player.inventory.currentItem = prevSlot;
            mc.playerController.updateController();
        }
    });
    
    @EventHandler
    private Listener<EventPlayerMotionUpdate> onMotionUpdate = new Listener<>(event ->
    {
        if (event.isCancelled() || mc.player.inventory == null || mc.world == null)
            return;
        
        if (event.getStage() != MinecraftEvent.Stage.Pre)
            return;
        
        if (!_timer.passed(Delay.getValue() * 1000))
            return;
        
        // verify we have a block in our hand
        ItemStack stack = mc.player.getHeldItemMainhand();
        
        int prevSlot = -1;
        
        if (!verifyStack(stack))
        {
            for (int i = 0; i < 9; ++i)
            {
                stack = mc.player.inventory.getStackInSlot(i);
                
                if (verifyStack(stack))
                {
                    prevSlot = mc.player.inventory.currentItem;
                    mc.player.inventory.currentItem = i;
                    mc.playerController.updateController();
                    break;
                }
            }
        }
        
        if (!verifyStack(stack))
            return;
        
        _timer.reset();
        
        BlockPos toPlaceAt = null;
        
        BlockPos feetBlock = PlayerUtil.GetLocalPlayerPosFloored().down();
        
        boolean placeAtFeet = feetBlock != BlockPos.ORIGIN ? isValidPlaceBlockState(feetBlock) : false;
        
        // verify we are on tower mode, feet block is valid to be placed at, and 
        if (Mode.getValue().equals("Tower") && placeAtFeet && mc.player.movementInput.jump && _towerTimer.passed(250) && !mc.player.isElytraFlying())
        {
            // todo: this can be moved to only do it on an SPacketPlayerPosLook?
            if (_towerPauseTimer.passed(1500))
            {
                _towerPauseTimer.reset();
                mc.player.motionY = -0.28f;
            }
            else
            {
                final float towerMotion = 0.41999998688f;
                
                mc.player.setVelocity(0, towerMotion, 0);
            }
        }
        
        if (placeAtFeet)
            toPlaceAt = feetBlock;
        else // find a supporting position for feet block
        {
            BlockInteractionHelper.ValidResult result = BlockInteractionHelper.valid(feetBlock);
            
            // find a supporting block
            if (result != BlockInteractionHelper.ValidResult.Ok && result != BlockInteractionHelper.ValidResult.AlreadyBlockThere)
            {
                BlockPos[] array = { feetBlock.north(), feetBlock.south(), feetBlock.east(), feetBlock.west() };
                
                BlockPos toSelect = null;
                double lastDistance = 420.0;
                
                for (BlockPos pos : array)
                {
                    if (!isValidPlaceBlockState(pos))
                        continue;
                    
                    double dist = pos.getDistance((int)mc.player.posX, (int)mc.player.posY, (int)mc.player.posZ);
                    if (lastDistance > dist)
                    {
                        lastDistance = dist;
                        toSelect = pos;
                    }
                }
                
                // if we found a position, that's our selection
                if (toSelect != null)
                    toPlaceAt = toSelect;
            }
        
        }
        
        if (toPlaceAt != null)
        {
            // PositionRotation
            // CPacketPlayerTryUseItemOnBlock
            // CPacketAnimation

            final Vec3d eyesPos = new Vec3d(mc.player.posX, mc.player.posY + mc.player.getEyeHeight(), mc.player.posZ);
            
            for (final EnumFacing side : EnumFacing.values())
            {
                final BlockPos neighbor = toPlaceAt.offset(side);
                final EnumFacing side2 = side.getOpposite();

                if (mc.world.getBlockState(neighbor).getBlock().canCollideCheck(mc.world.getBlockState(neighbor), false))
                {
                    final Vec3d hitVec = new Vec3d((Vec3i) neighbor).add(0.5, 0.5, 0.5).add(new Vec3d(side2.getDirectionVec()).scale(0.5));
                    if (eyesPos.distanceTo(hitVec) <= 5.0f)
                    {
                        float[] rotations = BlockInteractionHelper.getFacingRotations(toPlaceAt.getX(), toPlaceAt.getY(), toPlaceAt.getZ(), side);
                        
                        event.cancel();
                        event.setPitch(rotations[1]);
                        event.setYaw(rotations[0]);
                        break;
                    }
                }
            }

            if (event.isCancelled())
            {
                final BlockPos finalToPlaceAt = toPlaceAt;
                final int finalPrevSlot = prevSlot;
                
                Consumer<EntityPlayerSP> post = p -> 
                {
                    if (BlockInteractionHelper.place(finalToPlaceAt, 5.0f, false, false, true) == BlockInteractionHelper.PlaceResult.Placed)
                    {
                        // swinging is already in the place function.
                    }
                    
                    // set back our previous slot
                    if (finalPrevSlot != -1)
                    {
                        mc.player.inventory.currentItem = finalPrevSlot;
                        mc.playerController.updateController();
                    }
                };
                
                event.setFunct(post);
            }
            else
                _towerPauseTimer.reset();
        }
        else
            _towerPauseTimer.reset();
        
        if (!event.isCancelled())
        {
            // set back our previous slot
            if (prevSlot != -1)
            {
                mc.player.inventory.currentItem = prevSlot;
                mc.playerController.updateController();
            }
        }
    });

    @EventHandler
    private Listener<EventServerPacket> onPlayerPosLook = new Listener<>(event ->
    {
        if (event.getStage() == Stage.Pre && event.getPacket() instanceof SPacketPlayerPosLook)
        {
            // reset this if we flagged the anticheat
            _towerTimer.reset();
            if (mc.player.movementInput.jump)
            {
                mc.player.motionY = 0.42f;
            }
        }
    });

    @EventHandler
    private Listener<EventClientPacket> onClientPacket = new Listener<>(event ->
    {
        if (event.getStage() != MinecraftEvent.Stage.Pre)
            return;
        
        if (event.getPacket() instanceof CPacketPlayer.Rotation)
        {
            if (mc.player.isRiding() && _rotations != null)
            {
                CPacketPlayer.Rotation packet = (Rotation) event.getPacket();
                
                packet.pitch = _rotations[1];
                packet.yaw = _rotations[0];
            }
        }
    });

    @EventHandler
    private Listener<EventPlayerMove> OnPlayerMove = new Listener<>(p_Event ->
    {
        if (!StopMotion.getValue())
            return;
        
        double x = p_Event.X;
        double y = p_Event.Y;
        double z = p_Event.Z;
        
        if (mc.player.onGround && !mc.player.noClip)
        {
            double increment;
            for (increment = 0.05D; x != 0.0D && isOffsetBBEmpty(x, -1.0f, 0.0D);)
            {
                if (x < increment && x >= -increment)
                {
                    x = 0.0D;
                }
                else if (x > 0.0D)
                {
                    x -= increment;
                }
                else
                {
                    x += increment;
                }
            }
            for (; z != 0.0D && isOffsetBBEmpty(0.0D, -1.0f, z);)
            {
                if (z < increment && z >= -increment)
                {
                    z = 0.0D;
                }
                else if (z > 0.0D)
                {
                    z -= increment;
                }
                else
                {
                    z += increment;
                }
            }
            for (; x != 0.0D && z != 0.0D && isOffsetBBEmpty(x, -1.0f, z);)
            {
                if (x < increment && x >= -increment)
                {
                    x = 0.0D;
                }
                else if (x > 0.0D)
                {
                    x -= increment;
                }
                else
                {
                    x += increment;
                }
                if (z < increment && z >= -increment)
                {
                    z = 0.0D;
                }
                else if (z > 0.0D)
                {
                    z -= increment;
                }
                else
                {
                    z += increment;
                }
            }
        }
        
        p_Event.X = x;
        p_Event.Y = y;
        p_Event.Z = z;
        p_Event.cancel();
    });

    private boolean isOffsetBBEmpty(double x, double y, double z)
    {
        return mc.world.getCollisionBoxes(mc.player, mc.player.getEntityBoundingBox().offset(x, y, z)).isEmpty();
    }
    
    private boolean isValidPlaceBlockState(BlockPos pos)
    {
        BlockInteractionHelper.ValidResult result = BlockInteractionHelper.valid(pos);
        
        if (result == BlockInteractionHelper.ValidResult.AlreadyBlockThere)
            return mc.world.getBlockState(pos).getMaterial().isReplaceable();
        
        return result == BlockInteractionHelper.ValidResult.Ok;
    }

    private boolean verifyStack(ItemStack stack)
    {
        return !stack.isEmpty() && stack.getItem() instanceof ItemBlock;
    }
}
