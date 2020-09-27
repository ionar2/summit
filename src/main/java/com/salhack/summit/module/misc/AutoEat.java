package com.salhack.summit.module.misc;

import com.salhack.summit.events.bus.EventHandler;
import com.salhack.summit.events.bus.Listener;
import com.salhack.summit.events.player.EventPlayerIsHandActive;
import com.salhack.summit.events.player.EventPlayerOnStoppedUsingItem;
import com.salhack.summit.events.player.EventPlayerUpdate;
import com.salhack.summit.main.SummitStatic;
import com.salhack.summit.module.Module;
import com.salhack.summit.module.Value;
import com.salhack.summit.util.entity.PlayerUtil;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.CPacketPlayerTryUseItem;
import net.minecraft.util.EnumHand;

// Incompatible with AntiGappleDisease, mode Always.

public class AutoEat extends Module
{
    private Value<String> mode = new Value<>("Mode", new String[]{"Mode", "M"}, "Check for Hunger or Health?", "Hunger");
    private Value<Boolean> autoSwitch = new Value<>("AutoSwitch", new String[]{"AutoSwitch"}, "Automatically switches to Gaps.", true);
    private Value<Boolean> pauseOnCrystal = new Value<>("PauseOnCrystal", new String[]{"PauseOnCrystal"}, "Pauses while CrystalAura is enabled.", false);
    private Value<Float> hunger = new Value<>("Hunger", new String[]{"Hunger"}, "Only eats if hunger is below this amount. (Mode Hunger)", 16.0f, 0.0f, 20.0f, 1.0f);
    private Value<Float> health = new Value<>("Health", new String[]{"Health"}, "Only eats if health is below this amount. (Mode Heatlh)", 16.0f, 0.0f, 36.0f, 1.0f);

    public AutoEat()
    {
        super("AutoEat", new String[]{"Eat"}, "Automatically eats food, depending on hunger, or health", "NONE", 0xF9EE1E, ModuleType.MISC);
        
        mode.addString("Health");
        mode.addString("Hunger");
        mode.addString("Finish");
        mode.addString("Offhand");
    }

    boolean playerIsEating = false;

    @EventHandler
    private Listener<EventPlayerUpdate> OnPlayerUpdate = new Listener<>(event ->
    {
        if (mode.getValue().equals("Finish"))
            return;
        
        boolean healthCheck = PlayerUtil.GetHealthWithAbsorption() <= health.getValue() && mode.getValue().equals("Health");
        boolean hungerCheck = mc.player.getFoodStats().getSaturationLevel() <= hunger.getValue() && mode.getValue().equals("Hunger");
        boolean mainhandHasGap = mc.player.getHeldItemMainhand().getItem() == Items.GOLDEN_APPLE;
        boolean offhandHasGap = mc.player.getHeldItemOffhand().getItem() == Items.GOLDEN_APPLE;

        if (pauseOnCrystal.getValue() && SummitStatic.AUTOCRYSTAL.isEnabled())
        {
            playerIsEating = false;
            return;
        }

        if (mainhandHasGap && !offhandHasGap)
        {
            if (healthCheck || hungerCheck)
            {
                playerIsEating = true;

                if (mc.rightClickDelayTimer != 0) return;

                mc.rightClickDelayTimer = 4;
                mc.player.connection.sendPacket(new CPacketPlayerTryUseItem(EnumHand.MAIN_HAND));
            }
        }

        else if (autoSwitch.getValue() && !offhandHasGap)
        {
            if (hungerCheck && !mainhandHasGap || healthCheck && !mainhandHasGap)
            {
                for (int i = 0; i < 9; ++i)
                {
                    ItemStack stack = mc.player.inventory.getStackInSlot(i);
                    if (!stack.isEmpty())
                    {
                        if (stack.getItem() == Items.GOLDEN_APPLE)
                        {
                            mc.player.inventory.currentItem = i;
                            mc.playerController.updateController();
                            break;
                        }
                    }
                }
            }
        }

        if (mode.getValue().equals("Offhand"))
        {
            if (offhandHasGap && !mainhandHasGap)
            {
                if (hungerCheck || healthCheck)
                playerIsEating = true;

                if (mc.rightClickDelayTimer != 0) return;

                mc.rightClickDelayTimer = 4;
                mc.player.connection.sendPacket(new CPacketPlayerTryUseItem(EnumHand.OFF_HAND));
            }

            if (mode.getValue().equals("Hunger") && mc.player.getFoodStats().getSaturationLevel() > hunger.getValue() || mode.getValue().equals("Health") && PlayerUtil.GetHealthWithAbsorption() > health.getValue())
            {
                playerIsEating = false;
            }
        }
    });

    @EventHandler
    private Listener<EventPlayerOnStoppedUsingItem> OnStopUsingItem = new Listener<>(event ->
    {
        // If statement needed so that you don't get stuck eating after pressing right-click for no reason.
        if (playerIsEating || mode.getValue().equals("Finish"))
        {
            event.cancel();
        }
    });
    
    @EventHandler
    private Listener<EventPlayerIsHandActive> onIsHandActive = new Listener<>(event ->
    {
        // allow to mine while eating with offhand
        if (mode.getValue().equals("Offhand"))
        {
            event.cancel();
        }
    });
}