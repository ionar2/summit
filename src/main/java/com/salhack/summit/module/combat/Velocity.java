package com.salhack.summit.module.combat;

import com.salhack.summit.events.MinecraftEvent;
import com.salhack.summit.events.bus.EventHandler;
import com.salhack.summit.events.bus.Listener;
import com.salhack.summit.events.network.EventServerPacket;
import com.salhack.summit.events.player.EventPlayerApplyCollision;
import com.salhack.summit.events.player.EventPlayerPushOutOfBlocks;
import com.salhack.summit.events.player.EventPlayerPushedByWater;
import com.salhack.summit.events.player.EventPlayerUpdate;
import com.salhack.summit.module.Module;
import com.salhack.summit.module.Value;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.projectile.EntityFishHook;
import net.minecraft.network.play.server.SPacketEntityStatus;
import net.minecraft.network.play.server.SPacketEntityVelocity;
import net.minecraft.network.play.server.SPacketExplosion;

public final class Velocity extends Module
{

    public final Value<Integer> horizontal_vel = new Value<Integer>("Horizontal", new String[]
    { "Horizontal_Velocity", "HVel", "HV", "HorizontalVel", "Horizontal", "H" }, "The horizontal velocity you will take.", 0, 0, 100, 1);
    public final Value<Integer> vertical_vel = new Value<Integer>("Veritcal", new String[]
    { "Vertical_Velocity", "VVel", "VV", "VerticalVel", "Vertical", "Vert", "V" }, "The vertical velocity you will take.", 0, 0, 100, 1);
    public final Value<Boolean> explosions = new Value<Boolean>("Explosions", new String[]
    { "Explosions", "Explosion", "EXP", "EX", "Expl" }, "Apply velocity modifier on explosion velocity.", true);
    public final Value<Boolean> bobbers = new Value<Boolean>("Bobbers", new String[]
    { "Bobb", "Bob", "FishHook", "FishHooks" }, "Apply velocity modifier on fishing bobber velocity.", true);
    public final Value<Boolean> NoPush = new Value<Boolean>("NoPush", new String[]
    { "AntiPush" }, "Disable collision with entities, blocks and water", true);

    public Velocity()
    {
        super("Velocity", new String[]
        { "Vel", "AntiVelocity", "Knockback", "AntiKnockback" }, "Modify the velocity you take", "NONE", 0x71848A, ModuleType.COMBAT);
        setMetaData(getMetaData());
    }

    public String getMetaData()
    {
        return String.format("H:%s%% V:%s%%", this.horizontal_vel.getValue(), this.vertical_vel.getValue());
    }

    @EventHandler
    private Listener<EventPlayerUpdate> onPlayerUpdate  = new Listener<>(event ->
    {
        setMetaData(getMetaData());
    });
    
    @EventHandler
    private Listener<EventPlayerPushOutOfBlocks> PushOutOfBlocks = new Listener<>(event ->
    {
        if (!NoPush.getValue())
            return;

        event.cancel();
    });
    
    @EventHandler
    private Listener<EventPlayerPushedByWater> PushByWater = new Listener<>(event ->
    {
        if (!NoPush.getValue())
            return;

        event.cancel();
    });

    @EventHandler
    private Listener<EventPlayerApplyCollision> ApplyCollision = new Listener<>(event ->
    {
        if (!NoPush.getValue())
            return;

        event.cancel();
    });

    @EventHandler
    private Listener<EventServerPacket> onServerPacket = new Listener<>(event ->
    {
        if (event.getStage() != MinecraftEvent.Stage.Pre)
            return;
        
        if (mc.player == null)
            return;
        
        if (event.getPacket() instanceof SPacketEntityStatus && this.bobbers.getValue())
        {
            final SPacketEntityStatus packet = (SPacketEntityStatus) event.getPacket();
            if (packet.getOpCode() == 31)
            {
                final Entity entity = packet.getEntity(Minecraft.getMinecraft().world);
                if (entity != null && entity instanceof EntityFishHook)
                {
                    final EntityFishHook fishHook = (EntityFishHook) entity;
                    if (fishHook.caughtEntity == Minecraft.getMinecraft().player)
                    {
                        event.cancel();
                    }
                }
            }
        }
        if (event.getPacket() instanceof SPacketEntityVelocity)
        {
            final SPacketEntityVelocity packet = (SPacketEntityVelocity) event.getPacket();
            if (packet.getEntityID() == mc.player.getEntityId())
            {
                if (this.horizontal_vel.getValue() == 0 && this.vertical_vel.getValue() == 0)
                {
                    event.cancel();
                    return;
                }

                if (this.horizontal_vel.getValue() != 100)
                {
                    packet.motionX = packet.motionX / 100 * this.horizontal_vel.getValue();
                    packet.motionZ = packet.motionZ / 100 * this.horizontal_vel.getValue();
                }

                if (this.vertical_vel.getValue() != 100)
                {
                    packet.motionY = packet.motionY / 100 * this.vertical_vel.getValue();
                }
            }
        }
        if (event.getPacket() instanceof SPacketExplosion && this.explosions.getValue())
        {
            final SPacketExplosion packet = (SPacketExplosion) event.getPacket();

            if (this.horizontal_vel.getValue() == 0 && this.vertical_vel.getValue() == 0)
            {
                event.cancel();
                return;
            }

            if (this.horizontal_vel.getValue() != 100)
            {
                packet.motionX = packet.motionX / 100 * this.horizontal_vel.getValue();
                packet.motionZ = packet.motionZ / 100 * this.horizontal_vel.getValue();
            }

            if (this.vertical_vel.getValue() != 100)
            {
                packet.motionY = packet.motionY / 100 * this.vertical_vel.getValue();
            }
        }
    });

}
