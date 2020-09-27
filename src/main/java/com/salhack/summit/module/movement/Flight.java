package com.salhack.summit.module.movement;

import com.salhack.summit.events.MinecraftEvent;
import com.salhack.summit.events.bus.EventHandler;
import com.salhack.summit.events.bus.Listener;
import com.salhack.summit.events.network.EventClientPacket;
import com.salhack.summit.events.player.EventPlayerMotionUpdate;
import com.salhack.summit.events.player.EventPlayerTravel;
import com.salhack.summit.events.player.EventPlayerUpdate;
import com.salhack.summit.module.Module;
import com.salhack.summit.module.Value;
import com.salhack.summit.util.MathUtil;
import net.minecraft.network.play.client.CPacketPlayer;

public final class Flight extends Module
{
    public final Value<String> Mode = new Value<>("Mode", new String[]
    { "M" }, "Modes of the speed to use", "Vanilla");
    public final Value<Float> Speed = new Value<Float>("Speed", new String[]
    { "" }, "Speed to use", 1.0f, 0.0f, 10.0f, 1.0f);
    public final Value<Boolean> Glide = new Value<Boolean>("Glide", new String[] {""}, "Allows the glide speed under this to function.", false);
    public final Value<Boolean> GlideWhileMoving = new Value<Boolean>("GlideWhileMoving", new String[] {""}, "If no binds are pressed, should glide be enabled?", false);
    public final Value<Float> GlideSpeed = new Value<Float>("GlideSpeed", new String[] {"GlideSpeed"}, "Glide speed of going down", 0.0f, 0.0f, 10.0f, 1.0f);
    public final Value<Boolean> ElytraOnly = new Value<Boolean>("Elytra", new String[] {""}, "Only functions while on an elytra.", false);
    public final Value<Boolean> AntiFallDmg = new Value<Boolean>("AntiFallDmg", new String[] {""}, "Prevents you from taking fall damage while flying", false);
    public final Value<Boolean> AntiKick = new Value<Boolean>("AntiKick", new String[] {""}, "Prevents you from getting kicked while flying by vanilla anticheat", true);

    public Flight()
    {
        super("Flight", new String[]
        { "Flight" }, "Allows you to fly", "NONE", 0xB49FAD, ModuleType.MOVEMENT);
        setMetaData(getMetaData());
        
        Mode.addString("Vanilla");
        Mode.addString("Creative");
    }

    public String getMetaData()
    {
        return String.valueOf(Mode.getValue());
    }

    @EventHandler
    private Listener<EventPlayerUpdate> onPlayerUpdate  = new Listener<>(event ->
    {
        setMetaData(getMetaData());
    });
    
    @EventHandler
    private Listener<EventPlayerTravel> OnTravel = new Listener<>(p_Event ->
    {
        if (mc.player == null)
            return;

        if (ElytraOnly.getValue() && !mc.player.isElytraFlying())
            return;

        if (Mode.getValue().equals("Creative"))
        {
            mc.player.setVelocity(0, 0, 0);

            final double[] dir = MathUtil.directionSpeed(Speed.getValue());

            if (mc.player.movementInput.moveStrafe != 0 || mc.player.movementInput.moveForward != 0)
            {
                mc.player.motionX = dir[0];
                mc.player.motionZ = dir[1];
            }

            if (mc.player.movementInput.jump && !mc.player.isElytraFlying())
                mc.player.motionY = Speed.getValue();

            if (mc.player.movementInput.sneak)
                mc.player.motionY = -Speed.getValue();

            if (Glide.getValue() && (GlideWhileMoving.getValue() ? (mc.player.movementInput.moveStrafe != 0 || mc.player.movementInput.moveForward != 0) : true))
            {
                mc.player.motionY += -GlideSpeed.getValue();
            }

            p_Event.cancel();

            mc.player.prevLimbSwingAmount = 0;
            mc.player.limbSwingAmount = 0;
            mc.player.limbSwing = 0;
        }
    });

    @EventHandler
    private Listener<EventPlayerMotionUpdate> OnPlayerUpdate = new Listener<>(p_Event ->
    {
        if (p_Event.getStage() != MinecraftEvent.Stage.Pre)
            return;

        if (ElytraOnly.getValue() && !mc.player.isElytraFlying())
            return;

        if (Mode.getValue().equals("Vanilla"))
        {
            mc.player.setVelocity(0, 0, 0);

            final double[] dir = MathUtil.directionSpeed(Speed.getValue());

            if (mc.player.movementInput.moveStrafe != 0 || mc.player.movementInput.moveForward != 0)
            {
                mc.player.motionX = dir[0];
                mc.player.motionZ = dir[1];
            }

            if (mc.gameSettings.keyBindJump.isKeyDown())
                mc.player.motionY = Speed.getValue();

            if (mc.gameSettings.keyBindSneak.isKeyDown())
                mc.player.motionY = -Speed.getValue();
        }

        if (AntiKick.getValue() && (mc.player.ticksExisted % 4) == 0)
            mc.player.motionY += -0.04;
    });

    @EventHandler
    private Listener<EventClientPacket> onClientPacket = new Listener<>(event ->
    {
        if (event.getStage() != MinecraftEvent.Stage.Pre)
            return;
        
        if (event.getPacket() instanceof CPacketPlayer)
        {
            if (!AntiFallDmg.getValue())
                return;

            if (mc.player.isElytraFlying())
                return;

            final CPacketPlayer l_Packet = (CPacketPlayer) event.getPacket();

            if (mc.player.fallDistance > 3.8f)
            {
                l_Packet.onGround = true;
                mc.player.fallDistance = 0.0f;
            }
        }
    });
}
