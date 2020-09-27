package com.salhack.summit.module.misc;

import com.salhack.summit.events.entity.EventItemUseFinish;
import com.salhack.summit.module.Module;
import com.salhack.summit.events.bus.EventHandler;
import com.salhack.summit.events.bus.Listener;
import net.minecraft.item.ItemChorusFruit;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.network.play.server.SPacketPlayerPosLook;
import net.minecraft.util.math.MathHelper;

public class ChorusFruitBypass extends Module
{
    public ChorusFruitBypass()
    {
        super ("ChorusBypass", new String[] {"ChorusFruit"}, "Bypass chorus fruit", "NONE", -1, ModuleType.MISC);
    }
    
    private boolean needChorusFruitBypass = false;
    
    public boolean handlePlayerPosLook(SPacketPlayerPosLook packet)
    {
        if (!needChorusFruitBypass)
            return true;
        needChorusFruitBypass = false;
        SendMessage("SPacketPlayerPosLook!");
        
        SendMessage(new StringBuilder("X: ").append(MathHelper.floor(packet.getX())).
                append(" Y: ").append(MathHelper.floor(packet.getY())).
                append(" Z: ").append(MathHelper.floor(packet.getZ())).toString());
        
        SendMessage(new StringBuilder("X: ").append(MathHelper.floor(mc.player.posX)).
                append(" Y: ").append(MathHelper.floor(mc.player.posY)).
                append(" Z: ").append(MathHelper.floor(mc.player.posZ)).toString());
        
        int x = MathHelper.floor(packet.getX());
        int y = MathHelper.floor(packet.getY());
        int z = MathHelper.floor(packet.getZ());
    
        int playerX = MathHelper.floor(mc.player.posX);
        int playerY = MathHelper.floor(mc.player.posY);
        int playerZ = MathHelper.floor(mc.player.posZ);
        //mc.getConnection().sendPacket(new CPacketPlayer.PositionRotation(packet.getX(), packet.getY()+420, packet.getZ(), packet.getYaw(), packet.getPitch(), false));
        mc.player.setPosition(packet.getX(), packet.getY(), packet.getZ());
        return true;
    }
    
    @EventHandler
    private Listener<EventItemUseFinish> onItemUseFinish = new Listener<>(event ->
    {
        if (!event.getActive().isEmpty() && mc.player.isHandActive() && event.getEntity() == mc.player)
        {
            if (event.getActive().getItem() instanceof ItemChorusFruit)
            {
                needChorusFruitBypass = true;
                SendMessage("Ate a chorus fruit");
                
                mc.getConnection().sendPacket(new CPacketPlayer.Position(mc.player.posX, mc.player.posY, mc.player.posZ, mc.player.onGround));
              //  mc.getConnection().sendPacket(new CPacketPlayer.Position(mc.player.posX+4242, mc.player.posY, mc.player.posZ, false));
                
            }
        }
    });
}
