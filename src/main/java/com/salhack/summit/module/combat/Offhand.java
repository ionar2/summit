package com.salhack.summit.module.combat;

import com.salhack.summit.events.bus.EventHandler;
import com.salhack.summit.events.bus.Listener;
import com.salhack.summit.events.player.EventPlayerUpdate;
import com.salhack.summit.module.ListValue;
import com.salhack.summit.module.Module;
import com.salhack.summit.module.Value;
import com.salhack.summit.util.Timer;
import com.salhack.summit.util.entity.PlayerUtil;

import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.init.Items;
import net.minecraft.inventory.ClickType;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

public final class Offhand extends Module
{
    private Value<Float> Delay = new Value<>("Delay", new String[] {"D"}, "Delay", 0.1f, 0.0f, 1.0f, 0.1f);
    private ListValue Mode = new ListValue("Mode", "Will switch offhand if it meets the required health", new String[] { "Crystal", "Gap" });
    private Value<Float> MinHealth = new Value<>("MinHealth", new String[] {"MH"}, "MinHealth to pause this mod", 5f, 0.0f, 36.0f, 1f);
    
    public Offhand()
    {
        super("Offhand", new String[]
        { "Offhand" }, "Automatically places something in your offhand", "NONE", 0xDADB24, ModuleType.COMBAT);
        setMetaData("0");
    }
    
    private Timer timer = new Timer();
    
    private Item getCurrentItem()
    {
        switch (Mode.getValue())
        {
            case "Crystal":
                return Items.END_CRYSTAL;
            case "Gap":
                return Items.GOLDEN_APPLE;
        }
        
        return Items.GOLDEN_APPLE;
    }
    
    // for autototem
    public boolean offhandEnabled()
    {
        return PlayerUtil.GetHealthWithAbsorption() > MinHealth.getValue();
    }

    @EventHandler
    private Listener<EventPlayerUpdate> onPlayerUpdate = new Listener<>(event ->
    {
        setMetaData(String.valueOf(PlayerUtil.GetItemCount(getCurrentItem())));

        if (mc.currentScreen instanceof GuiContainer && !(mc.currentScreen instanceof GuiInventory))
            return;
        
        if (mc.player.getHeldItemOffhand().getItem() == getCurrentItem())
            return;

        if (mc.player.isCreative())
            return;
        
        if (!offhandEnabled())
            return;
        
        for (int i = 0; i < mc.player.inventoryContainer.getInventory().size(); ++i)
        {
            if (i == 0 || i == 5 || i == 6 || i == 7 || i == 8)
                continue;

            ItemStack s = mc.player.inventoryContainer.getInventory().get(i);
            
            if (s.isEmpty())
                continue;
            
            if (s.getItem().equals(getCurrentItem()))
            {
                if (mc.player.getHeldItemOffhand().isEmpty())
                {
                    if (timer.passed(Delay.getValue() * 1000))
                        mc.playerController.windowClick(0, i, 0, ClickType.PICKUP, mc.player);
                    if (timer.passed(Delay.getValue() * 2000))
                    {
                        mc.playerController.windowClick(0, 45, 0, ClickType.PICKUP, mc.player);
                        timer.reset();
                    }
                }
                else
                {
                    if (timer.passed(Delay.getValue() * 1000))
                        mc.playerController.windowClick(0, i, 0, ClickType.PICKUP, mc.player);
                    if (timer.passed(Delay.getValue() * 2000))
                        mc.playerController.windowClick(0, 45, 0, ClickType.PICKUP, mc.player);
                    if (timer.passed(Delay.getValue() * 3000))
                    {
                        timer.reset();
                        mc.playerController.windowClick(0, i, 0, ClickType.PICKUP, mc.player);
                    }
                }
            }
        }
    });
}