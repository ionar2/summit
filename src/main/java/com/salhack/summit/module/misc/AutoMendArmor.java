package com.salhack.summit.module.misc;

import com.mojang.realmsclient.gui.ChatFormatting;

import com.salhack.summit.events.MinecraftEvent;
import com.salhack.summit.events.bus.EventHandler;
import com.salhack.summit.events.bus.Listener;
import com.salhack.summit.events.player.EventPlayerMotionUpdate;
import com.salhack.summit.module.Module;
import com.salhack.summit.module.Value;
import com.salhack.summit.util.Timer;
import com.salhack.summit.util.entity.PlayerUtil;
import net.minecraft.init.Items;
import net.minecraft.inventory.ClickType;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedList;

public final class AutoMendArmor extends Module
{
    public final Value<Float> Delay = new Value<Float>("Delay", new String[] {"D"}, "Delay for moving armor pieces", 1.0f, 0.0f, 10.0f, 1.0f);
    public final Value<Float> Pct = new Value<Float>("Pct", new String[] {"P"}, "Amount of armor pct to heal at, so you don't waste extra experience potions", 90.0f, 0.0f, 100.0f, 10.0f);
    public final Value<Boolean> GhostHand = new Value<Boolean>("GhostHand", new String [] {"GH"}, "Uses ghost hand for exp", false);
    public final Value<Boolean> UseXCarry = new Value<Boolean>("UseXCarry", new String [] {"Xcarry"}, "Uses xcarry", false);
    public final Value<Boolean> turtleShell = new Value<Boolean>("TurtleShell", new String [] {"TurtleShell"}, "Puts armor back on if DMG is taken.", false);
    
    public AutoMendArmor()
    {
        super("AutoMendArmor", new String[]
        { "AMA" }, "Moves your armor to a free slot and mends them piece by piece. Recommended to use autoarmor incase you need to toggle this off while using it", "NONE", 0x24DBD4, ModuleType.MISC);
    }
    
    private LinkedList<MendState> SlotsToMoveTo = new LinkedList<MendState>();
    private Timer timer = new Timer();
    private Timer internalTimer = new Timer();
    private boolean ReadyToMend = false;
    private boolean AllDone = false;
    
    @Override
    public void toggleNoSave()
    {
        
    }
    
    @Override
    public void onEnable()
    {
        super.onEnable();
        
        ArrayList<ItemStack> ArmorsToMend = new ArrayList<ItemStack>();
        SlotsToMoveTo.clear();
        ReadyToMend = false;
        AllDone = false;
        
        int l_Slot = PlayerUtil.GetItemInHotbar(Items.EXPERIENCE_BOTTLE);
        
        if (l_Slot == -1)
        {
            SendMessage("You don't have any XP! Disabling!");
            toggle();
            return;
        }

        final Iterator<ItemStack> l_Armor = mc.player.getArmorInventoryList().iterator();
        
        int l_I = 0;
        boolean l_NeedMend = false;

        while (l_Armor.hasNext())
        {
            final ItemStack l_Item = l_Armor.next();
            if (l_Item != ItemStack.EMPTY && l_Item.getItem() != Items.AIR)
            {
                ArmorsToMend.add(l_Item);
                
                float l_Pct = GetArmorPct(l_Item);
                
                if (l_Pct < Pct.getValue())
                {
                    l_NeedMend = true;
                    SendMessage(ChatFormatting.LIGHT_PURPLE + "[" + ++l_I + "] Mending " + ChatFormatting.AQUA + l_Item.getDisplayName() + ChatFormatting.LIGHT_PURPLE + " it has " + l_Pct + "%.");
                }
            }
        }
        
        if (ArmorsToMend.isEmpty() || !l_NeedMend)
        {
            SendMessage(ChatFormatting.RED + "Nothing to mend!");
            toggle();
            return;
        }
        
        ArmorsToMend.sort(Comparator.comparing(ItemStack::getItemDamage).reversed());
        
        ArmorsToMend.forEach(p_Item ->
        {
            SendMessage(p_Item.getDisplayName() + " " + p_Item.getItemDamage());
        });
        
        l_I = 0;
        
        final Iterator<ItemStack> l_Itr = ArmorsToMend.iterator();
        
        boolean l_First = true;
        
        for (l_I = 0; l_I < mc.player.inventoryContainer.getInventory().size(); ++l_I)
        {
            if (l_I == 0 || l_I == 5 || l_I == 6 || l_I == 7 || l_I == 8)
                continue;
            
            if (!UseXCarry.getValue())
            {
                switch (l_I)
                {
                    case 1:
                    case 2:
                    case 3:
                    case 4:
                        continue;
                    default:
                        break;
                }
            }
            
            ItemStack l_Stack = mc.player.inventoryContainer.getInventory().get(l_I);
            
            /// Slot must be empty or air
            if (!l_Stack.isEmpty() && l_Stack.getItem() != Items.AIR)
                continue;
            
            if (!l_Itr.hasNext())
                break;
            
            ItemStack l_ArmorS = l_Itr.next();
            
            SlotsToMoveTo.add(new MendState(l_First, l_I, GetSlotByItemStack(l_ArmorS), GetArmorPct(l_ArmorS) < Pct.getValue(), l_ArmorS.getDisplayName()));
            
            if (l_First)
                l_First = false;
            
           // SendMessage("Found free slot " + l_I + " for " + l_ArmorS.getDisplayName() + " stack here is " + l_Stack.getDisplayName());
        }
    }

    @EventHandler
    private Listener<net.minecraftforge.event.entity.living.LivingAttackEvent> LivingAttackEvent = new Listener<>(p_Event ->
    {
        if (p_Event.getEntityLiving().equals(mc.player) && turtleShell.getValue())
        {
            for (MendState mendState : SlotsToMoveTo)
            {
                mendState.Reequip = true;
            }
            toggle();
        }
    });

    @EventHandler
    private Listener<EventPlayerMotionUpdate> OnPlayerUpdate = new Listener<>(p_Event ->
    {
        if (p_Event.getStage() != MinecraftEvent.Stage.Pre)
            return;
        
        p_Event.cancel();
        
        p_Event.setPitch(90.0f);
        
        if (timer.passed(Delay.getValue() * 100))
        {
            timer.reset();
            
            if (SlotsToMoveTo.isEmpty())
                return;
            
            boolean l_NeedBreak = false;
            
            for (MendState l_State : SlotsToMoveTo)
            {
                if (l_State.MovedToInv)
                    continue;
                
                l_State.MovedToInv = true;
                
             //   SendMessage("" + l_State.SlotMovedTo);

                if (l_State.Reequip)
                {
                    if (l_State.SlotMovedTo <= 4)
                    {
                        mc.playerController.windowClick(mc.player.inventoryContainer.windowId, l_State.SlotMovedTo, 0, ClickType.PICKUP, mc.player);
                        mc.playerController.windowClick(mc.player.inventoryContainer.windowId, l_State.ArmorSlot, 0, ClickType.PICKUP, mc.player);
                    }
                    else
                        mc.playerController.windowClick(mc.player.inventoryContainer.windowId, l_State.SlotMovedTo, 0, ClickType.QUICK_MOVE, mc.player);
                 //   mc.playerController.windowClick(mc.player.inventoryContainer.windowId, l_State.ArmorSlot, 0, ClickType.PICKUP, mc.player);
                }
                else
                {
                 //   mc.playerController.windowClick(mc.player.inventoryContainer.windowId, l_State.ArmorSlot, 0, ClickType.QUICK_MOVE, mc.player);
                    mc.playerController.windowClick(mc.player.inventoryContainer.windowId, l_State.SlotMovedTo, 0, ClickType.PICKUP, mc.player);
                    mc.playerController.windowClick(mc.player.inventoryContainer.windowId, l_State.ArmorSlot, 0, ClickType.PICKUP, mc.player);
                    mc.playerController.windowClick(mc.player.inventoryContainer.windowId, l_State.SlotMovedTo, 0, ClickType.PICKUP, mc.player);
                }
                
                l_NeedBreak = true;
                break;
            }
            
            if (!l_NeedBreak)
            {
                ReadyToMend = true;
                
                if (AllDone)
                {
                    SendMessage(ChatFormatting.AQUA + "Disabling.");
                    toggle();
                    return;
                }
            }
        }
        
        if (!internalTimer.passed(1000))
            return;
        
        if (ReadyToMend && !AllDone)
        {
            ItemStack l_CurrItem = mc.player.getHeldItemMainhand();

            int l_CurrSlot = -1;
            if (l_CurrItem.isEmpty() || l_CurrItem.getItem() != Items.EXPERIENCE_BOTTLE)
            {
                int l_Slot = PlayerUtil.GetItemInHotbar(Items.EXPERIENCE_BOTTLE);
                
                if (l_Slot != -1)
                {
                    l_CurrSlot = mc.player.inventory.currentItem;
                    mc.player.inventory.currentItem = l_Slot;
                    mc.playerController.updateController();
                }
                else
                {
                    SendMessage(ChatFormatting.RED + "No XP Found!");

                    SlotsToMoveTo.forEach(p_State ->
                    {
                        p_State.MovedToInv = false;
                        p_State.Reequip = true;
                    });
                    
                    SlotsToMoveTo.get(0).MovedToInv = true;
                    AllDone = true;
                    return;
                }
            }
            
            l_CurrItem = mc.player.getHeldItemMainhand();
            
            if (l_CurrItem.isEmpty() || l_CurrItem.getItem() != Items.EXPERIENCE_BOTTLE)
                return;
            
            final Iterator<ItemStack> l_Armor = mc.player.getArmorInventoryList().iterator();
            
            while (l_Armor.hasNext())
            {
                ItemStack l_Stack = l_Armor.next();

                if (l_Stack == ItemStack.EMPTY || l_Stack.getItem() == Items.AIR)
                    continue;

                float l_ArmorPct = GetArmorPct(l_Stack);
                
                if (l_ArmorPct >= Pct.getValue())
                {
                    if (!SlotsToMoveTo.isEmpty())
                    {
                        MendState l_State = SlotsToMoveTo.get(0);
                        
                        if (l_State.DoneMending)
                        {
                            SlotsToMoveTo.forEach(p_State ->
                            {
                                p_State.MovedToInv = false;
                                p_State.Reequip = true;
                            });
                            SendMessage(ChatFormatting.GREEN + "All done!");
                            l_State.MovedToInv = true;
                            AllDone = true;
                            return;
                        }
                        
                        l_State.DoneMending = true;
                        l_State.MovedToInv = false;
                        l_State.Reequip = false;
                        
                        SendMessage(String.format("%sDone Mending %s%s %sat the requirement of %s NewPct: %s", ChatFormatting.LIGHT_PURPLE, ChatFormatting.AQUA, l_Stack.getDisplayName(), ChatFormatting.LIGHT_PURPLE, Pct.getValue().toString() + "%", l_ArmorPct+"%"));
                        ReadyToMend = false;
                        
                        SlotsToMoveTo.remove(0);
                        SlotsToMoveTo.add(l_State);
                        
                        MendState l_NewState = SlotsToMoveTo.get(0);

                        if (l_NewState.DoneMending || !l_NewState.NeedMend)
                        {
                            SlotsToMoveTo.forEach(p_State ->
                            {
                                p_State.MovedToInv = false;
                                p_State.Reequip = true;
                            });
                            l_State.MovedToInv = true;
                            SendMessage(ChatFormatting.GREEN + "All done!");
                            AllDone = true;
                            return;
                        }
                        else
                        {
                            SendMessage(ChatFormatting.LIGHT_PURPLE + "Mending next piece.. it's name is " + ChatFormatting.AQUA + l_NewState.ItemName);
                            
                            l_NewState.MovedToInv = false;
                            l_NewState.Reequip = true;
                        }
                    }
                    
                    return;
                }
                else
                {
                    mc.playerController.processRightClick(mc.player, mc.world, EnumHand.MAIN_HAND);
                    
                    if (l_CurrSlot != -1 && GhostHand.getValue())
                    {
                        mc.player.inventory.currentItem = l_CurrSlot;
                        mc.playerController.updateController();
                    }
                    
                    break;
                }
            }
        }
    });
    
    public int GetSlotByItemStack(ItemStack p_Stack)
    {
        if (p_Stack.getItem() instanceof ItemArmor)
        {
            ItemArmor l_Armor = (ItemArmor) p_Stack.getItem();
            
            switch (l_Armor.getEquipmentSlot())
            {
                case CHEST:
                    return 6;
                case FEET:
                    return 8;
                case HEAD:
                    return 5;
                case LEGS:
                    return 7;
                default:
                    break;
            }
        }
        
        return mc.player.inventory.armorInventory.indexOf(p_Stack) + 5;
    }
    
    private float GetArmorPct(ItemStack p_Stack)
    {
        return ((float)(p_Stack.getMaxDamage()-p_Stack.getItemDamage()) /  (float)p_Stack.getMaxDamage())*100.0f;
    }
    
    private class MendState
    {
        public MendState(boolean p_MovedToInv, int p_SlotMovedTo, int p_ArmorSlot, boolean p_NeedMend, String p_ItemName)
        {
            MovedToInv = p_MovedToInv;
            SlotMovedTo = p_SlotMovedTo;
            ArmorSlot = p_ArmorSlot;
            NeedMend = p_NeedMend;
            ItemName = p_ItemName;
        }
        public boolean MovedToInv = false;
        public int SlotMovedTo = -1;
        public boolean Reequip = false;
        public int ArmorSlot = -1;
        public boolean DoneMending = false;
        public boolean NeedMend = true;
        public String ItemName;
    }
}
