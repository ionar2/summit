package com.salhack.summit.module.misc;

import com.salhack.summit.events.MinecraftEvent;
import com.salhack.summit.events.bus.EventHandler;
import com.salhack.summit.events.bus.Listener;
import com.salhack.summit.events.network.EventServerPacket;
import com.salhack.summit.main.Summit;
import com.salhack.summit.module.Module;
import com.salhack.summit.module.Value;
import net.minecraft.client.Minecraft;
import net.minecraft.init.SoundEvents;
import net.minecraft.network.play.server.SPacketEffect;
import net.minecraft.network.play.server.SPacketSoundEffect;
import net.minecraft.network.play.server.SPacketSpawnMob;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;

public final class GlobalLocation extends Module
{
    public final Value<Boolean> thunder = new Value<Boolean>("Thunder", new String[]
    { "thund" }, "Logs positions of thunder/lightning sounds.", true);
    public final Value<Boolean> slimes = new Value<Boolean>("Slimes", new String[]
    { "slime" }, "Logs positions of slime spawns.", false);
    public final Value<Boolean> Wither = new Value<Boolean>("Wither", new String[]
    { "Wither" }, "Logs positions of Wither spawns.", false);
    public final Value<Boolean> EndPortal = new Value<Boolean>("End Portal", new String[]
    { "EndPortal" }, "Logs positions of EndPortal spawns.", false);
    public final Value<Boolean> EnderDragon = new Value<Boolean>("Ender Dragon", new String[]
    { "ED" }, "Logs positions of EnderDragon spawns.", false);
    public final Value<Boolean> Donkey = new Value<Boolean>("Donkey", new String[]
    { "Donkey" }, "logs location of donkey spawns", false);

    public GlobalLocation()
    {
        super("GlobalLocation", new String[]
        { "WitherLocationModule" }, "Logs in chat where a global sound happened (Warning can send current location if server changed the packet!)", "NONE", 0xDBB024, ModuleType.MISC);
    }

    @EventHandler
    private Listener<EventServerPacket> onServerPacket = new Listener<>(event ->
    {
        if (event.getStage() != MinecraftEvent.Stage.Pre)
            return;
        
        if (event.getPacket() instanceof SPacketSpawnMob)
        {
            final SPacketSpawnMob packet = (SPacketSpawnMob) event.getPacket();
            if (this.slimes.getValue())
            {
                final Minecraft mc = Minecraft.getMinecraft();

                if (packet.getEntityType() == 55 && packet.getY() <= 40 && !mc.world.getBiome(mc.player.getPosition()).getBiomeName().toLowerCase().contains("swamp"))
                {
                    final BlockPos pos = new BlockPos(packet.getX(), packet.getY(), packet.getZ());
                    Summit.SendMessage("Slime Spawned in chunk X:" + mc.world.getChunk(pos).x + " Z:" + mc.world.getChunk(pos).z);
                }
            }

            if (this.Donkey.getValue() && packet.getEntityType() == 31)
            {
                Summit.SendMessage(String.format("Donkey spawned at %s %s %s", packet.getX(), packet.getY(), packet.getZ()));
            }
        }
        else if (event.getPacket() instanceof SPacketSoundEffect)
        {
            final SPacketSoundEffect packet = (SPacketSoundEffect) event.getPacket();
            if (this.thunder.getValue())
            {
                if (packet.getCategory() == SoundCategory.WEATHER && packet.getSound() == SoundEvents.ENTITY_LIGHTNING_THUNDER)
                {
                    float yaw = 0;
                    final double difX = packet.getX() - Minecraft.getMinecraft().player.posX;
                    final double difZ = packet.getZ() - Minecraft.getMinecraft().player.posZ;

                    yaw += MathHelper.wrapDegrees((Math.toDegrees(Math.atan2(difZ, difX)) - 90.0f) - yaw);

                    Summit.SendMessage("Lightning spawned X:" + Minecraft.getMinecraft().player.posX + " Z:" + Minecraft.getMinecraft().player.posZ + " Angle:" + yaw);
                }
            }
        }
        else if (event.getPacket() instanceof SPacketEffect)
        {
            final SPacketEffect packet = (SPacketEffect) event.getPacket();
            if (packet.getSoundType() == 1023 && Wither.getValue())
            {
                double theta = Math.atan2(packet.getSoundPos().getZ() - Minecraft.getMinecraft().player.posZ, packet.getSoundPos().getX() - Minecraft.getMinecraft().player.posX);
                theta += Math.PI / 2.0;
                double angle = Math.toDegrees(theta);
                if (angle < 0)
                    angle += 360;
                angle -= 180;

                Summit.SendMessage("Wither spawned in direction " + angle + " with y position: " + packet.getSoundPos().getY());
                // salhack.INSTANCE.logChat("Wither spawned at " + packet.getSoundPos().toString() + "You currently are at: X:" + Minecraft.getMinecraft().player.posX + " Z:" +
                // Minecraft.getMinecraft().player.posZ);
            }
            else if (packet.getSoundType() == 1038 && EndPortal.getValue())
            {
                Summit.SendMessage("End portal spawned at " + packet.getSoundPos().toString());
            }
            else if (packet.getSoundType() == 1028 && EnderDragon.getValue())
            {
                Summit.SendMessage("Ender dragon died at " + packet.getSoundPos().toString());
            }
        }
    });
}
