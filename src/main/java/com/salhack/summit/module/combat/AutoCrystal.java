package com.salhack.summit.module.combat;

import com.salhack.summit.events.MinecraftEvent;
import com.salhack.summit.events.client.EventClientTick;
import com.salhack.summit.events.entity.EventEntityRemoved;
import com.salhack.summit.events.network.EventServerPacket;
import com.salhack.summit.events.player.EventPlayerMotionUpdate;
import com.salhack.summit.events.player.EventPlayerUpdate;
import com.salhack.summit.events.render.RenderEvent;
import com.salhack.summit.main.SummitStatic;
import com.salhack.summit.main.Wrapper;
import com.salhack.summit.managers.FriendManager;
import com.salhack.summit.module.Module;
import com.salhack.summit.module.Value;
import com.salhack.summit.util.CrystalUtils;
import com.salhack.summit.util.Timer;
import com.salhack.summit.util.entity.EntityUtil;
import com.salhack.summit.util.entity.PlayerUtil;
import com.salhack.summit.util.render.RenderUtil;
import com.salhack.summit.events.bus.EventHandler;
import com.salhack.summit.events.bus.Listener;
import com.salhack.summit.util.CrystalUtils2;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityEnderCrystal;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.init.MobEffects;
import net.minecraft.init.SoundEvents;
import net.minecraft.inventory.ClickType;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;
import net.minecraft.item.ItemTool;
import net.minecraft.network.play.client.CPacketPlayerTryUseItemOnBlock;
import net.minecraft.network.play.server.SPacketSoundEffect;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

import static org.lwjgl.opengl.GL11.*;

public class AutoCrystal extends Module
{
    public static final Value<String> BreakMode = new Value<>("BreakMode", new String[] {"BM"}, "Mode of breaking to use", "Always");
    public static final Value<String> PlaceMode = new Value<>("PlaceMode", new String[] {"BM"}, "Mode of placing to use", "Most");
    public static final Value<Float> PlaceRadius = new Value<Float>("PlaceRadius", new String[] {""}, "Radius for placing", 4.0f, 0.0f, 5.0f, 0.5f);
    public static final Value<Float> BreakRadius = new Value<Float>("BreakRadius", new String[] {""}, "Radius for BreakRadius", 4.0f, 0.0f, 5.0f, 0.5f);
    public static final Value<Float> WallsRangePlace = new Value<Float>("WallsRangePlace", new String[] {""}, "Max distance through walls", 3.5f, 0.0f, 5.0f, 0.5f);
    public static final Value<Float> WallsRangeBreak = new Value<Float>("WallsRangeBreak", new String[] {""}, "Max distance through walls", 3.5f, 0.0f, 5.0f, 0.5f);
    public static final Value<Boolean> MultiPlace = new Value<Boolean>("MultiPlace", new String[] {"MultiPlaces"}, "Tries to multiplace", false);
    public static final Value<Integer> Ticks = new Value<Integer>("Ticks", new String[] {"IgnoreTicks"}, "The number of ticks to ignore on client update", 2, 0, 20, 1);
    
    public static final Value<Float> MinDMG = new Value<Float>("MinDMG", new String[] {""}, "Minimum damage to do to your opponent", 4.0f, 0.0f, 20.0f, 1f);
    public static final Value<Float> MaxSelfDMG = new Value<Float>("MaxSelfDMG", new String[] {""}, "Max self dmg for breaking crystals that will deal tons of dmg", 4.0f, 0.0f, 20.0f, 1.0f);
    public static final Value<Float> FacePlace = new Value<Float>("FacePlace", new String[] {""}, "Required target health for faceplacing", 8.0f, 0.0f, 20.0f, 0.5f);
    public static final Value<String> SwapMode = new Value<>("Swap", new String[] {""}, "Swap Mode", "Hotbar");
    public static final Value<Boolean> PauseIfHittingBlock = new Value<Boolean>("PauseIfHittingBlock", new String[] {""}, "Pauses when your hitting a block with a pickaxe", false);
    public static final Value<Boolean> PauseWhileEating = new Value<Boolean>("PauseWhileEating", new String[] {"PauseWhileEating"}, "Pause while eating", false);
    public static final Value<Boolean> NoSuicide = new Value<Boolean>("NoSuicide", new String[] {"NS"}, "Doesn't commit suicide/pop if you are going to take fatal damage from self placed crystal", true);
    public static final Value<Boolean> AntiWeakness = new Value<Boolean>("AntiWeakness", new String[] {"AW"}, "Switches to a sword to try and break crystals", true);
    
    public static final Value<Boolean> Players = new Value<>("Players", new String[] {"Players"}, "Target players", true);
    public static final Value<Boolean> Hostile = new Value<>("Hostile", new String[] {"Hostile", "Withers"}, "Target hostile", false);
    
    public static final Value<Boolean> Render = new Value<Boolean>("Render", new String[] {"Render"}, "Allows for rendering of block placements", true);
    public static final Value<Integer> Red = new Value<Integer>("Red", new String[] {"Red"}, "Red for rendering", 0x33, 0, 255, 5);
    public static final Value<Integer> Green = new Value<Integer>("Green", new String[] {"Green"}, "Green for rendering", 0xFF, 0, 255, 5);
    public static final Value<Integer> Blue = new Value<Integer>("Blue", new String[] {"Blue"}, "Blue for rendering", 0xF3, 0, 255, 5);
    public static final Value<Integer> Alpha = new Value<Integer>("Alpha", new String[] {"Alpha"}, "Alpha for rendering", 0x99, 0, 255, 5);
    
    public AutoCrystal()
    {
        super("AutoCrystal", new String[] {"CrystalAura"}, "Automatically places and destroys crystals", "NONE", 0xE22200, ModuleType.COMBAT);
        
        BreakMode.addString("Always");
        BreakMode.addString("Smart");
        BreakMode.addString("OnlyOwn");
        
        PlaceMode.addString("Most");
        PlaceMode.addString("Lethal");
        
        SwapMode.addString("Hotbar");
        SwapMode.addString("Inventory");
        SwapMode.addString("GoBack");
        SwapMode.addString("None");
    }
    
    public static Timer _removeVisualTimer = new Timer();
    private Timer _rotationResetTimer = new Timer();
    private ConcurrentLinkedQueue<BlockPos> _placedCrystals = new ConcurrentLinkedQueue<>();
    private ConcurrentHashMap<BlockPos, Float> _placedCrystalsDamage = new ConcurrentHashMap<>();
    private double[] _rotations = null;
    private ConcurrentHashMap<EntityEnderCrystal, Integer> _attackedEnderCrystals = new ConcurrentHashMap<>();
    private final Minecraft mc = Minecraft.getMinecraft();
    private String _lastTarget = null;
    private int _remainingTicks;
    private BlockPos _lastPlaceLocation = BlockPos.ORIGIN;
    private int prevSlot = -1;
    
    // don't allow this to load enabled on startup
    @Override
    public void toggleNoSave()
    {
        
    }
    
    @Override
    public void onEnable()
    {
        super.onEnable();
        
        // clear placed crystals, we don't want to display them later on
        _placedCrystals.clear();
        _placedCrystalsDamage.clear();
        
        // also reset ticks on enable, we need as much speed as we can get.
        _remainingTicks = 0;
        
        // reset this, we will get a new one
        _lastPlaceLocation = BlockPos.ORIGIN;
    }
    
    @EventHandler
    private Listener<EventEntityRemoved> OnEntityRemove = new Listener<>(event ->
    {
        if (event.GetEntity() instanceof EntityEnderCrystal)
        {
            // we don't need null things in this list.
            _attackedEnderCrystals.remove((EntityEnderCrystal)event.GetEntity());
        }
    });

    private boolean ValidateCrystal(EntityEnderCrystal e)
    {
        if (e == null || e.isDead)
            return false;
        
        if (_attackedEnderCrystals.containsKey(e) && _attackedEnderCrystals.get(e) > 15)
            return false;
        
        if (e.getDistance(mc.player) > (!mc.player.canEntityBeSeen(e) ? WallsRangeBreak.getValue() : BreakRadius.getValue()))
            return false;
        
        switch (BreakMode.getValue())
        {
            case "OnlyOwn":
            {
                for (BlockPos pos : _placedCrystals)
                {
                    if (e.getDistance(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5) <= 3)
                        return true;
                }
            }
            case "Smart":
                float selfDamage = CrystalUtils.calculateDamage(mc.world, e.posX, e.posY, e.posZ, mc.player, 0);
                
                if (selfDamage > MaxSelfDMG.getValue())
                    return false;
                
                if (NoSuicide.getValue() && selfDamage >= mc.player.getHealth()+mc.player.getAbsorptionAmount())
                    return false;

                // iterate through all players, and crystal positions to find the best position for most damage
                for (Entity en : mc.world.loadedEntityList)
                {
                    if (!(en instanceof EntityLivingBase))
                        continue;
                    
                    EntityLivingBase living = (EntityLivingBase) en;
                    
                    if (living instanceof EntityPlayer && !Players.getValue())
                        continue;
                    else if (EntityUtil.isHostileMob(living) && !Hostile.getValue())
                        continue;
                    
                    if (!(living instanceof EntityPlayer) && !EntityUtil.isHostileMob(living))
                        continue;
                    
                    // Ignore if the player is us, a friend, dead, or has no health (the dead variable is sometimes delayed)
                    if (living == mc.player || FriendManager.Get().IsFriend(living) || living.isDead || (living.getHealth() + living.getAbsorptionAmount()) <= 0.0f)
                        continue;
                    
                    // store this as a variable for faceplace per player
                    float minDamage = MinDMG.getValue();
                    
                    // check if players health + gap health is less than or equal to faceplace, then we activate faceplacing
                    if (living.getHealth() + living.getAbsorptionAmount() <= FacePlace.getValue())
                        minDamage = 1f;
                    
                    float calculatedDamage = CrystalUtils.calculateDamage(mc.world,  e.posX, e.posY, e.posZ, living, 0);
                    
                    if (calculatedDamage > minDamage)
                        return true;
                }
                return false;
            case "Always":
            default:
                break;
        }
        
        return true;
    }
    
    /*
     * Returns nearest crystal to an entity, if the crystal is not null or dead
     * @entity - entity to get smallest distance from
     */
    public EntityEnderCrystal GetNearestCrystalTo(Entity entity)
    {
        return mc.world.getLoadedEntityList().stream().filter(e -> e instanceof EntityEnderCrystal && ValidateCrystal((EntityEnderCrystal)e)).map(e -> (EntityEnderCrystal)e).min(Comparator.comparing(e -> entity.getDistance(e))).orElse(null);
    }
    
    public void AddAttackedCrystal(EntityEnderCrystal crystal)
    {
        if (_attackedEnderCrystals.containsKey(crystal))
        {
            int value = _attackedEnderCrystals.get(crystal);
            _attackedEnderCrystals.put(crystal, value + 1);
        }
        else
            _attackedEnderCrystals.put(crystal, 1);
    }
    
    public static boolean VerifyCrystalBlocks(final Minecraft mc, BlockPos pos)
    {
        // check distance
        if (mc.player.getDistanceSq(pos) > PlaceRadius.getValue()*PlaceRadius.getValue())
            return false;
        
        // check walls range
        if (WallsRangePlace.getValue() > 0)
        {
            if (!PlayerUtil.CanSeeBlock(pos))
            {
                double dist = pos.getDistance((int)mc.player.posX, (int)mc.player.posY, (int)mc.player.posZ);
                if (dist > WallsRangePlace.getValue())
                    return false;
            }
        }
        
        // check self damage
        float selfDamage = CrystalUtils.calculateDamage(mc.world, pos.getX() + 0.5, pos.getY() + 1.0, pos.getZ() + 0.5, mc.player, 0);
        
        // make sure self damage is not greater than maxselfdamage
        if (selfDamage > MaxSelfDMG.getValue())
            return false;

        // no suicide, verify self damage won't kill us
        if (NoSuicide.getValue() && selfDamage >= mc.player.getHealth()+mc.player.getAbsorptionAmount())
            return false;
        
        // it's an ok position.
        return true;
    }
    
    @EventHandler
    private Listener<EventClientTick> OnClientTick = new Listener<>(event ->
    {
        // this is our 1 second timer to remove our attackedEnderCrystals list, and remove the first placedCrystal for the visualizer.
        if (_removeVisualTimer.passed(1000))
        {
            _removeVisualTimer.reset();
            
            if (!_placedCrystals.isEmpty())
            {
                BlockPos removed = _placedCrystals.remove();
                
                if (removed != null)
                    _placedCrystalsDamage.remove(removed);
            }
            
            _attackedEnderCrystals.clear();
        }

        if (NeedPause())
        {
            _remainingTicks = 0;
            return;
        }

        // override
        if (PlaceMode.getValue().equals("Lethal") && _lastPlaceLocation != BlockPos.ORIGIN)
        {
            float damage = 0f;
            
            EntityPlayer trappedTarget = null;
            
            // verify that this location will exceed lethal damage for atleast one enemy.
            // iterate through all players, and crystal positions to find the best position for most damage
            for (EntityPlayer player : mc.world.playerEntities)
            {
                // Ignore if the player is us, a friend, dead, or has no health (the dead variable is sometimes delayed)
                if (player == mc.player || FriendManager.Get().IsFriend(player) || mc.player.isDead || (mc.player.getHealth() + mc.player.getAbsorptionAmount()) <= 0.0f)
                    continue;
                
                // store this as a variable for faceplace per player
                float minDamage = MinDMG.getValue();
                
                // check if players health + gap health is less than or equal to faceplace, then we activate faceplacing
                if (player.getHealth() + player.getAbsorptionAmount() <= FacePlace.getValue())
                    minDamage = 1f;
                
                float calculatedDamage = CrystalUtils.calculateDamage(mc.world, _lastPlaceLocation.getX() + 0.5, _lastPlaceLocation.getY() + 1.0, _lastPlaceLocation.getZ() + 0.5, player, 0);
                
                if (calculatedDamage >= minDamage && calculatedDamage > damage)
                {
                    damage = calculatedDamage;
                    trappedTarget = player;
                }
            }
            
            if (damage == 0f || trappedTarget == null)
            {
                // set this back to null
                _lastPlaceLocation = BlockPos.ORIGIN;
            }
        }
        
        
        if (_remainingTicks > 0)
        {
            --_remainingTicks;
        }
        
        boolean skipUpdateBlocks = _lastPlaceLocation != BlockPos.ORIGIN && PlaceMode.getValue().equals("Lethal");

        // create a list of available place locations
        ArrayList<BlockPos> placeLocations = new ArrayList<BlockPos>();
        EntityLivingBase livingTarget = null;
        
        // if we don't need to skip update, get crystal blocks
        if (!skipUpdateBlocks && _remainingTicks <= 0)
        {
            _remainingTicks = Ticks.getValue();
            
            // this is the most expensive code, we need to get valid crystal blocks. -> todo verify stream to see if it's slower than normal looping.
            final List<BlockPos> cachedCrystalBlocks = CrystalUtils2.crystalBlocks;//CrystalUtils.findCrystalBlocks(mc.player, PlaceRadius.getValue()).stream().filter(pos -> VerifyCrystalBlocks(pos)).collect(Collectors.toList());
            
            // this is where we will iterate through all players (for most damage) and cachedCrystalBlocks
            if (!cachedCrystalBlocks.isEmpty())
            {
                float damage = 0f;
                String target = null;
                
                // iterate through all players, and crystal positions to find the best position for most damage
                for (Entity e : mc.world.loadedEntityList)
                {
                    if (!(e instanceof EntityLivingBase))
                        continue;

                    EntityLivingBase living = (EntityLivingBase) e;
                    
                    if (living instanceof EntityPlayer && !Players.getValue())
                        continue;
                    else if (EntityUtil.isHostileMob(living) && !Hostile.getValue())
                        continue;
                    
                    if (!(living instanceof EntityPlayer) && !EntityUtil.isHostileMob(living))
                        continue;
                    
                    // Ignore if the player is us, a friend, dead, or has no health (the dead variable is sometimes delayed)
                    if (living == mc.player || FriendManager.Get().IsFriend(living) || mc.player.isDead || (mc.player.getHealth() + mc.player.getAbsorptionAmount()) <= 0.0f)
                        continue;
                    
                    // store this as a variable for faceplace per player
                    float minDamage = MinDMG.getValue();
                    
                    // check if players health + gap health is less than or equal to faceplace, then we activate faceplacing
                    if (living.getHealth() + living.getAbsorptionAmount() <= FacePlace.getValue())
                        minDamage = 1f;
                    
                    // iterate through all valid crystal blocks for this player, and calculate the damages.
                    for (BlockPos pos : cachedCrystalBlocks)
                    {
                        float calculatedDamage = CrystalUtils.calculateDamage(mc.world, pos.getX() + 0.5, pos.getY() + 1.0, pos.getZ() + 0.5, living, 0);
                        
                        if (calculatedDamage >= minDamage && calculatedDamage > damage)
                        {
                            damage = calculatedDamage;
                            if (!placeLocations.contains(pos))
                                placeLocations.add(pos);
                            target = living.getName();
                            livingTarget = living;
                        }
                    }
                }
                
                // playerTarget can nullptr during client tick
                if (livingTarget != null)
                {
                    // the player could have died during this code run, wait till next tick for doing more calculations.
                    if (livingTarget.isDead || livingTarget.getHealth() <= 0.0f)
                        return;
                    
                    // ensure we have place locations
                    if (!placeLocations.isEmpty())
                    {
                        // store this as a variable for faceplace per player
                        float minDamage = MinDMG.getValue();
                        
                        // check if players health + gap health is less than or equal to faceplace, then we activate faceplacing
                        if (livingTarget.getHealth() + livingTarget.getAbsorptionAmount() <= FacePlace.getValue())
                            minDamage = 1f;
                        
                        final float finalMinDamage = minDamage;
                        final EntityLivingBase finalTarget = livingTarget;
                        
                        // iterate this again, we need to remove some values that are useless, since we iterated all players
                        placeLocations.removeIf(pos -> CrystalUtils.calculateDamage(mc.world, pos.getX() + 0.5, pos.getY() + 1.0, pos.getZ() + 0.5, finalTarget, 0) < finalMinDamage);
                        
                        // at this point, the place locations list is in asc order, we need to reverse it to get to desc
                        Collections.reverse(placeLocations);
                    
                        // store our last target name.
                        _lastTarget = target;
                    }
                }
            }
        }
        
        // at this point, we are going to destroy/place crystals.
        
        // Get nearest crystal to the player, we will need to null check this on the timer.
        EntityEnderCrystal crystal = GetNearestCrystalTo(mc.player);
        
        // get a valid crystal in range, and check if it's in break radius
        boolean isValidCrystal = crystal != null ? mc.player.getDistance(crystal) < BreakRadius.getValue() : false;
        
        // no where to place or break
        if (!isValidCrystal && placeLocations.isEmpty() && !skipUpdateBlocks)
        {
            _remainingTicks = 0;
            return;
        }
        
        if (isValidCrystal && (skipUpdateBlocks ? true : _remainingTicks == Ticks.getValue())) // we are checking null here because we don't want to waste time not destroying crystals right away
        {
            if (AntiWeakness.getValue() && mc.player.isPotionActive(MobEffects.WEAKNESS) && !mc.player.isPotionActive(MobEffects.STRENGTH))
            {
                if (mc.player.getHeldItemMainhand() == ItemStack.EMPTY || (!(mc.player.getHeldItemMainhand().getItem() instanceof ItemSword) && !(mc.player.getHeldItemMainhand().getItem() instanceof ItemTool)))
                {
                    for (int i = 0; i < 9; ++i)
                    {
                        ItemStack stack = mc.player.inventory.getStackInSlot(i);
                        
                        if (stack.isEmpty())
                            continue;
                        
                        if (stack.getItem() instanceof ItemTool || stack.getItem() instanceof ItemSword)
                        {
                            mc.player.inventory.currentItem = i;
                            mc.playerController.updateController();
                            break;
                        }
                    }
                }
            }

            double y = crystal.posY - 0.5;
            
            /*if (!mc.player.canEntityBeSeen(crystal))
            {
                SendMessage("Crystal is out of LOS! " + crystal.getDistance(mc.player));
            }
            else
            {
                SendMessage("in LOS!");
                y -= 0.5;
            }*/

            // swing arm and attack the entity
            mc.playerController.attackEntity(mc.player, crystal);
            mc.player.swingArm(EnumHand.MAIN_HAND);
            AddAttackedCrystal(crystal);

            // if we are not multiplacing return here, we have something to do for this tick.
            if (!MultiPlace.getValue())
                return;
        }
        
        // verify the placeTimer is ready, selectedPosition is not 0,0,0 and the event isn't already cancelled
        if (!placeLocations.isEmpty() || skipUpdateBlocks)
        {
            // auto switch
            switch (SwapMode.getValue())
            {
                case "GoBack":
                    if (mc.player.getHeldItemOffhand().getItem() != Items.END_CRYSTAL)
                    {
                        if (mc.player.getHeldItemMainhand().getItem() != Items.END_CRYSTAL)
                        {
                            for (int i = 0; i < 9; ++i)
                            {
                                ItemStack stack = mc.player.inventory.getStackInSlot(i);
                                
                                if (!stack.isEmpty() && stack.getItem() == Items.END_CRYSTAL)
                                {
                                    prevSlot = mc.player.inventory.currentItem;
                                    mc.player.inventory.currentItem = i;
                                    mc.playerController.updateController();
                                    break;
                                }
                            }
                        }
                    }
                    break;
                case "Hotbar":
                    if (mc.player.getHeldItemOffhand().getItem() != Items.END_CRYSTAL)
                    {
                        if (mc.player.getHeldItemMainhand().getItem() != Items.END_CRYSTAL)
                        {
                            for (int i = 0; i < 9; ++i)
                            {
                                ItemStack stack = mc.player.inventory.getStackInSlot(i);
                                
                                if (!stack.isEmpty() && stack.getItem() == Items.END_CRYSTAL)
                                {
                                    mc.player.inventory.currentItem = i;
                                    mc.playerController.updateController();
                                    break;
                                }
                            }
                        }
                    }
                    break;
                case "Inventory":
                    if (mc.player.getHeldItemOffhand().getItem() != Items.END_CRYSTAL)
                    {
                        if (mc.player.getHeldItemMainhand().getItem() != Items.END_CRYSTAL)
                        {
                            int slot = PlayerUtil.GetItemSlot(Items.END_CRYSTAL);
                            
                            if (slot != -1)
                            {
                                mc.playerController.windowClick(mc.player.inventoryContainer.windowId, slot, 0,
                                        ClickType.PICKUP, mc.player);
                                mc.playerController.windowClick(mc.player.inventoryContainer.windowId, 45, 0, ClickType.PICKUP,
                                        mc.player);
                                mc.playerController.windowClick(mc.player.inventoryContainer.windowId, slot, 0,
                                        ClickType.PICKUP, mc.player);
                                mc.playerController.updateController();
                                
                                prevSlot = slot;
                            }
                        }
                    }
                    break;
                case "None":
                    break;
                default:
                    break;
                
            }
            
            // no need to process the code below if we are not using off hand crystal or main hand crystal
            if (mc.player.getHeldItemMainhand().getItem() != Items.END_CRYSTAL && mc.player.getHeldItemOffhand().getItem() != Items.END_CRYSTAL)
                return;
            
            BlockPos selectedPos = null;
            
            // iterate through available place locations
            if (!skipUpdateBlocks)
            {
                for (BlockPos pos : placeLocations)
                {
                    // verify we can still place crystals at this location, if we can't we try next location
                    if (CrystalUtils2.canPlaceCrystalAt(pos, mc.world.getBlockState(pos)))
                    {
                        selectedPos = pos;
                        break;
                    }
                }
            }
            else
                selectedPos = _lastPlaceLocation;
            
            // nothing found... this is bad, wait for next tick to correct it
            if (selectedPos == null)
            {
                _remainingTicks = 0;
                return;
            }
    
            // create a raytrace between player's position and the selected block position
            RayTraceResult result = mc.world.rayTraceBlocks(new Vec3d(mc.player.posX, mc.player.posY + mc.player.getEyeHeight(), mc.player.posZ), new Vec3d(selectedPos.getX() + 0.5, selectedPos.getY() - 0.5, selectedPos.getZ() + 0.5));
    
            // this will allow for bypassing placing through walls afaik
            EnumFacing facing;
    
            if (result == null || result.sideHit == null)
                facing = EnumFacing.UP;
            else
                facing = result.sideHit;
            
            mc.getConnection().sendPacket(new CPacketPlayerTryUseItemOnBlock(selectedPos, facing,
                    mc.player.getHeldItemOffhand().getItem() == Items.END_CRYSTAL ? EnumHand.OFF_HAND : EnumHand.MAIN_HAND, 0, 0, 0));
            
            // if placedcrystals already contains this position, remove it because we need to have it at the back of the list
            if (_placedCrystals.contains(selectedPos))
                _placedCrystals.remove(selectedPos);
            
            // adds the selectedPos to the back of the placed crystals list
            _placedCrystals.add(selectedPos);
            
            if (livingTarget != null)
            {
                float calculatedDamage = CrystalUtils.calculateDamage(mc.world, selectedPos.getX() + 0.5, selectedPos.getY() + 1.0, selectedPos.getZ() + 0.5, livingTarget, 0);
                
                _placedCrystalsDamage.put(selectedPos, calculatedDamage);
            }
            
            if (_lastPlaceLocation != BlockPos.ORIGIN && _lastPlaceLocation == selectedPos)
            {
                // reset ticks, we don't need to do more rotations for this position, so we can crystal faster.
                if (PlaceMode.getValue().equals("Lethal"))
                    _remainingTicks = 0;
            }
            else // set this to our last place location
                _lastPlaceLocation = selectedPos;
        }
        else if (livingTarget == null)
        {
            switch (SwapMode.getValue())
            {
                case "GoBack":
                    if (prevSlot != -1 && prevSlot < 9)
                    {
                        mc.player.inventory.currentItem = prevSlot;
                        mc.playerController.updateController();
                        prevSlot = -1;
                    }
                    break;
                case "Hotbar":
                    break;
                case "Inventory":
                    mc.playerController.windowClick(mc.player.inventoryContainer.windowId, prevSlot, 0,
                            ClickType.PICKUP, mc.player);
                    mc.playerController.windowClick(mc.player.inventoryContainer.windowId, 45, 0, ClickType.PICKUP,
                            mc.player);
                    mc.playerController.windowClick(mc.player.inventoryContainer.windowId, prevSlot, 0,
                            ClickType.PICKUP, mc.player);
                    mc.playerController.updateController();
                    
                    prevSlot = -1;
                    break;
                case "None":
                    break;
                default:
                    break;
                
            }
        }
    });
    
    @EventHandler
    private Listener<EventPlayerUpdate> onPlayerUpdate = new Listener<>(event ->
    {
        setMetaData(_lastTarget); 
    });
    
    @EventHandler
    private Listener<EventPlayerMotionUpdate> OnPlayerUpdate = new Listener<>(event ->
    {
        // we only want to run this event on pre motion, but don't reset rotations here
        if (event.getStage() != MinecraftEvent.Stage.Pre)
            return;
        
        if (event.isCancelled())
        {
            _rotations = null;
            return;
        }
        
        // if the previous event isn't cancelled, or if we don't need to pause.
        if (NeedPause())
        {
            _rotations = null;
            return;
        }
        
        // in order to not flag NCP, we don't want to reset our pitch after we have nothing to do, so do it every second. more legit
        if (_rotationResetTimer.passed(1000))
        {
            _rotations = null;
        }

        // rotations are valid, cancel this update and use our custom rotations instead.
        if (_rotations != null)
        {
            event.cancel();
            event.setPitch(_rotations[1]);
            event.setYaw(_rotations[0]);
        }
    });
    
    @EventHandler
    private Listener<EventServerPacket> onServerPacket = new Listener<>(event ->
    {
        if (event.getStage() != MinecraftEvent.Stage.Pre)
            return;
        
        if (event.getPacket() instanceof SPacketSoundEffect)
        {
            SPacketSoundEffect packet = (SPacketSoundEffect) event.getPacket();

            if (mc.world == null)
                return;
            
            // we need to remove crystals on this packet, because the server sends packets too slow to remove them
            if (packet.getCategory() == SoundCategory.BLOCKS && packet.getSound() == SoundEvents.ENTITY_GENERIC_EXPLODE)
            {
                // loadedEntityList is not thread safe, create a copy and iterate it
                new ArrayList<Entity>(mc.world.loadedEntityList).forEach(e ->
                {
                    // if it's an endercrystal, within 6 distance, set it to be dead
                    if (e instanceof EntityEnderCrystal)
                        if (e.getDistance(packet.getX(), packet.getY(), packet.getZ()) <= 6.0)
                            e.setDead();
                    
                    // remove all crystals within 6 blocks from the placed crystals list
                    _placedCrystals.removeIf(p_Pos -> p_Pos.getDistance((int)packet.getX(), (int)packet.getY(), (int)packet.getZ()) <= 6.0);
                });
            }
        }
    });
    

    @EventHandler
    private Listener<RenderEvent> OnRenderEvent = new Listener<>(p_Event ->
    {
        if (mc.getRenderManager() == null || !Render.getValue())
            return;
        
        _placedCrystals.forEach(pos ->
        {
            final AxisAlignedBB bb = new AxisAlignedBB(pos.getX() - mc.getRenderManager().viewerPosX,
                    pos.getY() - mc.getRenderManager().viewerPosY, pos.getZ() - mc.getRenderManager().viewerPosZ,
                    pos.getX() + 1 - mc.getRenderManager().viewerPosX,
                    pos.getY() + (1) - mc.getRenderManager().viewerPosY,
                    pos.getZ() + 1 - mc.getRenderManager().viewerPosZ);
    
            RenderUtil.camera.setPosition(mc.getRenderViewEntity().posX, mc.getRenderViewEntity().posY,
                    mc.getRenderViewEntity().posZ);
    
            if (RenderUtil.camera.isBoundingBoxInFrustum(new AxisAlignedBB(bb.minX + mc.getRenderManager().viewerPosX,
                    bb.minY + mc.getRenderManager().viewerPosY, bb.minZ + mc.getRenderManager().viewerPosZ,
                    bb.maxX + mc.getRenderManager().viewerPosX, bb.maxY + mc.getRenderManager().viewerPosY,
                    bb.maxZ + mc.getRenderManager().viewerPosZ)))
            {
                GlStateManager.pushMatrix();
                GlStateManager.enableBlend();
                GlStateManager.disableDepth();
                GlStateManager.tryBlendFuncSeparate(770, 771, 0, 1);
                GlStateManager.disableTexture2D();
                GlStateManager.depthMask(false);
                glEnable(GL_LINE_SMOOTH);
                glHint(GL_LINE_SMOOTH_HINT, GL_NICEST);
                glLineWidth(1.5f);
    
                int color = (Alpha.getValue() << 24) | (Red.getValue() << 16) | (Green.getValue() << 8) | Blue.getValue(); 
                
                RenderUtil.drawBoundingBox(bb, 1.0f, color);
                RenderUtil.drawFilledBox(bb, color);
                glDisable(GL_LINE_SMOOTH);
                GlStateManager.depthMask(true);
                GlStateManager.enableDepth();
                GlStateManager.enableTexture2D();
                GlStateManager.disableBlend();
                GlStateManager.popMatrix();

                if (_placedCrystalsDamage.containsKey(pos))
                {
                    GlStateManager.pushMatrix();
                    RenderUtil.glBillboardDistanceScaled((float) pos.getX() + 0.5f, (float) pos.getY() + 0.5f, (float) pos.getZ() + 0.5f, mc.player, 1);
                    final float damage = _placedCrystalsDamage.get(pos);
                    final String damageText = (Math.floor(damage) == damage ? (int) damage : String.format("%.1f", damage)) + "";
                    GlStateManager.disableDepth();
                    GlStateManager.translate(-(RenderUtil.getStringWidth(damageText) / 2.0d), 0, 0);
                    RenderUtil.drawStringWithShadow(damageText, 0, 0, -1);
                    GlStateManager.popMatrix();
                }
            }
        });
    });
    
    public static boolean NeedPause()
    {
        /// We need to pause if we have surround enabled, and don't have obsidian
        if (SummitStatic.SURROUND.isEnabled() && !SummitStatic.SURROUND.IsSurrounded() && SummitStatic.SURROUND.HasObsidian())
        {
            if (!SummitStatic.SURROUND.ActivateOnlyOnShift.getValue())
                return true;

            if (!Wrapper.GetMC().gameSettings.keyBindSneak.isKeyDown())
                return true;
        }
        
        if (SummitStatic.AUTOTRAPFEET.isEnabled() && !SummitStatic.AUTOTRAPFEET.IsCurrentTargetTrapped() && SummitStatic.AUTOTRAPFEET.HasObsidian())
            return true;
        
        if (SummitStatic.AUTOMEND.isEnabled())
            return true;
        
        if (SummitStatic.SELFTRAP.isEnabled() && !SummitStatic.SELFTRAP.IsSelfTrapped() && SummitStatic.SURROUND.HasObsidian())
            return true;
        
        if (SummitStatic.HOLEFILLER.isEnabled() && SummitStatic.HOLEFILLER.IsProcessing())
            return true;

        if (PauseIfHittingBlock.getValue() && Wrapper.GetMC().playerController.isHittingBlock && Wrapper.GetMC().player.getHeldItemMainhand().getItem() instanceof ItemTool)
            return true;
        
        if (SummitStatic.AUTOCITY.isEnabled())
            return true;
        
        if (PauseWhileEating.getValue() && PlayerUtil.IsEating())
            return true;
        
        return false;
    }

    public String getTarget()
    {
        return _lastTarget;
    }

    public boolean isCrystalling()
    {
        return !_rotationResetTimer.passed(1000);
    }
}
