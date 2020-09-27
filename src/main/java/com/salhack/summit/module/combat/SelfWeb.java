package com.salhack.summit.module.combat;

import java.util.function.Consumer;

import com.mojang.realmsclient.gui.ChatFormatting;
import com.salhack.summit.events.MinecraftEvent.Stage;
import com.salhack.summit.events.bus.EventHandler;
import com.salhack.summit.events.bus.Listener;
import com.salhack.summit.events.player.EventPlayerMotionUpdate;
import com.salhack.summit.main.SummitStatic;
import com.salhack.summit.managers.FriendManager;
import com.salhack.summit.module.Module;
import com.salhack.summit.module.Value;
import com.salhack.summit.util.BlockInteractionHelper;
import com.salhack.summit.util.MathUtil;
import com.salhack.summit.util.BlockInteractionHelper.ValidResult;
import com.salhack.summit.util.entity.PlayerUtil;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

public class SelfWeb extends Module
{
    // Created by Jumpinqq on 6/24/20
    private Value<String> Mode = new Value<>("Mode", new String[]{"Modes"}, "Choose the mode!", "Distance");
    private Value<Float> validDistance = new Value<>("ValidDistance", new String[]{"ValidDistance"}, "The range at which other players will trigger SelfWeb.", 2f, 0f, 10f, 1f);
    public final Value<Boolean> Double = new Value<>("Double", new String[]{"Double"}, "Places two blocks", true);
    public final Value<Boolean> Toggles = new Value<>("Toggles", new String[]{"Toggles"}, "Toggles after a place", true);
    public final Value<Boolean> ToggleCA = new Value<>("ToggleCA", new String[]{"ToggleCA"}, "Toggles CA on after webs are placed.", true);

    public SelfWeb()
    {
        super("SelfWeb", new String[]{"SelfWeb"}, "Places webs on you automatically", "NONE", -1, Module.ModuleType.COMBAT);
        Mode.addString("Distance");
        Mode.addString("Toggle");
    }

    @EventHandler
    private Listener<EventPlayerMotionUpdate> onMotionUpdate = new Listener<>(event ->
    {
        if (event.getStage() != Stage.Pre || event.isCancelled()) return;

        if (PlayerUtil.isPlayerInHole(Blocks.OBSIDIAN))
        {
            if (Mode.getValue().equals("Distance"))
            {
                boolean shouldTrigger = false;

                for (EntityPlayer player : mc.world.playerEntities)
                {
                    if (player instanceof EntityPlayerSP || FriendManager.Get().IsFriend(player)) continue;

                    if (mc.player.getDistance(player) < validDistance.getValue())
                    {
                        shouldTrigger = true;
                        break;
                    }
                }

                if (!shouldTrigger) return;
            }

            int webSlot = PlayerUtil.GetItemSlotInHotbar(Blocks.WEB);

            if (webSlot == -1) return;

            final Vec3d pos = MathUtil.interpolateEntity(mc.player, mc.getRenderPartialTicks());
            BlockPos blockPos = new BlockPos(pos.x, pos.y, pos.z);

            IBlockState state = mc.world.getBlockState(blockPos);

            if (state.getBlock() == Blocks.WEB)
            {
                boolean valid = true;

                if (Double.getValue())
                {
                    blockPos = blockPos.up();
                    state = mc.world.getBlockState(blockPos);

                    if (state.getBlock() != Blocks.WEB) valid = false;
                }

                if (valid)
                {
                    if (ToggleCA.getValue() && !SummitStatic.AUTOCRYSTAL.isEnabled())
                    {
                        SummitStatic.AUTOCRYSTAL.toggle();
                    }
                    if (Toggles.getValue())
                    {
                        SendMessage("Finished");
                        toggle();
                        return;
                    }
                }
            }

            final BlockPos interpPos = blockPos;

            if (BlockInteractionHelper.valid(interpPos, true) != ValidResult.Ok) return;

            final int prevSlot = mc.player.inventory.currentItem;

            mc.player.inventory.currentItem = webSlot;
            mc.playerController.updateController();

            event.cancel();

            float rotations[] = BlockInteractionHelper.getLegitRotations(new Vec3d(interpPos.getX() - 0.5f, interpPos.getY() - 0.5f, interpPos.getZ() - 0.5f));
            event.setYaw(rotations[0]);
            event.setPitch(90);

            Consumer<EntityPlayerSP> post = p ->
            {
                BlockInteractionHelper.place(interpPos, 5.0f, false, false);

                mc.player.inventory.currentItem = prevSlot;
                mc.playerController.updateController();
            };

            event.setFunct(post);
        }

        // if player is not in obsidian hole
        else
        {
            SendMessage(ChatFormatting.RED + "You're not in a hole! Toggling...");
            toggle();
        }
    });
}