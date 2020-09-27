package com.salhack.summit.module.combat;

import com.mojang.realmsclient.gui.ChatFormatting;
import com.salhack.summit.events.MinecraftEvent;
import com.salhack.summit.events.bus.EventHandler;
import com.salhack.summit.events.bus.Listener;
import com.salhack.summit.events.player.EventPlayerMotionUpdate;
import com.salhack.summit.managers.BlockManager;
import com.salhack.summit.module.Module;
import com.salhack.summit.module.render.CityESP;
import com.salhack.summit.util.Pair;
import com.salhack.summit.util.entity.EntityUtil;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;

import java.util.ArrayList;

public class AutoCity extends Module
{
    public AutoCity()
    {
        super("AutoCity", new String[]
        { "AutoCityBoss" }, "Automatically mines the city block if a target near you can be citied", "NONE", 0xDADB24, ModuleType.COMBAT);
    }
    
    @Override
    public void onEnable()
    {
        super.onEnable();
        
        final ArrayList<Pair<EntityPlayer, ArrayList<BlockPos>>> cityPlayers = CityESP.GetPlayersReadyToBeCitied();
        
        if (cityPlayers.isEmpty())
        {
            SendMessage(ChatFormatting.RED + "There is no one to city!");
            toggle();
            return;
        }
        
        EntityPlayer target = null;
        BlockPos targetBlock = null;
        double currDistance = 100;
        
        for (Pair<EntityPlayer, ArrayList<BlockPos>> pair : cityPlayers)
        {
            for (BlockPos pos : pair.getSecond())
            {
                if (targetBlock == null)
                {
                    target = pair.getFirst();
                    targetBlock = pos;
                    continue;
                }
                
                double dist = pos.getDistance(targetBlock.getX(), targetBlock.getY(), targetBlock.getZ());
                
                if (dist < currDistance)
                {
                    currDistance = dist;
                    targetBlock = pos;
                    target = pair.getFirst();
                }
            }
        }
        
        if (targetBlock == null)
        {
            SendMessage(ChatFormatting.RED + "Couldn't find any blocks to mine!");
            toggle();
            return;
        }

        BlockManager.SetCurrentBlock(targetBlock);
        SendMessage(ChatFormatting.LIGHT_PURPLE + "Attempting to mine a block by your target: " + ChatFormatting.RED + target.getName());
    }
    
    @EventHandler
    private Listener<EventPlayerMotionUpdate> OnPlayerUpdate = new Listener<>(p_Event ->
    {
        if (p_Event.getStage() != MinecraftEvent.Stage.Pre)
            return;

        boolean hasPickaxe = mc.player.getHeldItemMainhand().getItem() == Items.DIAMOND_PICKAXE;

        if (!hasPickaxe)
        {
            for (int i = 0; i < 9; ++i)
            {
                ItemStack stack = mc.player.inventory.getStackInSlot(i);
                
                if (stack.isEmpty())
                    continue;
                
                if (stack.getItem() == Items.DIAMOND_PICKAXE)
                {
                    hasPickaxe = true;
                    mc.player.inventory.currentItem = i;
                    mc.playerController.updateController();
                    break;
                }
            }
        }
        
        if (!hasPickaxe)
        {
            SendMessage(ChatFormatting.RED + "No pickaxe!");
            toggle();
            return;
        }
        
        BlockPos currBlock = BlockManager.GetCurrBlock();
        
        if (currBlock == null)
        {
            SendMessage(ChatFormatting.GREEN + "Done!");
            toggle();
            return;
        }
        
        p_Event.cancel();
        
        final double rotations[] =  EntityUtil.calculateLookAt(
                currBlock.getX() + 0.5,
                currBlock.getY() - 0.5,
                currBlock.getZ() + 0.5,
                mc.player);

        p_Event.setPitch(rotations[1]);
        p_Event.setYaw(rotations[0]);
        
        BlockManager.Update(3, false);
    });
}
