package com.salhack.summit.module.movement;

import com.salhack.summit.events.MinecraftEvent;
import com.salhack.summit.events.bus.EventHandler;
import com.salhack.summit.events.bus.Listener;
import com.salhack.summit.events.client.EventClientTick;
import com.salhack.summit.events.network.EventClientPacket;
import com.salhack.summit.events.player.EventPlayerMotionUpdate;
import com.salhack.summit.events.player.EventPlayerUpdateMoveState;
import com.salhack.summit.gui.SalGuiScreen;
import com.salhack.summit.gui.chat.SalGuiChat;
import com.salhack.summit.main.SummitStatic;
import com.salhack.summit.module.Module;
import com.salhack.summit.module.Value;
import com.salhack.summit.util.entity.PlayerUtil;
import net.minecraft.client.gui.GuiChat;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.item.ItemShield;
import net.minecraft.network.play.client.CPacketClickWindow;
import net.minecraft.network.play.client.CPacketEntityAction;
import net.minecraft.network.play.client.CPacketPlayerDigging;
import net.minecraft.network.play.client.CPacketPlayerDigging.Action;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import org.lwjgl.input.Keyboard;

public final class NoSlow extends Module
{
    public final Value<Boolean> InventoryMove = new Value<Boolean>("InventoryMove", new String[]
    { "InvMove", "InventoryMove", "GUIMove" }, "Allows you to move while guis are open", true);
    public final Value<Boolean> OnlyOnCustom = new Value<Boolean>("OnlyOnCustom", new String[]
    { "Custom" }, "Only inventory move on custom GUIs", true);
    public final Value<Boolean> noInputGUIs = new Value<Boolean>("NoInputGUIs", new String[]
            { "NoConsole" }, "Doesn't Inventory move on the Console / Chat GUI.", true);
    public final Value<Boolean> items = new Value<Boolean>("Items", new String[]
    { "it" }, "Disables the slowness from using items (shields, eating, etc).", true);
    public final Value<Boolean> NCPStrict = new Value<Boolean>("NCPStrict", new String[]{"NCP"}, "Allows NoSlow to work on nocheatplus", true);

    public NoSlow()
    {
        super("NoSlow", new String[]
        { "AntiSlow", "NoSlowdown", "AntiSlowdown", "InventoryMove" }, "Allows you to move faster with things that slow you down", "NONE", 0x2460DB, ModuleType.MOVEMENT);
    }
    
    @EventHandler
    private Listener<EventPlayerUpdateMoveState> OnIsKeyPressed = new Listener<>(event ->
    {
        if (InventoryMove.getValue() && mc.currentScreen != null)
        {
            if (OnlyOnCustom.getValue())
            {
                if (!(mc.currentScreen instanceof SalGuiScreen))
                    return;
            }

            if (noInputGUIs.getValue())
            {
                if (mc.currentScreen instanceof GuiChat)
                {
                    return;
                }

                if (mc.currentScreen instanceof SalGuiChat)
                {
                    return;
                }
            }

            if (!(mc.currentScreen instanceof SalGuiScreen))
            {

                if (Keyboard.isKeyDown(200))
                {
                    SalGuiScreen.UpdateRotationPitch(-5.0f);
                }
                if (Keyboard.isKeyDown(208))
                {
                    SalGuiScreen.UpdateRotationPitch(5.0f);
                }
                if (Keyboard.isKeyDown(205))
                {
                    SalGuiScreen.UpdateRotationYaw(5.0f);
                }

                if (Keyboard.isKeyDown(203))
                {
                    SalGuiScreen.UpdateRotationYaw(-5.0f);
                }
            }
            
            if (SummitStatic.AUTOWALK.isEnabled())
                return;

            mc.player.movementInput.moveStrafe = 0.0F;
            mc.player.movementInput.moveForward = 0.0F;
            
            KeyBinding.setKeyBindState(mc.gameSettings.keyBindForward.getKeyCode(), Keyboard.isKeyDown(mc.gameSettings.keyBindForward.getKeyCode()));
            if (Keyboard.isKeyDown(mc.gameSettings.keyBindForward.getKeyCode()))
            {
                ++mc.player.movementInput.moveForward;
                mc.player.movementInput.forwardKeyDown = true;
            }
            else
            {
                mc.player.movementInput.forwardKeyDown = false;
            }

            KeyBinding.setKeyBindState(mc.gameSettings.keyBindBack.getKeyCode(), Keyboard.isKeyDown(mc.gameSettings.keyBindBack.getKeyCode()));
            if (Keyboard.isKeyDown(mc.gameSettings.keyBindBack.getKeyCode()))
            {
                --mc.player.movementInput.moveForward;
                mc.player.movementInput.backKeyDown = true;
            }
            else
            {
                mc.player.movementInput.backKeyDown = false;
            }

            KeyBinding.setKeyBindState(mc.gameSettings.keyBindLeft.getKeyCode(), Keyboard.isKeyDown(mc.gameSettings.keyBindLeft.getKeyCode()));
            if (Keyboard.isKeyDown(mc.gameSettings.keyBindLeft.getKeyCode()))
            {
                ++mc.player.movementInput.moveStrafe;
                mc.player.movementInput.leftKeyDown = true;
            }
            else
            {
                mc.player.movementInput.leftKeyDown = false;
            }

            KeyBinding.setKeyBindState(mc.gameSettings.keyBindRight.getKeyCode(), Keyboard.isKeyDown(mc.gameSettings.keyBindRight.getKeyCode()));
            if (Keyboard.isKeyDown(mc.gameSettings.keyBindRight.getKeyCode()))
            {
                --mc.player.movementInput.moveStrafe;
                mc.player.movementInput.rightKeyDown = true;
            }
            else
            {
                mc.player.movementInput.rightKeyDown = false;
            }

            KeyBinding.setKeyBindState(mc.gameSettings.keyBindJump.getKeyCode(), Keyboard.isKeyDown(mc.gameSettings.keyBindJump.getKeyCode()));
            mc.player.movementInput.jump = Keyboard.isKeyDown(mc.gameSettings.keyBindJump.getKeyCode());
        }
    });

    @EventHandler
    private Listener<EventClientTick> OnTick = new Listener<>(p_Event ->
    {
        if (mc.player.isHandActive())
        {
            if (mc.player.getHeldItem(mc.player.getActiveHand()).getItem() instanceof ItemShield)
            {
                if (mc.player.movementInput.moveStrafe != 0 || mc.player.movementInput.moveForward != 0 && mc.player.getItemInUseMaxCount() >= 8)
                {
                    mc.player.connection.sendPacket(new CPacketPlayerDigging(CPacketPlayerDigging.Action.RELEASE_USE_ITEM, BlockPos.ORIGIN, mc.player.getHorizontalFacing()));
                }
            }
        }
    });

    @EventHandler
    private Listener<EventPlayerUpdateMoveState> OnUpdateMoveState = new Listener<>(event ->
    {
        if (items.getValue() && mc.player.isHandActive() && !mc.player.isRiding())
        {
            mc.player.movementInput.moveForward /= 0.2F;
            mc.player.movementInput.moveStrafe /= 0.2F;
        }
    });
    
    @EventHandler
    private Listener<EventPlayerMotionUpdate> onMotionUpdate = new Listener<>(event ->
    {
        if (event.getStage() == MinecraftEvent.Stage.Post && NCPStrict.getValue())
        {
            if (items.getValue() && mc.player.isHandActive() && !mc.player.isRiding())
                mc.player.connection.sendPacket(new CPacketPlayerDigging(Action.ABORT_DESTROY_BLOCK, PlayerUtil.GetLocalPlayerPosFloored(), EnumFacing.DOWN));
        }
    });

    @EventHandler
    private Listener<EventClientPacket> onClientPacket = new Listener<>(event -> {
        if (NCPStrict.getValue() && event.getPacket() instanceof CPacketClickWindow) {

            if (event.getStage() == MinecraftEvent.Stage.Pre) {
                if (mc.player.isActiveItemStackBlocking()) {
                    mc.playerController.onStoppedUsingItem(mc.player);
                }
                if (mc.player.isSneaking())
                    mc.player.connection.sendPacket(new CPacketEntityAction(mc.player, CPacketEntityAction.Action.STOP_SNEAKING));
                if (mc.player.isSprinting())
                    mc.player.connection.sendPacket(new CPacketEntityAction(mc.player, CPacketEntityAction.Action.STOP_SPRINTING));
            }
            else {
                if (mc.player.isSneaking())
                    mc.player.connection.sendPacket(new CPacketEntityAction(mc.player, CPacketEntityAction.Action.START_SNEAKING));
                if (mc.player.isSprinting())
                    mc.player.connection.sendPacket(new CPacketEntityAction(mc.player, CPacketEntityAction.Action.START_SPRINTING));
            }
        }
    });
}
