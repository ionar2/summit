package com.salhack.summit.module.misc;

import com.salhack.summit.events.bus.EventHandler;
import com.salhack.summit.events.bus.Listener;
import com.salhack.summit.events.player.EventPlayerUpdate;
import com.salhack.summit.module.Module;
import com.salhack.summit.module.Value;
import com.salhack.summit.util.Timer;
import net.minecraft.client.gui.inventory.GuiChest;
import net.minecraft.client.gui.inventory.GuiScreenHorseInventory;
import net.minecraft.client.gui.inventory.GuiShulkerBox;
import net.minecraft.init.Items;
import net.minecraft.inventory.ClickType;
import net.minecraft.item.ItemShulkerBox;
import net.minecraft.item.ItemStack;

public class ChestStealer extends Module
{
    public Value<String> Mode = new Value<>("Mode", new String[]
    { "M" }, "The mode for chest stealer", "Steal");
    public Value<Float> Delay = new Value<Float>("Delay", new String[]
    { "D" }, "Delay for each tick", 1f, 0f, 10f, 1f);
    public Value<Boolean> DepositShulkers = new Value<Boolean>("DepositShulkers", new String[]
    { "S" }, "Only deposit shulkers", false);
    public Value<Boolean> EntityChests = new Value<Boolean>("EntityChests", new String[]
    { "EC" }, "Take from entity chests", false);
    public Value<Boolean> Shulkers = new Value<Boolean>("Shulkers", new String[]
    { "EC" }, "Take from shulkers", false);

    public ChestStealer()
    {
        super("ChestStealer", new String[]
        { "Chest" }, "Steals the contents from chests", "NONE", 0xDB5E24, ModuleType.MISC);
        setMetaData(getMetaData());
        
        Mode.addString("Steal");
        Mode.addString("Store");
        Mode.addString("Drop");
        Mode.addString("TakeEntityPut");
    }

    private Timer timer = new Timer();

    public String getMetaData()
    {
        return Mode.getValue().toString();
    }

    @EventHandler
    private Listener<EventPlayerUpdate> OnPlayerUpdate = new Listener<>(p_Event ->
    {
        if (!timer.passed(Delay.getValue() * 100f))
            return;
        
        setMetaData(getMetaData());

        timer.reset();

        if (mc.currentScreen instanceof GuiChest)
        {
            GuiChest l_Chest = (GuiChest) mc.currentScreen;

            for (int l_I = 0; l_I < l_Chest.lowerChestInventory.getSizeInventory(); ++l_I)
            {
                ItemStack l_Stack = l_Chest.lowerChestInventory.getStackInSlot(l_I);

                if ((l_Stack.isEmpty() || l_Stack.getItem() == Items.AIR) && (Mode.getValue().equals("Store") || Mode.getValue().equals("TakeEntityPut")))
                {
                    HandleStoring(l_Chest.inventorySlots.windowId, l_Chest.lowerChestInventory.getSizeInventory() - 9);
                    return;
                }

                if (Shulkers.getValue() && !(l_Stack.getItem() instanceof ItemShulkerBox))
                    continue;
                
                if (l_Stack.isEmpty())
                    continue;

                switch (Mode.getValue())
                {
                    case "Steal":
                        mc.playerController.windowClick(l_Chest.inventorySlots.windowId, l_I, 0, ClickType.QUICK_MOVE, mc.player);
                        return;
                    case "Drop":
                        mc.playerController.windowClick(l_Chest.inventorySlots.windowId, l_I, -999, ClickType.THROW, mc.player);
                        return;
                    default:
                        break;
                }
            }
        }
        else if (mc.currentScreen instanceof GuiScreenHorseInventory && (EntityChests.getValue() || Mode.getValue().equals("TakeEntityPut")))
        {
            GuiScreenHorseInventory l_Chest = (GuiScreenHorseInventory)mc.currentScreen;
            
            for (int l_I = 0; l_I < l_Chest.horseInventory.getSizeInventory(); ++l_I)
            {
                ItemStack l_Stack = l_Chest.horseInventory.getStackInSlot(l_I);

                if ((l_Stack.isEmpty() || l_Stack.getItem() == Items.AIR) && Mode.getValue().equals("Store"))
                {
                    HandleStoring(l_Chest.inventorySlots.windowId, l_Chest.horseInventory.getSizeInventory() - 9);
                    return;
                }

                if (Shulkers.getValue() && !(l_Stack.getItem() instanceof ItemShulkerBox))
                    continue;
                
                if (l_Stack.isEmpty())
                    continue;

                switch (Mode.getValue())
                {
                    case "TakeEntityPut": // handle the same case as steal :) (case merging looks weird ngl)
                    case "Steal":
                        mc.playerController.windowClick(l_Chest.inventorySlots.windowId, l_I, 0, ClickType.QUICK_MOVE, mc.player);
                        return;
                    case "Drop":
                        mc.playerController.windowClick(l_Chest.inventorySlots.windowId, l_I, -999, ClickType.THROW, mc.player);
                        return;
                    default:
                        break;
                }
            }
        }
        else if (mc.currentScreen instanceof GuiShulkerBox && Shulkers.getValue())
        {
            GuiShulkerBox l_Chest = (GuiShulkerBox)mc.currentScreen;
            
            for (int l_I = 0; l_I < l_Chest.inventory.getSizeInventory(); ++l_I)
            {
                ItemStack l_Stack = l_Chest.inventory.getStackInSlot(l_I);

                if ((l_Stack.isEmpty() || l_Stack.getItem() == Items.AIR) && Mode.getValue().equals("Store"))
                {
                    HandleStoring(l_Chest.inventorySlots.windowId, l_Chest.inventory.getSizeInventory() - 9);
                    return;
                }

                if (Shulkers.getValue() && !(l_Stack.getItem() instanceof ItemShulkerBox))
                    continue;

                if (l_Stack.isEmpty())
                    continue;
                
                switch (Mode.getValue())
                {
                    case "Steal":
                        mc.playerController.windowClick(l_Chest.inventorySlots.windowId, l_I, 0, ClickType.QUICK_MOVE, mc.player);
                        return;
                    case "Drop":
                        mc.playerController.windowClick(l_Chest.inventorySlots.windowId, l_I, -999, ClickType.THROW, mc.player);
                        return;
                    default:
                        break;
                }
            }
        }
    });

    private void HandleStoring(int p_WindowId, int p_Slot)
    {
        if (Mode.getValue().equals("Store") || Mode.getValue().equals("TakeEntityPut"))
        {
            for (int l_Y = 9; l_Y < mc.player.inventoryContainer.inventorySlots.size() - 1; ++l_Y)
            {
                ItemStack l_InvStack = mc.player.inventoryContainer.getSlot(l_Y).getStack();

                if (l_InvStack.isEmpty() || l_InvStack.getItem() == Items.AIR)
                    continue;

                if (DepositShulkers.getValue() && !(l_InvStack.getItem() instanceof ItemShulkerBox))
                    continue;

                mc.playerController.windowClick(p_WindowId, l_Y + p_Slot, 0, ClickType.QUICK_MOVE, mc.player);
                return;
            }
        }
    }
}
