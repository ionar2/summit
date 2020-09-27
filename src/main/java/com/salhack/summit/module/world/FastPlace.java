package com.salhack.summit.module.world;

import com.salhack.summit.events.MinecraftEvent;
import com.salhack.summit.events.bus.EventHandler;
import com.salhack.summit.events.bus.Listener;
import com.salhack.summit.events.player.EventPlayerMotionUpdate;
import com.salhack.summit.events.player.EventPlayerUpdate;
import com.salhack.summit.gui.hud.items.ArmorComponent;
import com.salhack.summit.module.Module;
import com.salhack.summit.module.Value;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemExpBottle;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.CPacketPlayerTryUseItem;
import net.minecraft.util.EnumHand;

public final class FastPlace extends Module
{

    public final Value<Boolean> xp = new Value<Boolean>("XP", new String[]
    { "EXP" }, "Only activate while holding XP bottles.", false);
    public final Value<Boolean> Crystals = new Value<Boolean>("Crystals", new String[]
    { "Cry" }, "Active only when using crystals", false);
    public final Value<Boolean> AutoXP = new Value<Boolean>("AutoXP", new String[] {"AutoEXP"}, "Automatically uses XP at your feet when hovered over", false);
    public final Value<Boolean> ArmorCheck = new Value<Boolean>("ArmorCheck", new String[] {"ArmorCheck"}, "Checks if your armor is already at full durability before processing AutoXP", true);

    public FastPlace()
    {
        super("FastPlace", new String[]
        { "Fp" }, "Removes place delay", "NONE", 0x66DB24, ModuleType.WORLD);
        setMetaData("EXP:0");
    }

    @Override
    public void onDisable()
    {
        super.onDisable();
        mc.rightClickDelayTimer = 6;
    }

    public String getMetaData()
    {
        if (xp.getValue())
            return "EXP:" + this.getItemCount(Items.EXPERIENCE_BOTTLE);

        return null;
    }

    @EventHandler
    private Listener<EventPlayerUpdate> OnPlayerUpdate = new Listener<>(p_Event ->
    {
        setMetaData(getMetaData());
        
        if (this.xp.getValue())
        {
            if (mc.player.getHeldItemMainhand().getItem() instanceof ItemExpBottle
                    || mc.player.getHeldItemOffhand().getItem() instanceof ItemExpBottle)
            {
                mc.rightClickDelayTimer = 0;
            }
        }
        else if (Crystals.getValue())
        {
            if (mc.player.inventory.getCurrentItem().getItem() == Items.END_CRYSTAL || mc.player.getHeldItemOffhand().getItem() == Items.END_CRYSTAL)
                mc.rightClickDelayTimer = 0;
        }
        else
        {
            mc.rightClickDelayTimer = 0;
        }
    });
    
    @EventHandler
    private Listener<EventPlayerMotionUpdate> onMotionUpdate = new Listener<>(event ->
    {
        if (event.getStage() != MinecraftEvent.Stage.Pre || event.isCancelled())
            return;
        
        if (ArmorCheck.getValue())
        {
            boolean skip = false;
            
            for (ItemStack stack : mc.player.getArmorInventoryList())
            {
                if (ArmorComponent.GetPctFromStack(stack) < 100f)
                {
                    skip = true;
                    break;
                }
            }
            
            if (!skip)
                return;
        }
        
        if (mc.player.getHeldItemMainhand().getItem() == Items.EXPERIENCE_BOTTLE && AutoXP.getValue())
        {
            event.cancel();
            event.setPitch(90f);
            event.setYaw(mc.player.rotationYaw);
            mc.player.connection.sendPacket(new CPacketPlayerTryUseItem(EnumHand.MAIN_HAND));
        }
    });

    private int getItemCount(Item input)
    {
        if (mc.player == null)
            return 0;
        
        int items = 0;

        for (int i = 0; i < 45; i++)
        {
            final ItemStack stack = mc.player.inventory.getStackInSlot(i);
            if (stack.getItem() == input)
            {
                items += stack.getCount();
            }
        }

        return items;
    }
}
