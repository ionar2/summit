package com.salhack.summit.module.combat;

import java.util.Comparator;

import javax.annotation.Nullable;

import com.salhack.summit.events.MinecraftEvent;
import com.salhack.summit.events.network.EventClientPacket;
import com.salhack.summit.events.player.EventPlayerMotionUpdate;
import com.salhack.summit.events.player.EventPlayerUpdate;
import com.salhack.summit.managers.FriendManager;
import com.salhack.summit.managers.RotationManager;
import com.salhack.summit.managers.TickRateManager;
import com.salhack.summit.module.Module;
import com.salhack.summit.module.Value;
import com.salhack.summit.util.MathUtil;
import com.salhack.summit.util.Timer;
import com.salhack.summit.util.entity.EntityUtil;
import com.salhack.summit.events.bus.EventHandler;
import com.salhack.summit.events.bus.Listener;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.EntityShulker;
import net.minecraft.entity.passive.AbstractHorse;
import net.minecraft.entity.passive.EntityTameable;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityFireball;
import net.minecraft.entity.projectile.EntityShulkerBullet;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.network.play.client.CPacketPlayer.Rotation;
import net.minecraft.util.EnumHand;

public class Aura extends Module
{
    public final Value<String> Mode = new Value<>("Mode", new String[] {"Mode"}, "The KillAura Mode to use", "Closest");
    public final Value<Float> Distance = new Value<>("Distance", new String[] {"Range"}, "Range for attacking a target", 5.0f, 0.0f, 10.0f, 1.0f);
    public final Value<Float> WallsRange = new Value<>("WallsRange", new String[] {"Wall"}, "Range for attacking a target", 5.0f, 0.0f, 10.0f, 1.0f);
    public final Value<Boolean> HitDelay = new Value<>("Hit Delay", new String[] {"Hit Delay"}, "Use vanilla hit delay", true);
    public final Value<Boolean> TpsSync = new Value<>("TpsSync", new String[] {"TPS"}, "Tps syncs the HitDelay", false);
    public final Value<String> SwitchMode = new Value<>("SwitchMode", new String[] {"AutoSwitch"}, "Different types of switch modes", "Auto");
    public final Value<Boolean> Players = new Value<Boolean>("Players", new String[] {"Players"}, "Should we target Players", true);
    public final Value<Boolean> Monsters = new Value<Boolean>("Monsters", new String[] {"Players"}, "Should we target Monsters", true);
    public final Value<Boolean> Neutrals = new Value<Boolean>("Neutrals", new String[] {"Neutral"}, "Should we target Neutrals", false);
    public final Value<Boolean> Animals = new Value<Boolean>("Animals", new String[] {"Cows"}, "Should we target Animals", false);
    public final Value<Boolean> Tamed = new Value<Boolean>("Tamed", new String[] {"Tame"}, "Should we target Tamed", false);
    public final Value<Boolean> Shulker = new Value<Boolean>("Shulker", new String[] {"Shulker"}, "Should we target Shulker", false);
    public final Value<Boolean> Projectiles = new Value<Boolean>("Projectile", new String[] {"Projectile"}, "Should we target Projectiles (shulker bullets, etc)", false);
    
    public Aura()
    {
        super("Aura", new String[] {"KillAura, Forcefield"}, "Automatically attacks enemies in range", "NONE", 0xFA2601, ModuleType.COMBAT);
        setMetaData(String.valueOf(Mode.getValue()));
        
        Mode.addString("Closest");
        Mode.addString("Priority");
        Mode.addString("Switch");
        Mode.addString("LowestHP");
        
        SwitchMode.addString("None");
        SwitchMode.addString("Auto");
        SwitchMode.addString("OnlySword");
    }
    
    private Entity _lastTarget = null;
    private Timer _rotationEndTimer = new Timer();
    private float[] _rotations;
    private Timer critTimer = new Timer();
    
    @EventHandler
    private Listener<EventPlayerUpdate> onPlayerUpdate = new Listener<>(event ->
    {
        setMetaData(String.valueOf(Mode.getValue()));

        switch (SwitchMode.getValue())
        {
            case "OnlySword":
                if (!(mc.player.getHeldItemMainhand().getItem() instanceof ItemSword))
                {
                    _rotations = null;
                    RotationManager.Get().resetRotations();
                    return;
                }
                break;
            default:
                break;
        }

        if (mc.player.isRiding())
        {
            Entity entity = getTargetEntity();

            if (entity != null && entity != mc.player.getRidingEntity() && isAttackReady())
            {
                autoSwitchIfEnabled();

                _rotations = MathUtil.calcAngle(mc.player.getPositionEyes(mc.getRenderPartialTicks()), entity.getPositionEyes(mc.getRenderPartialTicks()).subtract(0, 0.6, 0));
                _rotationEndTimer.reset();

                mc.playerController.attackEntity(mc.player, entity);
                mc.player.swingArm(EnumHand.MAIN_HAND);
            }
        }

        if (_rotationEndTimer.passed(1000) && _rotations != null)
        {
            _rotationEndTimer.reset();
            _rotations = null;
        }
    });
    
    // used for custom rotation packets, if we are riding this isn't called.
    @EventHandler
    private Listener<EventPlayerMotionUpdate> onMotionUpdate = new Listener<>(event ->
    {
        if (event.getStage() != MinecraftEvent.Stage.Pre || event.isCancelled())
            return;

        switch (SwitchMode.getValue())
        {
            case "OnlySword":
                if (!(mc.player.getHeldItemMainhand().getItem() instanceof ItemSword))
                {
                    _rotations = null;
                    RotationManager.Get().resetRotations();
                    return;
                }
                break;
            default:
                break;
        }
        
        Entity entity = getTargetEntity();
        
        if (entity != null)
        {
            if (_rotations != null)
            {
                event.cancel();
                event.setPitch(_rotations[1]);
                event.setYaw(_rotations[0]);
            }
            
            /*if (critTimer.passed(300) && mc.player.onGround && SummitStatic.CRITICALS.isEnabled() && SummitStatic.CRITICALS.Mode.getValue() == Criticals.Modes.Aura) // criticals
            {
                critTimer.reset();
                event.cancel();
                event.setOnGround(false);
                return;
            }*/
            
            if (isAttackReady() && entity != null)
            {
                autoSwitchIfEnabled();

                boolean didCrit = critTimer.getTime() == System.currentTimeMillis();
                
                event.cancel();
                
                _rotations = MathUtil.calcAngle(mc.player.getPositionEyes(mc.getRenderPartialTicks()), entity.getPositionEyes(mc.getRenderPartialTicks()).subtract(0, 0.6, 0));
                _rotationEndTimer.reset();
                
                event.setPitch(_rotations[1]);
                event.setYaw(_rotations[0]);
                
                if (didCrit)
                {
                    boolean prev = mc.player.onGround;
                    float prevDist = mc.player.fallDistance;
                    
                    mc.player.fallDistance = 0.1f;
                    mc.player.onGround = false;
                    mc.playerController.attackEntity(mc.player, entity);
                    mc.player.fallDistance = prevDist;
                    mc.player.onGround = prev;
                }
                else
                    mc.playerController.attackEntity(mc.player, entity);

                mc.player.swingArm(EnumHand.MAIN_HAND);
            }
        }
    });

    @EventHandler
    private Listener<EventClientPacket> onClientPacket = new Listener<>(event ->
    {
        if (event.getStage() != MinecraftEvent.Stage.Pre)
            return;
        
        if (event.getPacket() instanceof CPacketPlayer.Rotation && _rotations != null && mc.player.isRiding())
        {
            CPacketPlayer.Rotation packet = (Rotation) event.getPacket();

            RotationManager.Get().setRotations(_rotations[0], _rotations[1]);
            packet.pitch = _rotations[1];
            packet.yaw = _rotations[0];
        }
    });
    
    private boolean isValidTarget(Entity e, @Nullable Entity toExclude)
    {
        if (e == toExclude || mc.player == e || FriendManager.Get().IsFriend(e))
            return false;
        
        if (!(e instanceof EntityLivingBase))
        {
            boolean isProj = (e instanceof EntityShulkerBullet || e instanceof EntityFireball);
            
            if (!isProj)
                return false;
            
            if (isProj && !Projectiles.getValue())
                return false;
        }
        
        if (e instanceof EntityPlayer && !Players.getValue())
            return false;
        
        if (EntityUtil.isHostileMob(e) && !Monsters.getValue())
            return false;
        
        if (EntityUtil.isNeutralMob(e) && !Neutrals.getValue())
            return false;
        
        if (EntityUtil.isPassive(e))
        {
            boolean skipCheck = false;
            
            if (e instanceof EntityTameable)
            {
                skipCheck = true;
                
                if (((EntityTameable)e).isTamed() && !Tamed.getValue())
                    return false;
            }
            else if (e instanceof AbstractHorse)
            {
                skipCheck = true;
                
                if (((AbstractHorse)e).isTame() && !Tamed.getValue())
                    return false;
            }
            
            if (!skipCheck)
            {
                if (!Animals.getValue())
                    return false;
            }
        }
        
        if (e instanceof EntityShulker && !Shulker.getValue())
            return false;
        
        if (e instanceof EntityLivingBase)
        {
            EntityLivingBase base = (EntityLivingBase) e;
            
            if (base.getHealth()+base.getAbsorptionAmount() <= 0.0)
                return false;
        }
        
        float dist = e.getDistance(mc.player);
        
        return !e.isDead && dist <= Distance.getValue() && mc.player.canEntityBeSeen(e) ? true : dist <= WallsRange.getValue();
    }
    public void autoSwitchIfEnabled()
    {
        if (SwitchMode.getValue().equals("Auto"))
        {
            if (!(mc.player.getHeldItemMainhand().getItem() instanceof ItemSword))
            {
                for (int i = 0; i < 9; ++i)
                {
                    ItemStack stack = mc.player.inventory.getStackInSlot(i);
                    if (!stack.isEmpty())
                    {
                        if (stack.getItem() instanceof ItemSword)
                        {
                            mc.player.inventory.currentItem = i;
                            mc.playerController.updateController();
                            break;
                        }
                    }
                }
            }
        }
    }

    private Entity getTargetEntity()
    {
        Entity who = _lastTarget;
        
        switch (Mode.getValue())
        {
            case "Closest":
                who = mc.world.loadedEntityList.stream()
                    .filter(e -> isValidTarget(e, null))
                    .min(Comparator.comparing(e -> mc.player.getDistance(e)))
                    .orElse(null);
                break;
            case "Priority":
                if (who == null)
                {
                    who = mc.world.loadedEntityList.stream()
                            .filter(e -> isValidTarget(e, null))
                            .min(Comparator.comparing(e -> mc.player.getDistance(e)))
                            .orElse(null);
                }
                break;
            case "Switch":
                who = mc.world.loadedEntityList.stream()
                    .filter(e -> isValidTarget(e, isAttackReady() ? _lastTarget : null))
                    .min(Comparator.comparing(e -> mc.player.getDistance(e)))
                    .orElse(null);
                
                if (who == null)
                    who = _lastTarget;
                break;
            case "LowestHP":
                who = mc.world.loadedEntityList.stream()
                    .filter(e -> isValidTarget(e, null))
                    .map(e -> (EntityLivingBase)e)
                    .min(Comparator.comparing(e -> ((EntityLivingBase)e).getHealth()))
                    .orElse(null);
                break;
            default:
                break;
            
        }
        
        if (who != _lastTarget)
            _lastTarget = who;
        
        return who;
    }
    
    private boolean isAttackReady()
    {
        final float ticks = 20.0f - TickRateManager.Get().getTickRate();

        return HitDelay.getValue() ? (mc.player.getCooledAttackStrength(TpsSync.getValue() ? -ticks : 0f) >= 1) : true;
    }
}
