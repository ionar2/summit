package com.salhack.summit.module.render;

import com.salhack.summit.events.MinecraftEvent;
import com.salhack.summit.events.blocks.EventSetOpaqueCube;
import com.salhack.summit.events.client.EventClientTick;
import com.salhack.summit.events.network.EventClientPacket;
import com.salhack.summit.events.network.EventServerPacket;
import com.salhack.summit.events.player.EventPlayerMove;
import com.salhack.summit.events.player.EventPlayerUpdate;
import com.salhack.summit.module.Module;
import com.salhack.summit.module.Value;
import com.salhack.summit.util.CameraEntity;
import com.salhack.summit.util.MathUtil;
import com.salhack.summit.events.bus.EventHandler;
import com.salhack.summit.events.bus.Listener;
import net.minecraft.client.entity.EntityOtherPlayerMP;
import net.minecraft.entity.Entity;
import net.minecraft.network.play.client.*;
import net.minecraft.network.play.server.SPacketRespawn;
import net.minecraft.network.play.server.SPacketSetPassengers;
import net.minecraft.util.math.Vec3d;

public class Freecam extends Module
{   
    public final Value<Float> speed = new Value<Float>("Speed", new String[]
    { "Spd" }, "Speed of freecam flight, higher number equals quicker motion.", 1.0f, 0.0f, 10.0f, 0.1f);
    public final Value<Boolean> CancelPackes = new Value<Boolean>("Cancel Packets", new String[] {""}, "Cancels the packets, you won't be able to freely move without this.", true);
    
    public final Value<String> Mode = new Value<>("Mode", new String[] {"M"}, "Mode of freecam to use, camera allows you to watch baritone, etc", "Camera");
    
    public Freecam()
    {
        super("Freecam", new String[] {"OutOfBody"}, "Allows out of body movement", "NONE", -1, ModuleType.RENDER);
        
        Mode.addString("Camera");
        Mode.addString("Normal");
    }

    private Entity riding;
    private EntityOtherPlayerMP Camera;
    private Vec3d position;
    private float yaw;
    private float pitch;

    @Override
    public void toggleNoSave()
    {

    }
    
    @Override
    public void onEnable()
    {
        super.onEnable();

        if (mc.world == null)
            return;
        
        if (Mode.getValue().equals("Normal"))
        {
            riding = null;
            
            if (mc.player.getRidingEntity() != null)
            {
                this.riding = mc.player.getRidingEntity();
                mc.player.dismountRidingEntity();
            }
            
            Camera = new EntityOtherPlayerMP(mc.world, mc.getSession().getProfile());
            Camera.copyLocationAndAnglesFrom(mc.player);
            Camera.prevRotationYaw = mc.player.rotationYaw;
            Camera.rotationYawHead = mc.player.rotationYawHead;
            Camera.inventory.copyInventory(mc.player.inventory);
            mc.world.addEntityToWorld(-69, Camera);
            
            if (riding != null)
                Camera.startRiding(riding);
    
            this.position = mc.player.getPositionVector();
            this.yaw = mc.player.rotationYaw;
            this.pitch = mc.player.rotationPitch;
           
            mc.player.noClip = true;
        }
        else // camera
        {
            CameraEntity.setCameraState(true);
        }
    }
    
    @Override
    public void onDisable()
    {
        super.onDisable();
        
        if (mc.world == null)
            return;
        
        if (Mode.getValue().equals("Normal"))
        {
            if (this.riding != null)
            {
                if (Camera != null)
                    Camera.dismountRidingEntity();
                
                mc.player.startRiding(this.riding, true);
                riding = null;
            }
            if (this.Camera != null)
            {
                mc.world.removeEntity(this.Camera);
            }
            if (this.position != null)
            {
                mc.player.setPosition(this.position.x, this.position.y, this.position.z);
            }
            mc.player.rotationYaw = this.yaw;
            mc.player.rotationPitch = this.pitch;
            mc.player.noClip = false;
            mc.player.setVelocity(0, 0, 0);
        }
        else
            CameraEntity.setCameraState(false);
    }
    
    @EventHandler
    private Listener<EventClientTick> onTick = new Listener<>(event ->
    {
        if (Mode.getValue().equals("Camera"))
            CameraEntity.movementTick(mc.player.movementInput.sneak, mc.player.movementInput.jump);
    });

    @EventHandler
    private Listener<EventPlayerMove> OnPlayerMove = new Listener<>(p_Event ->
    {
        if (Mode.getValue().equals("Normal"))
            mc.player.noClip = true;
    });

    @EventHandler
    private Listener<EventSetOpaqueCube> OnEventSetOpaqueCube  = new Listener<>(p_Event ->
    {
        p_Event.cancel();
    });

    @EventHandler
    private Listener<EventPlayerUpdate> OnPlayerUpdate = new Listener<>(p_Event ->
    {
        if (Mode.getValue().equals("Normal"))
        {
            mc.player.noClip = true;
    
            mc.player.setVelocity(0, 0, 0);
    
            final double[] dir = MathUtil.directionSpeed(this.speed.getValue());
    
            if (mc.player.movementInput.moveStrafe != 0 || mc.player.movementInput.moveForward != 0)
            {
                mc.player.motionX = dir[0];
                mc.player.motionZ = dir[1];
            }
            else
            {
                mc.player.motionX = 0;
                mc.player.motionZ = 0;
            }
    
            mc.player.setSprinting(false);
    
            if (mc.gameSettings.keyBindJump.isKeyDown())
            {
                mc.player.motionY += this.speed.getValue();
            }
    
            if (mc.gameSettings.keyBindSneak.isKeyDown())
            {
                mc.player.motionY -= this.speed.getValue();
            }
            
            if (mc.player.isRiding())
            {
                mc.player.dismountRidingEntity();
            }
        }
    });

    @EventHandler
    private Listener<EventServerPacket> onServerPacket = new Listener<>(event ->
    {
        if (event.getStage() != MinecraftEvent.Stage.Pre)
            return;

        if (event.getPacket() instanceof SPacketRespawn)
        {
            toggle();
            return;
        }
        else if (event.getPacket() instanceof SPacketSetPassengers && Mode.getValue().equals("Normal"))
        {
            SPacketSetPassengers packet = (SPacketSetPassengers)event.getPacket();
            
            Entity en = mc.world.getEntityByID(packet.getEntityId());
            
            if (en != null)
            {
                for (int id : packet.getPassengerIds())
                {
                    if (id == mc.player.getEntityId())
                    {
                        riding = en;
                        break;
                    }
                }
            }
        }
    });

    @EventHandler
    private Listener<EventClientPacket> onClientPacket = new Listener<>(event ->
    {
        if (event.getStage() != MinecraftEvent.Stage.Pre)
            return;

        if (Mode.getValue().equals("Normal"))
        {
            if (CancelPackes.getValue())
            {
                if ((event.getPacket() instanceof CPacketUseEntity)
                        || (event.getPacket() instanceof CPacketPlayerTryUseItem)
                        || (event.getPacket() instanceof CPacketPlayerTryUseItemOnBlock)
                        || (event.getPacket() instanceof CPacketPlayer)
                        || (event.getPacket() instanceof CPacketVehicleMove)
                        || (event.getPacket() instanceof CPacketChatMessage))
                {
                    event.cancel();
                }
            }
        }
        else if (Mode.getValue().equals("Camera"))
        {
            if (event.getPacket() instanceof CPacketUseEntity)
            {
                CPacketUseEntity packet = (CPacketUseEntity) event.getPacket();
                
                if (packet.getEntityFromWorld(mc.world) == mc.player)
                    event.cancel();
            }
        }
    });
}
