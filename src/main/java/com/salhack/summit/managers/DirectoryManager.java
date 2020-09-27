package com.salhack.summit.managers;

import java.io.File;
import java.io.IOException;

import com.salhack.summit.main.Summit;

public class DirectoryManager
{
    public DirectoryManager()
    {
    }

    public void Init()
    {
        /// Create directories as needed
        try
        {
            CreateDirectory("Summit");
            CreateDirectory("Summit/Modules");
            CreateDirectory("Summit/GUI");
            CreateDirectory("Summit/HUD");
            CreateDirectory("Summit/Locater");
            CreateDirectory("Summit/StashFinder");
            CreateDirectory("Summit/Config");
            CreateDirectory("Summit/Capes");
            CreateDirectory("Summit/Music");
            CreateDirectory("Summit/CoordExploit");
            CreateDirectory("Summit/LogoutSpots");
            CreateDirectory("Summit/DeathSpots");
            CreateDirectory("Summit/Waypoints");
            CreateDirectory("Summit/Fonts");
            CreateDirectory("Summit/CustomMods");
            CreateDirectory("Summit/Presets");
            CreateDirectory("Summit/Presets/Default");
            CreateDirectory("Summit/Presets/Default/Modules");
            CreateDirectory("Summit/PacketLogger");
            CreateDirectory("Summit/Macros");
        }
        catch (IOException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
    
    public void CreateDirectory(String p_Path) throws IOException
    {
        new File(p_Path).mkdirs();
        
        //System.out.println("Created path at " + l_Path.get().toString());
    }
    
    public static DirectoryManager Get()
    {
        return Summit.GetDirectoryManager();
    }

    public String GetCurrentDirectory() throws IOException
    {
        return new java.io.File(".").getCanonicalPath();
    }
}
