package com.salhack.summit.module.misc;

import org.lwjgl.input.Mouse;

import com.salhack.summit.events.bus.EventHandler;
import com.salhack.summit.events.bus.Listener;
import com.salhack.summit.events.player.EventPlayerUpdate;
import com.salhack.summit.module.Module;
import com.salhack.summit.util.Timer;
import com.salhack.summit.util.entity.PlayerUtil;
import net.minecraft.init.Items;
import net.minecraft.inventory.ClickType;
import net.minecraft.util.EnumHand;
import net.minecraftforge.fml.common.gameevent.InputEvent.MouseInputEvent;

public class MiddleClickPearl extends Module
{

    public MiddleClickPearl()
    {
        super("MiddleClickPearl", new String[]
        { "MCP" }, "Automatically throws a pearl", "NONE", -1, ModuleType.MISC);
    }
    
    int slot = -1;
    boolean needSendPackets;
    private Timer timer = new Timer();

    @EventHandler
    private Listener<MouseInputEvent> onMouseEvent = new Listener<>(event ->
    {
        if (mc.world == null || mc.player == null || mc.player.inventory == null)
            return;
        
        if (Mouse.getEventButton() == 2)
        {
            slot = PlayerUtil.GetItemSlot(Items.ENDER_PEARL);
            int hotbarSlot = PlayerUtil.GetItemInHotbar(Items.ENDER_PEARL);
            
            if (slot != -1 && slot < 36)
            {
                mc.playerController.windowClick(this.mc.player.inventoryContainer.windowId, slot, mc.player.inventory.currentItem, ClickType.SWAP, this.mc.player);
                mc.playerController.updateController();
                mc.playerController.processRightClick(mc.player, mc.world, EnumHand.MAIN_HAND);
                timer.reset();
                needSendPackets = true;
            }
            else if (hotbarSlot != -1)
            {
                int prevSlot = mc.player.inventory.currentItem;
                mc.player.inventory.currentItem = hotbarSlot;
                mc.playerController.updateController();
                mc.playerController.processRightClick(mc.player, mc.world, EnumHand.MAIN_HAND);
                mc.player.inventory.currentItem = prevSlot;
                mc.playerController.updateController();
            }
        }
    });
    
    @EventHandler
    private Listener<EventPlayerUpdate> onUpdate = new Listener<>(event ->
    {
        if (needSendPackets && timer.passed(100))
        {
            needSendPackets = false;
            mc.playerController.windowClick(this.mc.player.inventoryContainer.windowId, slot, mc.player.inventory.currentItem, ClickType.SWAP, this.mc.player);
            mc.playerController.updateController();
        }
    });
}
