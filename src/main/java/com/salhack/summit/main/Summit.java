package com.salhack.summit.main;

import com.mojang.realmsclient.gui.ChatFormatting;

import com.salhack.summit.communication.Client;
import com.salhack.summit.util.Timer;
import com.salhack.summit.util.render.RenderUtil;
import com.salhack.summit.waypoints.WaypointManager;
import com.salhack.summit.SummitMod;
import com.salhack.summit.managers.CapeManager;
import com.salhack.summit.managers.CommandManager;
import com.salhack.summit.managers.DirectoryManager;
import com.salhack.summit.managers.DiscordManager;
import com.salhack.summit.managers.FriendManager;
import com.salhack.summit.managers.HudManager;
import com.salhack.summit.managers.ImageManager;
import com.salhack.summit.managers.MacroManager;
import com.salhack.summit.managers.ModuleManager;
import com.salhack.summit.managers.NotificationManager;
import com.salhack.summit.managers.PresetsManager;
import com.salhack.summit.managers.RotationManager;
import com.salhack.summit.managers.TickRateManager;
import com.salhack.summit.managers.UUIDManager;
import net.minecraft.util.text.TextComponentString;

public class Summit
{
    private static ModuleManager m_ModuleManager = new ModuleManager();
    private static ImageManager m_ImageManager = new ImageManager();
    private static HudManager m_HudManager = new HudManager();
    private static FriendManager m_FriendManager = new FriendManager();
    private static DiscordManager m_DiscordManager = new DiscordManager();
    private static DirectoryManager m_DirectoryManager = new DirectoryManager();
    private static CommandManager m_CommandManager = new CommandManager();
    private static TickRateManager m_TickRateManager = new TickRateManager();
    private static NotificationManager m_NotificationManager = new NotificationManager();
    private static WaypointManager m_WaypointManager = new WaypointManager();
    private static CapeManager m_CapeManager = new CapeManager();
    private static AlwaysEnabledModule m_AlwaysEnabledMod;
    private static PresetsManager m_PresetsManager = new PresetsManager();
    private static UUIDManager m_UUIDManager = new UUIDManager();
    private static RotationManager m_RotationManager = new RotationManager();
    private static MacroManager m_MacroManager = new MacroManager();
    private static Client m_Client;
    
    /////////////////////////////////////////////////////
    private static Timer updateTimer = new Timer();

    public static void Init()
    {
        SummitMod.log.info("initalizing salhack object (all static fields)");
        RenderUtil.init(); // init static class
        m_DirectoryManager.Init();

        /// load before mods - must be in gl thread
        m_CapeManager.Init();

        try
        {
            m_PresetsManager.LoadPresets(); // must be before module init
            m_ModuleManager.Init();
            m_HudManager.Init();
            m_CommandManager.InitializeCommands();
            m_ImageManager.Load();
            m_FriendManager.Load();
            m_MacroManager.Load();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        
        new Thread(() ->
        {
            while (true)
            {
                if (!updateTimer.passed(50))
                    continue;
                updateTimer.reset();
                
                try
                {
                    m_PresetsManager.getActivePreset().onUpdate();
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
            }
        }).start();
        
        /// features people can't turn off
        m_AlwaysEnabledMod = new AlwaysEnabledModule();
        m_AlwaysEnabledMod.init();
        m_RotationManager.init();
        
        new Thread(() ->
        {
            m_Client = new Client("127.0.0.1", 5056);

            m_Client.Update();
        }).start();
    }

    public static ModuleManager GetModuleManager()
    {
        return m_ModuleManager;
    }

    public static ImageManager GetImageManager()
    {
        return m_ImageManager;
    }

    /// Writes a message to ingame chat
    /// Player must be ingame for this
    public static void SendMessage(String string)
    {
        if (Wrapper.GetMC().ingameGUI != null || Wrapper.GetPlayer() == null)
            Wrapper.GetMC().ingameGUI.getChatGUI().printChatMessage(new TextComponentString(ChatFormatting.RED + "[Summit]: " + string));
    }

    public static HudManager GetHudManager()
    {
        return m_HudManager;
    }

    public static FriendManager GetFriendManager()
    {
        return m_FriendManager;
    }

    public static DiscordManager GetDiscordManager()
    {
        return m_DiscordManager;
    }

    public static DirectoryManager GetDirectoryManager()
    {
        return m_DirectoryManager;
    }

    public static CommandManager GetCommandManager()
    {
        return m_CommandManager;
    }
    
    public static TickRateManager GetTickRateManager()
    {
        return m_TickRateManager;
    }

    public static NotificationManager GetNotificationManager()
    {
        return m_NotificationManager;
    }

    public static WaypointManager GetWaypointManager()
    {
        return m_WaypointManager;
    }

    public static CapeManager GetCapeManager()
    {
        return m_CapeManager;
    }
    
    public static PresetsManager GetPresetsManager()
    {
        return m_PresetsManager;
    }

    public static UUIDManager GetUUIDManager()
    {
        return m_UUIDManager;
    }
    
    public static Client GetClient()
    {
        return m_Client;
    }

    public static RotationManager GetRotationManager()
    {
        return m_RotationManager;
    }

    public static void SendMessage(TextComponentString portalTextComponent)
    {
        Wrapper.GetMC().ingameGUI.getChatGUI().printChatMessage(portalTextComponent);
    }

    public static MacroManager GetMacroManager()
    {
        return m_MacroManager;
    }
}
