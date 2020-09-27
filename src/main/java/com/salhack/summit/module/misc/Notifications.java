package com.salhack.summit.module.misc;

import java.awt.Image;
import java.awt.SystemTray;
import java.awt.Toolkit;
import java.awt.TrayIcon;
import java.awt.TrayIcon.MessageType;

import com.salhack.summit.events.MinecraftEvent;
import com.salhack.summit.events.entity.EventEntityAdded;
import com.salhack.summit.events.network.EventServerPacket;
import com.salhack.summit.events.player.EventPlayerUpdate;
import com.salhack.summit.main.SummitStatic;
import com.salhack.summit.managers.FriendManager;
import com.salhack.summit.module.Module;
import com.salhack.summit.module.Value;
import com.salhack.summit.events.bus.EventHandler;
import com.salhack.summit.events.bus.Listener;
import com.salhack.summit.util.Timer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.play.server.SPacketChat;
import net.minecraft.network.play.server.SPacketDisconnect;
import net.minecraft.network.play.server.SPacketEntityStatus;
import net.minecraft.network.play.server.SPacketUpdateHealth;

public class Notifications extends Module
{
    public final Value<Boolean> Queue = new Value<>("Queue", new String[] {"Q"}, "Q", true);
    public final Value<Boolean> Kick = new Value<>("Kick", new String[] {"Q"}, "Q", true);
    public final Value<Boolean> PM = new Value<>("PM", new String[] {"Q"}, "Q", true);
    public final Value<Boolean> Name = new Value<>("Name", new String[] {"Q"}, "Q", true);
    public final Value<Boolean> Stuck = new Value<>("Stuck", new String[] {"Q"}, "Q", true);
    public final Value<Boolean> Damage = new Value<>("Damage", new String[] {"Q"}, "Q", true);
    public final Value<Boolean> Totem = new Value<>("Totem", new String[] {"Q"}, "Q", true);
    public final Value<Boolean> Nearby = new Value<>("Nearby", new String[] {"Q"}, "Q", true);
    public final Value<Boolean> ShowFriends = new Value<>("Show Friends", new String[] {"Q"}, "Q", true);

    public Notifications()
    {
        super("Notifications", new String[] {"WinNotifications"}, "Sends you notifications when the game is not focused when events happen", "NONE", 0x990E0E, ModuleType.MISC);
    }

    private SystemTray tray;
    private Image image;
    private TrayIcon trayIcon;
    
    private Timer timer = new Timer();
    private Timer healthTimer = new Timer();
    private Timer totemPopTimer = new Timer();
    private Timer chatTimer = new Timer();
    
    private int prevDimension = -1337;
    
    @Override
    public void init()
    {
        tray = SystemTray.getSystemTray();
        
        String path = getClass().getResource("/assets/salhack/imgs/summitlogo.png").getPath();
        
        image = Toolkit.getDefaultToolkit().createImage(path);
        trayIcon = new TrayIcon(image, "Summit");
        
        //Let the system resize the image if needed
        trayIcon.setImageAutoSize(true);
        //Set tooltip text for the tray icon
        trayIcon.setToolTip("Summit Notifications");
    }
    
    @Override
    public void onEnable()
    {
        super.onEnable();
        
        try
        {
            tray.add(trayIcon);
        }
        catch (Exception e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        
        sendNotificationIfNeed("Initalized Summit Notifications Module!");
    }
    
    @Override
    public void onDisable()
    {
        super.onDisable();
    }
    
    private void sendNotificationIfNeed(String msg)
    {
        try
        {
            trayIcon.displayMessage("Summit", msg, MessageType.INFO);
        }
        catch (Exception e)
        {
            
        }
    }

    @EventHandler
    private Listener<EventPlayerUpdate> onPlayerUpdate = new Listener<>(event ->
    {
        if (Minecraft.getMinecraft().inGameHasFocus)
            return;
        
        if (!timer.passed(10000))
            return;
        
        if (SummitStatic.AUTOWALK.isEnabled() && Stuck.getValue())
        {
            if (Math.abs(mc.player.motionX) < 0.1 && Math.abs(mc.player.motionZ) < 0.1)
            {
                sendNotificationIfNeed("AutoWalk has detected you are stuck.");
                timer.reset();
            }
        }

        if (prevDimension != mc.player.dimension)
        {
            if (prevDimension == 1 && mc.getCurrentServerData() != null && mc.getCurrentServerData().serverIP.equals("2b2t.org") && Queue.getValue()) // end (2b queue)
                sendNotificationIfNeed("You've finished going through the queue.");
            
            prevDimension = mc.player.dimension;
        }
    });
    
    @EventHandler
    private Listener<EventEntityAdded> onEntityAdd = new Listener<>(event ->
    {
        if (!Minecraft.getMinecraft().inGameHasFocus && Nearby.getValue() && !(event.GetEntity() instanceof EntityPlayerSP))
        {
            if (event.GetEntity() instanceof EntityPlayer && (ShowFriends.getValue() ? true : !FriendManager.Get().IsFriend(event.GetEntity())))
            {
                sendNotificationIfNeed(new StringBuilder(event.GetEntity().getName()).append(" ").append("has just came into your render distance!").toString());
            }
        }
    });
    
    @EventHandler
    private Listener<EventServerPacket> onServerPacket = new Listener<>(event ->
    {
        if (event.getStage() != MinecraftEvent.Stage.Pre)
            return;
        
        if (Minecraft.getMinecraft().inGameHasFocus)
            return;
        
        try
        {
            if (event.getPacket() instanceof SPacketEntityStatus && Totem.getValue())
            {
                if (!totemPopTimer.passed(10000))
                    return;
                if (mc.world == null)
                    return;
                
                SPacketEntityStatus packet = (SPacketEntityStatus) event.getPacket();
                
                if (packet.getEntity(mc.world) == mc.player && packet.getOpCode() == 35)
                {
                    totemPopTimer.reset();
                    sendNotificationIfNeed("You just popped a totem.");
                }
            }
            else if (event.getPacket() instanceof SPacketUpdateHealth && Damage.getValue())
            {
                if (!healthTimer.passed(10000))
                    return;
                
                SPacketUpdateHealth packet = (SPacketUpdateHealth) event.getPacket();
                
                if (packet.getHealth() < mc.player.getHealth())
                {
                    healthTimer.reset();
                    sendNotificationIfNeed("You've just taken damage.");
                }
            }
            else if (event.getPacket() instanceof SPacketChat && (PM.getValue() || Name.getValue()))
            {
                if (!chatTimer.passed(10000))
                    return;
                
                SPacketChat packet = (SPacketChat) event.getPacket();
                
                if (packet.chatComponent.getFormattedText().contains(mc.player.getName()) && Name.getValue())
                {
                    chatTimer.reset();
                    sendNotificationIfNeed("You were mentioned in chat.");
                }
                else if (packet.chatComponent.getFormattedText().contains("whispers") && PM.getValue())//&& packet.chatComponent.getFormattedText().contains(ChatFormatting.LIGHT_PURPLE))
                {
                    chatTimer.reset();
                    sendNotificationIfNeed("You recieved a private message");
                }
            }
            else if (event.getPacket() instanceof SPacketDisconnect && Kick.getValue())
            {
                sendNotificationIfNeed("You've just disconnected");
            }
        }
        catch (Exception e)
        {
            
        }
    });
}
