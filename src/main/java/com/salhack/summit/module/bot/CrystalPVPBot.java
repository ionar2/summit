package com.salhack.summit.module.bot;

import com.salhack.summit.events.bus.EventHandler;
import com.salhack.summit.events.bus.Listener;
import com.salhack.summit.events.player.EventPlayerOnStoppedUsingItem;
import com.salhack.summit.events.player.EventPlayerUpdate;
import com.salhack.summit.main.SummitStatic;
import com.salhack.summit.managers.FriendManager;
import com.salhack.summit.module.Module;
import com.salhack.summit.module.Value;
import com.salhack.summit.module.combat.AutoCrystal;
import com.salhack.summit.util.Hole;
import com.salhack.summit.util.entity.PlayerUtil;
import com.salhack.summit.util.render.ESPUtil;

import baritone.api.BaritoneAPI;
import baritone.api.pathing.goals.GoalXZ;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.network.play.client.CPacketPlayerTryUseItem;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;

public class CrystalPVPBot extends Module
{
    public final Value<Integer> Radius = new Value<Integer>("Radius", new String[] { "Radius", "Range", "Distance" }, "Radius in blocks to scan for holes.", 8, 0, 32, 1);

    public CrystalPVPBot()
    {
        super("CrystalPVPBot", new String[] {"CPVPBot"}, "Automatically fights the battles for you, because pvp is gay", "NONE", -1, ModuleType.BOT);
    }
    
    private boolean isEating;
    
    @EventHandler
    private Listener<EventPlayerUpdate> onPlayerUpdate = new Listener<>(event ->
    {
        boolean isTrapped = PlayerUtil.IsPlayerTrapped();
        
        if (PlayerUtil.GetHealthWithAbsorption() <= 20.0f || isTrapped)
        {
            int slot = PlayerUtil.GetItemInHotbar(isTrapped ? Items.CHORUS_FRUIT : Items.GOLDEN_APPLE);
            
            if (slot != -1)
            {
                mc.player.inventory.currentItem = slot;
                mc.playerController.updateController();
                
                if (mc.rightClickDelayTimer != 0) return;

                mc.rightClickDelayTimer = 4;
                mc.player.connection.sendPacket(new CPacketPlayerTryUseItem(EnumHand.MAIN_HAND));
                isEating = true;
            }
        }
        else
            isEating = false;
        
        
        
        EntityPlayer target = null;
        float distance = 6.0f;
        
        for (EntityPlayer player : mc.world.playerEntities)
        {
            if (player instanceof EntityPlayerSP || FriendManager.Get().IsFriend(player))
                continue;
            
            float dist = mc.player.getDistance(player);
            
            if (dist < distance)
            {
                distance = dist;
                target = player;
            }
        }
        
        if (target != null)
        {
            BaritoneAPI.getProvider().getPrimaryBaritone().getCustomGoalProcess().setGoal(null);
            
            if (!SummitStatic.AUTOCRYSTAL.isEnabled())
                SummitStatic.AUTOCRYSTAL.toggle();
            
            if (!PlayerUtil.IsPlayerInHole())
            {
                // no hole found, use surround
                if (!baritoneIntoHole())
                {
                    if (!SummitStatic.SURROUND.isEnabled())
                        SummitStatic.SURROUND.toggle();
                }
                else
                {
                    if (SummitStatic.SURROUND.isEnabled())
                        SummitStatic.SURROUND.toggle();
                }
            }
            
            boolean enemyInHole = PlayerUtil.IsPlayerInHole(target);
            
            if (enemyInHole)
            {
                if (target.getHealth()+target.getAbsorptionAmount() > AutoCrystal.FacePlace.getValue())
                {
                    if (!SummitStatic.AURA.isEnabled())
                        SummitStatic.AURA.toggle();
                }
                else
                {
                    if (SummitStatic.AURA.isEnabled())
                        SummitStatic.AURA.toggle();
                }
            }
            else
            {
                if (SummitStatic.AUTOCRYSTAL.isCrystalling())
                {
                    if (SummitStatic.AURA.isEnabled())
                        SummitStatic.AURA.toggle();
                }
                else if (!SummitStatic.AURA.isEnabled())
                {
                    SummitStatic.AURA.toggle();
                    
                    int slot = PlayerUtil.GetItemSlot(Items.DIAMOND_SWORD);
                    
                    if (slot != -1)
                    {
                        mc.player.inventory.currentItem = slot;
                        mc.playerController.updateController();
                    }
                }
            }
        }
        else
        {
            if (SummitStatic.SURROUND.isEnabled())
                SummitStatic.SURROUND.toggle();
            if (SummitStatic.AUTOCRYSTAL.isEnabled())
                SummitStatic.AUTOCRYSTAL.toggle();
            if (SummitStatic.AURA.isEnabled())
                SummitStatic.AURA.toggle();
            
            BaritoneAPI.getProvider().getPrimaryBaritone().getCustomGoalProcess().setGoalAndPath(GoalXZ.fromDirection(
                    mc.player.getPositionVector(),
                    mc.player.rotationYawHead,
                    100
            ));
        }
    });

    @EventHandler
    private Listener<EventPlayerOnStoppedUsingItem> OnStopUsingItem = new Listener<>(event ->
    {
        if (isEating)
        {
            event.cancel();
        }
    });
    
    private boolean baritoneIntoHole()
    {
        BlockPos bestHole = null;
        double dist = 100.0;
        final Vec3i playerPos = new Vec3i(mc.player.posX, mc.player.posY, mc.player.posZ);
    
        for (int x = playerPos.getX() - Radius.getValue(); x < playerPos.getX() + Radius.getValue(); x++)
        {
            for (int z = playerPos.getZ() - Radius.getValue(); z < playerPos.getZ() + Radius.getValue(); z++)
            {
                for (int y = playerPos.getY() + Radius.getValue(); y > playerPos.getY() - Radius.getValue(); y--)
                {
                    final BlockPos blockPos = new BlockPos(x, y, z);
                    double holeDist = mc.player.getDistanceSq(blockPos);
    
                    // ignore hole we are in
                    if (holeDist <= 1)
                        continue;
    
                    final IBlockState blockState = mc.world.getBlockState(blockPos);
    
                    Hole.HoleTypes l_Type = ESPUtil.isBlockValid(blockState, blockPos);
    
                    if (l_Type == Hole.HoleTypes.Bedrock)
                    {
                        final IBlockState downBlockState = mc.world.getBlockState(blockPos.down());
                        if (downBlockState.getBlock() == Blocks.AIR)
                        {
                            l_Type = ESPUtil.isBlockValid(downBlockState, blockPos);
    
                            if (l_Type != Hole.HoleTypes.None)
                            {
                                if (holeDist < dist)
                                {
                                    dist = holeDist;
                                    bestHole = blockPos;
                                }
                            }
                        }
                        else
                        {
                            if (holeDist < dist)
                            {
                                dist = holeDist;
                                bestHole = blockPos;
                            }
                        }
                    }
                }
            }
        }
        
        if (bestHole == null)
        {
            SendMessage("Couldn't find a proper hole");
            return false;
        }
        
        return true;
    }
}
