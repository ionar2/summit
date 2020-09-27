package com.salhack.summit.module.combat;

import com.mojang.realmsclient.gui.ChatFormatting;
import com.salhack.summit.events.bus.EventHandler;
import com.salhack.summit.events.bus.Listener;
import com.salhack.summit.events.player.EventPlayerUpdate;
import com.salhack.summit.main.SummitStatic;
import com.salhack.summit.module.Module;
import com.salhack.summit.module.Value;
import com.salhack.summit.util.Timer;
import com.salhack.summit.util.entity.PlayerUtil;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.init.Items;
import net.minecraft.inventory.ClickType;
import net.minecraft.util.EnumHand;

public class AutoTotem extends Module
{
    private final Value<Double> Delay = new Value<>("Delay", new String[]
            {"Delay"}, "Delay before switching to a totem.", 0.0, 0.0, 2.0, 0.1);
    private final Value<Boolean> ChatMSGS = new Value<>("ChatMSGS", new String[]
            {"ChatMSGS"}, "Display messages in chat.", true);

    public AutoTotem() {
        super("AutoTotem", new String[]
                { "AutoTotem" }, "Automatically puts a Totem in offhand.", "NONE", 0xDADB24, ModuleType.COMBAT);
    }

    private boolean isSwitching;
    private Timer timer = new Timer();

    @Override
    public void onDisable() {
        super.onDisable();

        isSwitching = false;
    }

    @EventHandler
    private Listener<EventPlayerUpdate> onPlayerUpdate = new Listener<>(p_Event -> {
        setMetaData(String.valueOf(PlayerUtil.GetItemCount(Items.TOTEM_OF_UNDYING)));

        if (SummitStatic.OFFHAND.isEnabled())
            return;

        if (mc.currentScreen instanceof GuiContainer && !(mc.currentScreen instanceof GuiInventory))
            return;

        if (!isSwitching) {
            timer.reset();
        }

        if (mc.player.getHeldItem(EnumHand.OFF_HAND).getItem() == Items.TOTEM_OF_UNDYING)
            return;

        if (mc.player.isCreative())
            return;

        int n;
        int i = n = 44;
        while (i >= 9) {
            if (mc.player.inventoryContainer.getSlot(n).getStack().getItem() == Items.TOTEM_OF_UNDYING) {
                isSwitching = true;
                if (this.timer.passed(Delay.getValue() * 1000.0f) && mc.player.inventory.getItemStack().getItem() != Items.TOTEM_OF_UNDYING) {
                    mc.playerController.windowClick(0, n, 0, ClickType.PICKUP, mc.player);
                }
                if (this.timer.passed(Delay.getValue() * 2000.0f) && mc.player.inventory.getItemStack().getItem() == Items.TOTEM_OF_UNDYING) {
                    mc.playerController.windowClick(0, 45, 0, ClickType.PICKUP, mc.player);
                    if (mc.player.inventory.getItemStack().isEmpty()) {
                        if (ChatMSGS.getValue()) SendMessage(ChatFormatting.YELLOW + "Offhand now has a Totem.");
                        isSwitching = false;
                        return;
                    }
                }
                if (this.timer.passed(Delay.getValue() * 3000.0f) && !mc.player.inventory.getItemStack().isEmpty() && mc.player.getHeldItem(EnumHand.OFF_HAND).getItem() == Items.TOTEM_OF_UNDYING) {
                    mc.playerController.windowClick(0, n, 0, ClickType.PICKUP, mc.player);
                    isSwitching = false;
                    return;
                }
            }
            i = --n;
        }
    });
}
