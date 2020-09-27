package com.salhack.summit.module.misc;

import java.io.BufferedWriter;
import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

import com.salhack.summit.events.MinecraftEvent.Stage;
import com.salhack.summit.events.bus.EventHandler;
import com.salhack.summit.events.bus.Listener;
import com.salhack.summit.events.network.EventClientPacket;
import com.salhack.summit.events.network.EventServerPacket;
import com.salhack.summit.events.player.EventPlayerMove;
import com.salhack.summit.events.player.EventPlayerTravel;
import com.salhack.summit.main.Summit;
import com.salhack.summit.module.Module;
import com.salhack.summit.module.Value;
import com.salhack.summit.util.Timer;

import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.network.play.server.SPacketEntityHeadLook;
import net.minecraft.network.play.server.SPacketMoveVehicle;
import net.minecraft.util.StringUtils;

public class PacketLogger extends Module
{
    public final Value<Boolean> Client = new Value<>("Client", new String[] {"CMSG"}, "Logs client packets", true);
    public final Value<Boolean> Server = new Value<>("Server", new String[] {"SMSG"}, "Logs server packets", true);
    public final Value<Boolean> Data = new Value<>("Data", new String[] {"Datas"}, "Logs data in packets", true);
    public final Value<Boolean> Save = new Value<>("Save", new String[] {"Save"}, "Saves to a file", false);
    public final Value<Boolean> PlayerOnly = new Value<>("PlayerOnly", new String[] {"PlayerOnly"}, "Only logs player packets", false);
    public final Value<Boolean> Chat = new Value<>("Chat", new String[] {"Save"}, "Saves to a file", false);
    
    public PacketLogger()
    {
        super("PacketLogger", new String[] {"PacketSniffer", "Packets"}, "Allows you to log certain types of packets", "NONE", -1, ModuleType.MISC);
    }
    
    private Timer timer = new Timer();
    
    @Override
    public void toggleNoSave()
    {
    }
    
    private long startTimer;
    
    @Override
    public void onEnable()
    {
        super.onEnable();
        
        startTimer = System.currentTimeMillis();
    }
    
    public void SendMessage(String msg, boolean client)
    {
        if (Chat.getValue())
            Summit.SendMessage(msg);

        if (!Save.getValue())
            return;
        
        msg += "\n";
        
        try
        {
            Path path = Paths.get(Summit.GetDirectoryManager().GetCurrentDirectory() + "/Summit/PacketLogger/", startTimer + "SMSG.txt");
            
            if (client)
                path = Paths.get(Summit.GetDirectoryManager().GetCurrentDirectory() + "/Summit/PacketLogger/", startTimer + "CMSG.txt");
            
            try (BufferedWriter writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8, StandardOpenOption.APPEND, StandardOpenOption.CREATE))
            {
                writer.write(msg);
            } 
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }
        catch (Exception e)
        {
            
        }
    }
    
    private int totalPackets = 0;
    
    @EventHandler
    private Listener<EventClientPacket> onClientPacket = new Listener<>(event ->
    {
        if (event.getStage() != Stage.Pre || !Client.getValue())
            return;
        
        if (PlayerOnly.getValue() && !(event.getPacket() instanceof CPacketPlayer))
            return;

        SendMessage(String.format("MotionY: %s", mc.player.motionY), true);

        SendMessage("[Client] " + event.getPacket().getClass().getSimpleName(), true);
        
        if (!Data.getValue())
            return;
        
        try
        {
            Class<?> clazz = event.getPacket().getClass();
    
            while (clazz != Object.class)
            {
                for (Field field : clazz.getDeclaredFields())
                {
                    if (field != null)
                    {
                        if (!field.isAccessible())
                            field.setAccessible(true);
                        
                        SendMessage(StringUtils.stripControlCodes("      " + field.getType().getSimpleName() + " " + field.getName() + " = " + field.get(event.getPacket())), true);
                    }
                }
    
                clazz = clazz.getSuperclass();
            }
        }
        catch (Exception e)
        {
            
        }
    });

    @EventHandler
    private Listener<EventServerPacket> onServerPacket = new Listener<>(event ->
    {
        if (event.getStage() != Stage.Pre || !Server.getValue())
            return;
        
        if (event.getPacket() instanceof SPacketMoveVehicle || event.getPacket() instanceof SPacketEntityHeadLook)
            return;

        SendMessage("[Server] " + event.getPacket().getClass().getSimpleName(), false);

        if (!Data.getValue())
            return;
        
        try
        {
            Class<?> clazz = event.getPacket().getClass();
    
            while (clazz != Object.class)
            {
                for (Field field : clazz.getDeclaredFields())
                {
                    if (field != null)
                    {
                        if (!field.isAccessible())
                            field.setAccessible(true);
                        
                        SendMessage(StringUtils.stripControlCodes("      " + field.getType().getSimpleName() + " " + field.getName() + " = " + field.get(event.getPacket())), false);
                    }
                }
    
                clazz = clazz.getSuperclass();
            }
        }
        catch (Exception e)
        {
            
        }
    });
}
