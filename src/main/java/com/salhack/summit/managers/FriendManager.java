package com.salhack.summit.managers;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.internal.LinkedTreeMap;
import com.google.gson.reflect.TypeToken;
import com.salhack.summit.friend.Friend;
import com.salhack.summit.main.Summit;
import com.salhack.summit.main.SummitStatic;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Paths;

public class FriendManager
{
    public static FriendManager Get()
    {
        return Summit.GetFriendManager();
    }
    
    public FriendManager()
    {
    }
    
    /// Loads the friends from the JSON
    public void LoadFriends()
    {
        File l_Exists = new File("Summit/FriendList.json");
        if (!l_Exists.exists())
            return;
        
        try 
        {
            // create Gson instance
            Gson gson = new Gson();

            // create a reader
            Reader reader = Files.newBufferedReader(Paths.get("Summit/" + "FriendList" + ".json"));

            // convert JSON file to map
            FriendList = gson.fromJson(reader, new TypeToken<LinkedTreeMap<String, Friend>>(){}.getType());

            // close reader
            reader.close();

        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }
    }
    
    public void SaveFriends()
    {
        GsonBuilder builder = new GsonBuilder();
        
        Gson gson = builder.setPrettyPrinting().create();

        Writer writer;
        try
        {
            writer = Files.newBufferedWriter(Paths.get("Summit/" + "FriendList" + ".json"));
        
            gson.toJson(FriendList, new TypeToken<LinkedTreeMap<String, Friend>>(){}.getType(), writer);
            writer.close();
        }
        catch (IOException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
    
    private LinkedTreeMap<String, Friend> FriendList = new LinkedTreeMap<>();
    
    public String GetFriendName(Entity p_Entity)
    {
        if (!FriendList.containsKey(p_Entity.getName().toLowerCase()))
            return p_Entity.getName();
        
        return FriendList.get(p_Entity.getName().toLowerCase()).GetAlias();
    }
    
    public boolean IsFriend(Entity p_Entity)
    {
        return p_Entity instanceof EntityPlayer && FriendList.containsKey(p_Entity.getName().toLowerCase());
    }

    public boolean AddFriend(String p_Name)
    {
        if (FriendList.containsKey(p_Name))
            return false;
        
        Friend l_Friend = new Friend(p_Name, p_Name, null);
        
        FriendList.put(p_Name, l_Friend);
        SaveFriends();
        return true;
    }

    public boolean RemoveFriend(String p_Name)
    {
        if (!FriendList.containsKey(p_Name))
            return false;

        FriendList.remove(p_Name);
        SaveFriends();
        return true;
    }

    public final LinkedTreeMap<String, Friend> GetFriends()
    {
        return FriendList;
    }

    public boolean IsFriend(String p_Name)
    {
        if (!SummitStatic.FRIENDS.isEnabled())
            return false;
        
        return FriendList.containsKey(p_Name.toLowerCase());
    }

    public Friend GetFriend(Entity e)
    {
        if (!SummitStatic.FRIENDS.isEnabled())
            return null;
        
        if (!FriendList.containsKey(e.getName().toLowerCase()))
            return null;
        
        return FriendList.get(e.getName().toLowerCase());
    }

    public void Load()
    {
        LoadFriends();
    }
}
