package com.salhack.summit.module.combat;

import akka.io.UdpConnected;
import com.mojang.realmsclient.gui.ChatFormatting;
import com.salhack.summit.events.bus.EventHandler;
import com.salhack.summit.events.bus.Listener;
import com.salhack.summit.events.player.EventPlayerUpdate;
import com.salhack.summit.module.Module;
import com.salhack.summit.module.Value;
import com.salhack.summit.util.CrystalUtils;
import com.salhack.summit.util.entity.PlayerUtil;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.entity.item.EntityEnderCrystal;
import net.minecraft.init.Items;
import net.minecraft.init.SoundEvents;
import net.minecraft.network.play.server.SPacketDisconnect;
import net.minecraft.util.text.TextComponentString;

import java.util.Comparator;

public class AutoLog extends Module {
    public final Value<Float> MinHealth = new Value<>("MinHealth", new String[]{"MinHealth"}, "HP to logout at, 2 per heart.", 16.0f, 0.0f, 20.0f, 1.0f);
    public final Value<Boolean> NoTotems = new Value<>("NoTotems", new String[]{"NoTotems"}, "Only logs out when no totems are left.", true);
    public final Value<Boolean> LethalCrystals = new Value<>("LethalCrystals", new String[]{"LethalCrystals"}, "Logs out when a lethal crystal is detected.", false);

    public AutoLog() {
        super("AutoLog", new String[]{"AutoLog"}, "Automatically logs out before death.", "NONE", 0xDA2100, ModuleType.COMBAT);
    }

    // Created by Jumpinqq
    // Last updated on 7/31/20

    float potentialCrystalDamage = 0;
    boolean isCrystalLethal = false;

    @EventHandler
    private final Listener<EventPlayerUpdate> OnPlayerUpdate = new Listener<>(event -> {
        if (mc.player == null || mc.getConnection() == null || mc.world == null) return;

        float playerHealth = PlayerUtil.GetHealthWithAbsorption();

        if (LethalCrystals.getValue()) {
            EntityEnderCrystal crystal = mc.world.getLoadedEntityList().stream()
                    .filter(entity -> entity instanceof EntityEnderCrystal)
                    .map(entity -> (EntityEnderCrystal) entity)
                    .max(Comparator.comparing(this::calculateSelfDamage))
                    .orElse(null);

            if (crystal != null) {
                potentialCrystalDamage = calculateSelfDamage(crystal);
                if (potentialCrystalDamage > playerHealth - 8) isCrystalLethal = true;
            }
            else {
                potentialCrystalDamage = 0;
                isCrystalLethal = false;
            }
        }

        boolean healthCheck = playerHealth <= MinHealth.getValue();

        int totemCount = PlayerUtil.GetItemCount(Items.TOTEM_OF_UNDYING);
        int playerHealthToInt = (int) playerHealth;

        if (healthCheck || LethalCrystals.getValue() && isCrystalLethal) {
            if (totemCount == 0 || !NoTotems.getValue()) {
                mc.getSoundHandler().playSound(PositionedSoundRecord.getRecord(SoundEvents.ENTITY_ENDERMEN_TELEPORT, 1.0f, 1.0f));
                sendDisconnectMessage("Logged out with " + ChatFormatting.YELLOW + totemCount + " Totems " + ChatFormatting.RESET + "remaining, and " + healthToColor(playerHealth) + playerHealthToInt + " hp.");
                toggle();
            }
        }
    });

    @Override
    public void onDisable() {
        super.onDisable();
        isCrystalLethal = false;
        potentialCrystalDamage = 0;
    }

    private ChatFormatting healthToColor(double health) {
        if (health <= 6) return ChatFormatting.RED;
        else if (health <= 10) return ChatFormatting.YELLOW;
        else if (health <= 20) return ChatFormatting.GREEN;

        return ChatFormatting.RESET;
    }

    private void sendDisconnectMessage(String message) {
        if (mc.getConnection() != null) {
            mc.getConnection().handleDisconnect(new SPacketDisconnect(new TextComponentString( ChatFormatting.RED + "[AutoLog] " + ChatFormatting.RESET + message)));
        }
    }

    private float calculateSelfDamage(EntityEnderCrystal crystal) {
        return CrystalUtils.calculateDamage(mc.world, crystal.posX, crystal.posY, crystal.posZ, mc.player, 0);
    }
}