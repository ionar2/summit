package com.salhack.summit.module.movement;

import com.salhack.summit.events.MinecraftEvent;
import com.salhack.summit.events.bus.EventHandler;
import com.salhack.summit.events.bus.Listener;
import com.salhack.summit.events.network.EventClientPacket;
import com.salhack.summit.module.Module;
import com.salhack.summit.module.Value;
import net.minecraft.client.entity.EntityOtherPlayerMP;
import net.minecraft.entity.passive.EntityDonkey;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.CPacketConfirmTeleport;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.network.play.client.CPacketVehicleMove;

import java.util.LinkedList;

public class Blink extends Module
{
    public final Value<Boolean> Visualize = new Value<Boolean>("Visualize", new String[] {"FakePlayer"}, "Visualizes your body while blink is enabled", true);
    public final Value<Boolean> EntityBlink = new Value<Boolean>("EntityBlink", new String[] {"Vehicles"}, "Holds the CPacketVehicleMove", true);

    public Blink()
    {
        super("Blink", new String[]
                { "FakeLag" }, "Abuses an exploit to save up packets and teleport you", "NONE", -1, ModuleType.MOVEMENT);
    }

    private EntityOtherPlayerMP Original;
    private EntityDonkey RidingEntity;
    private LinkedList<Packet<?>> Packets = new LinkedList<>();

    @Override
    public void onEnable()
    {
        super.onEnable();
        
        if (mc.world == null)
        {
            toggle();
            return;
        }

        Packets.clear();
        Original = null;
        RidingEntity = null;

        if (Visualize.getValue())
        {
            Original = new EntityOtherPlayerMP(mc.world, mc.session.getProfile());
            Original.copyLocationAndAnglesFrom(mc.player);
            Original.rotationYaw = mc.player.rotationYaw;
            Original.rotationYawHead = mc.player.rotationYawHead;
            Original.inventory.copyInventory(mc.player.inventory);
            mc.world.addEntityToWorld(-0xFFFFF, Original);

            if (mc.player.isRiding() && mc.player.getRidingEntity() instanceof EntityDonkey)
            {
                EntityDonkey l_Original = (EntityDonkey)mc.player.getRidingEntity();

                RidingEntity = new EntityDonkey(mc.world);
                RidingEntity.copyLocationAndAnglesFrom(l_Original);
                RidingEntity.setChested(l_Original.hasChest());
                mc.world.addEntityToWorld(-0xFFFFF+1, RidingEntity);

                Original.startRiding(RidingEntity, true);
            }
        }
        
        setMetaData("0");
    }

    @Override
    public void onDisable()
    {
        super.onDisable();

        if (!Packets.isEmpty() && mc.world != null)
        {
            while (!Packets.isEmpty())
            {
                mc.getConnection().sendPacket(Packets.getFirst()); ///< front
                Packets.removeFirst(); ///< pop
            }
        }

        if (Original != null)
        {
            if (Original.isRiding())
                Original.dismountRidingEntity();

            mc.world.removeEntity(Original);
        }

        if (RidingEntity != null)
            mc.world.removeEntity(RidingEntity);
    }

    @EventHandler
    private Listener<EventClientPacket> onClientPacket = new Listener<>(event ->
    {
        if (event.getStage() != MinecraftEvent.Stage.Pre)
            return;
        
        if (event.getPacket() instanceof CPacketPlayer || event.getPacket() instanceof CPacketConfirmTeleport || (EntityBlink.getValue() && event.getPacket() instanceof CPacketVehicleMove))
        {
            event.cancel();
            Packets.add(event.getPacket());
            setMetaData(String.valueOf(Packets.size()));
        }
    });
}
