package com.salhack.summit.module.movement;

import com.salhack.summit.events.MinecraftEvent;
import com.salhack.summit.events.MinecraftEvent.Stage;
import com.salhack.summit.events.bus.EventHandler;
import com.salhack.summit.events.bus.Listener;
import com.salhack.summit.events.network.EventServerPacket;
import com.salhack.summit.events.player.EventPlayerMotionUpdate;
import com.salhack.summit.events.player.EventPlayerTravel;
import com.salhack.summit.events.player.EventPlayerUpdate;
import com.salhack.summit.main.SummitStatic;
import com.salhack.summit.module.Module;
import com.salhack.summit.module.Value;
import com.salhack.summit.util.MathUtil;
import net.minecraft.init.Items;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.network.play.client.CPacketEntityAction;
import net.minecraft.network.play.server.SPacketPlayerPosLook;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

public class ElytraFly extends Module
{
    public final Value<String> Mode = new Value<>("Mode", new String[] { "M" }, "Modes of the speed to use", "Control");
    public final Value<Float> Speed = new Value<Float>("Speed", new String[] { "" }, "Speed to use", 1.8f, 0.0f, 3.0f, 1.0f);
    public final Value<Float> GlideSpeed = new Value<Float>("GlideSpeed", new String[] { "" }, "GlideSpeed to use", 2.73f, 0.0f, 3.0f, 1.0f);
    public final Value<Float> DownSpeed = new Value<Float>("DownSpeed", new String[]
    { "DS" }, "DownSpeed multiplier for flight, higher values equals more speed.", 1.82f, 0.0f, 10.0f, 0.1f);
    public final Value<Boolean> PitchSpoof = new Value<>("PitchSpoof", new String[] {"PS"}, "Spoofs pitch as a workaround for hauses patch", true);
    public final Value<Boolean> UseTimer = new Value<>("UseTimer", new String[] {"Timer"}, "Uses timer to go faster", true);
    public final Value<Boolean> InstantTakeoff = new Value<>("InstantTakeoff", new String[] {"InstantTakeOff"}, "Instantly takes off", true);
    public final Value<Boolean> AutoAccelerate = new Value<>("AutoAccelerate", new String[] {"Accelerate"}, "(PacketMode): auto accelerates", true);
    public final Value<Boolean> InfiniteDurability = new Value<>("InfiniteDurability", new String[] {"Inf"}, "(PacketMode): Infinite durability exploit", true);
    public final Value<Boolean> NCPStrict = new Value<>("NCPStrict", new String[] {"NCPStrict"}, "(PacketMode): Allows working on a strict NCP config", false);
    
    public ElytraFly()
    {
        super("ElytraFly", new String[] {"ElytraPlus", "ElytraControl", "ElytraFlight"}, "Allows you to fully control elytras", "NONE", -1, ModuleType.MOVEMENT);
        setMetaData(getMetaData());
        
        Mode.addString("Control");
        Mode.addString("PitchControl");
        Mode.addString("JetPack");
        Mode.addString("Packet");
    }
    
    private float currentSpeed = 0.1f;
    private float currentPitchSpoof = 10f;
    private float currentOffset = 0f;
    private float lastRotationYaw = 0f;
    private boolean canAutoAccelerate = true;
    
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
    private Listener<EventPlayerTravel> OnTravel = new Listener<>(event ->
    { 
        if (mc.player == null || SummitStatic.FLIGHT.isEnabled())
            return;
        
        if (UseTimer.getValue())
            SummitStatic.TIMER.SetOverrideSpeed(1.0f);

        if (mc.player.getItemStackFromSlot(EntityEquipmentSlot.CHEST).getItem() != Items.ELYTRA)
            return;

        if (!mc.player.isElytraFlying())
            return;
        
        if (UseTimer.getValue())
            SummitStatic.TIMER.SetOverrideSpeed(1.09f);
        
        switch (Mode.getValue())
        {
            case "Control":
            {
                if (mc.player.movementInput.jump)
                {
                    float speed = (float) Math.sqrt(mc.player.motionX * mc.player.motionX + mc.player.motionZ * mc.player.motionZ);
                    
                    if (speed > 1.0f)
                        return;
                    else
                    { 
                        double[] dir = MathUtil.directionSpeedNoForward(Speed.getValue());
                        
                        mc.player.motionX = dir[0];
                        mc.player.motionY = -(GlideSpeed.getValue() / 10000f);
                        mc.player.motionZ = dir[1];
                        event.cancel();
                        return;
                    }
                }
                
                mc.player.setVelocity(0f, 0f, 0f);

                final double[] dir = MathUtil.directionSpeed(Speed.getValue());

                if (mc.player.movementInput.moveStrafe != 0f || mc.player.movementInput.moveForward != 0f)
                {
                    mc.player.motionX = dir[0];
                    mc.player.motionY = -(GlideSpeed.getValue() / 10000.0f);
                    mc.player.motionZ = dir[1];
                }

                if (mc.player.movementInput.sneak)
                    mc.player.motionY = -DownSpeed.getValue();

                event.cancel();

                mc.player.prevLimbSwingAmount = 0f;
                mc.player.limbSwingAmount = 0f;
                mc.player.limbSwing = 0f;
                break;
            }
            case "PitchControl":
            {
                final double[] dir = MathUtil.directionSpeed(Speed.getValue());
                
                if (mc.player.movementInput.moveStrafe != 0 || mc.player.movementInput.moveForward != 0)
                {
                    mc.player.motionX = dir[0];
                    mc.player.motionZ = dir[1];
                    
                    mc.player.motionX -= (mc.player.motionX*(Math.abs(mc.player.rotationPitch)+90)/90) - mc.player.motionX;
                    mc.player.motionZ -= (mc.player.motionZ*(Math.abs(mc.player.rotationPitch)+90)/90) - mc.player.motionZ;
                }
                else
                {
                    mc.player.motionX = 0;
                    mc.player.motionZ = 0;
                }
                
                mc.player.motionY = (-MathUtil.degToRad(mc.player.rotationPitch)) * mc.player.movementInput.moveForward;

                if (mc.player.movementInput.sneak)
                    mc.player.motionY = -DownSpeed.getValue();

                mc.player.prevLimbSwingAmount = 0;
                mc.player.limbSwingAmount = 0;
                mc.player.limbSwing = 0;
                event.cancel();
                break;
            }
            case "JetPack":
                event.cancel();
                mc.player.prevLimbSwingAmount = 0f;
                mc.player.limbSwingAmount = 0f;
                mc.player.limbSwing = 0f;
                boolean isMoving = mc.player.movementInput.moveStrafe != 0f || mc.player.movementInput.moveForward != 0f;

                float speed = (float) Math.sqrt(mc.player.motionX * mc.player.motionX + mc.player.motionZ * mc.player.motionZ);
                
                currentSpeed = Speed.getValue();
                //currentSpeed = AutoAccelerate.getValue() ? currentSpeed : Speed.getValue();
                
                if (mc.player.movementInput.jump)
                {
                    currentPitchSpoof = (float) (-51f);

                    // holding space
                    if (speed > 0.5f)
                    {
                        float vecf = MathHelper.cos(-mc.player.rotationYaw * 0.017453292F - (float)Math.PI);
                        float vecf1 = MathHelper.sin(-mc.player.rotationYaw * 0.017453292F - (float)Math.PI);
                        float vecf2 = -MathHelper.cos(-currentPitchSpoof * 0.017453292F);
                        float vecf3 = MathHelper.sin(-currentPitchSpoof * 0.017453292F);
                        Vec3d vec3d =  new Vec3d((double)(vecf1 * vecf2), (double)vecf3, (double)(vecf * vecf2));
                        
                        float f = currentPitchSpoof * 0.017453292F;
                        double d6 = Math.sqrt(vec3d.x * vec3d.x + vec3d.z * vec3d.z);
                        double d1 = vec3d.length();
                        float f4 = MathHelper.cos(f);
                        f4 = (float)((double)f4 * (double)f4 * Math.min(1.0D, d1 / 0.4D));
                        mc.player.motionY += -0.08D + (double)f4 * 0.06D;
                        
                        //SendMessage("Current Speed is " + speed + " f is " + f + " d6 is " + d6 + " motionY is " + mc.player.motionY);

                        /*if (mc.player.motionY < 0.0D && d6 > 0.0D)
                        {
                            double d2 = mc.player.motionY * -0.1D * (double)f4;
                            mc.player.motionY += d2;
                            mc.player.motionX += vec3d.x * d2 / d6;
                            mc.player.motionZ += vec3d.z * d2 / d6;
                        }*/

                        if (f < 0.0F)
                        {
                            double d10 = speed * (double)(-MathHelper.sin(f)) * 0.04D;
                            mc.player.motionY += d10 * 3.2D;
                            mc.player.motionX -= vec3d.x * d10 / d6;
                            mc.player.motionZ -= vec3d.z * d10 / d6;
                        }

                        /*if (d6 > 0.0D)
                        {
                            mc.player.motionX += (vec3d.x / d6 * speed - mc.player.motionX) * 0.1D;
                            mc.player.motionZ += (vec3d.z / d6 * speed - mc.player.motionZ) * 0.1D;
                        }*/
                        
                        mc.player.motionX *= 0.9900000095367432D;
                        mc.player.motionY *= 0.9800000190734863D;
                        mc.player.motionZ *= 0.9900000095367432D;
                        return;
                    }
                    else if (!isMoving)
                    {
                        double[] dir = MathUtil.directionSpeedNoForward(currentSpeed);
                        currentPitchSpoof = 10f;
                        mc.player.motionX = dir[0];
                        mc.player.motionY = -(GlideSpeed.getValue() / 10000f);
                        mc.player.motionZ = dir[1];
                        return;
                    }
                }
                else if (isMoving)
                {
                    double diff = mc.player.posY - MathHelper.floor(mc.player.posY) ;
                    
                    if (diff < 0.5)
                    {
                        currentPitchSpoof = -20f;
                        Vec3d vec3d = mc.player.getLookVec();
                        float f = currentPitchSpoof * 0.017453292F;
                        double d6 = Math.sqrt(vec3d.x * vec3d.x + vec3d.z * vec3d.z);
                        double d1 = vec3d.length();
                        float f4 = MathHelper.cos(f);
                        f4 = (float)((double)f4 * (double)f4 * Math.min(1.0D, d1 / 0.4D));
                        mc.player.motionY += -0.08D + (double)f4 * 0.06D;
                        
                        if (f < 0.0F)
                        {
                            double d10 = speed * (double)(-MathHelper.sin(f)) * 0.04D;
                            mc.player.motionY += d10 * 3.2D;
                            mc.player.motionX -= vec3d.x * d10 / d6;
                            mc.player.motionZ -= vec3d.z * d10 / d6;
                        }

                        mc.player.motionX *= 0.9900000095367432D;
                        mc.player.motionY *= 0.9800000190734863D;
                        mc.player.motionZ *= 0.9900000095367432D;
                        return;
                    }
                }
                
                currentPitchSpoof = 10f;//Math.max(mc.player.rotationPitch, 10f);
                
                // Speed for flat flying/gliding
                final double[] dir = MathUtil.directionSpeed(currentSpeed);

                if (mc.player.movementInput.moveStrafe != 0f || mc.player.movementInput.moveForward != 0f)
                {
                    mc.player.motionX = dir[0];
                    mc.player.motionY = -(GlideSpeed.getValue() / 10000.0f);
                    mc.player.motionZ = dir[1];
                    
                    lastRotationYaw = mc.player.rotationYaw;
                }
                else
                {
                    mc.player.setVelocity(0f, 0f, 0f);
                }
                
                if (mc.player.movementInput.sneak)
                    mc.player.motionY = -DownSpeed.getValue();
                
               // if (AutoAccelerate.getValue() && canAutoAccelerate && isMoving)
               //     currentSpeed += 0.001f;
                
                if (!isMoving)
                {
                    canAutoAccelerate = true;
                    currentSpeed = Speed.getValue();
                }
                break;
            default:
                break;
        }
    });

    @EventHandler
    private Listener<EventServerPacket> onPlayerPosLook = new Listener<>(event ->
    {
        if (event.getStage() == Stage.Pre && event.getPacket() instanceof SPacketPlayerPosLook)
            currentSpeed = 0.5f;
    });
    
    @Override
    public void onDisable()
    {
        super.onDisable();

        mc.player.capabilities.isFlying = false;
        SummitStatic.TIMER.SetOverrideSpeed(1.0f);
    }
    
    @EventHandler
    private Listener<EventPlayerMotionUpdate> onMotionUpdate = new Listener<>(event ->
    {
        if (event.getStage() != MinecraftEvent.Stage.Pre || event.isCancelled())
            return;
        
        // ensure we are wearing an elytra
        if (mc.player.getItemStackFromSlot(EntityEquipmentSlot.CHEST).getItem() != Items.ELYTRA)
            return;
        
        if (Mode.getValue().equals("Packet"))
        {
            mc.player.capabilities.isFlying = true;
            
            if (mc.player.movementInput.moveStrafe != 0f || mc.player.movementInput.moveForward != 0f)
            {
                if (!AutoAccelerate.getValue())
                    currentSpeed = Speed.getValue();
                
                if (mc.player.ticksExisted % 5 == 0 && AutoAccelerate.getValue())
                {
                    currentSpeed += 0.05f;
                    if (currentSpeed >= Speed.getValue())
                        currentSpeed = Speed.getValue();
                }
                
                mc.player.capabilities.setFlySpeed(currentSpeed / 10);
                
                if (NCPStrict.getValue())
                {
                    if (currentOffset >= 0.8f)
                        currentOffset = 0.0f;
                    
                    currentOffset += Math.sqrt(mc.player.motionX * mc.player.motionX + mc.player.motionZ * mc.player.motionZ) / 10000; //0.00029994;//19994;
                    mc.player.setPosition(mc.player.posX, event.getY() - currentOffset, mc.player.posZ);
                }
            }
            else
            {
                currentOffset = 0.0f;
                currentSpeed = 0.5f;
            }

            if (mc.player.movementInput.sneak)
                mc.player.motionY = -DownSpeed.getValue();
            
            // todo ? 
            if (InfiniteDurability.getValue())
                mc.player.connection.sendPacket(new CPacketEntityAction(mc.player, CPacketEntityAction.Action.START_FALL_FLYING));
            else
                mc.player.connection.sendPacket(new CPacketEntityAction(mc.player, CPacketEntityAction.Action.START_FALL_FLYING));
            
            return;
        }
        else
            mc.player.capabilities.isFlying = false;
        
        if (InstantTakeoff.getValue() && !mc.player.isInWater() && !mc.player.isElytraFlying())
        {
            if (UseTimer.getValue())
                SummitStatic.TIMER.SetOverrideSpeed(0.2f);
            
            if (mc.player.onGround)
                mc.player.motionY = 0.405f;
            else
            {
                if (mc.player.ticksExisted % 5 == 0)
                    mc.player.connection.sendPacket(new CPacketEntityAction(mc.player, CPacketEntityAction.Action.START_FALL_FLYING));
            }
        }
        
        if (!mc.player.isElytraFlying())
            return;

        boolean isMoving = mc.player.movementInput.moveStrafe != 0f || mc.player.movementInput.moveForward != 0f || mc.player.movementInput.jump;
        
        float yaw = isMoving ? mc.player.rotationYaw : lastRotationYaw;
        
        switch (Mode.getValue())
        {
            case "Control":
                if (!mc.player.movementInput.jump && PitchSpoof.getValue())
                {
                    event.setPitch(Math.max(mc.player.rotationPitch, 10f));
                    event.setYaw(yaw);
                    event.cancel();
                }
                break;
            case "JetPack":
                if (PitchSpoof.getValue())
                {
                    event.setPitch(currentPitchSpoof);
                    event.setYaw(yaw);
                    event.cancel();
                }
                break;
            default:
                break;
        }
    });
}
