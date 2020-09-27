package com.salhack.summit.managers;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.io.FileUtils;

import com.google.common.io.Files;
import com.salhack.summit.main.Summit;
import com.salhack.summit.util.MinecraftInstance;

public class MacroManager
{
    private HashMap<String, Macro> macros = new HashMap<>();
    
    public void Load()
    {
        try
        {
            File files = new File(DirectoryManager.Get().GetCurrentDirectory() + "/Summit/Macros/");
            for (File file : files.listFiles())
            {
                List<String> lines = FileUtils.readLines(file, "UTF-8");
                Macro mac = new Macro();
                lines.forEach(l -> mac.addText(l));
                macros.put(file.getName().replace(".txt", ""), mac);
            }
        }
        catch (Exception e)
        {
            
        }
    }

    private void save()
    {
        macros.forEach((K, V) ->
        {
            try
            {
                final File mac = new File(DirectoryManager.Get().GetCurrentDirectory() + "/Summit/Macros/" + K + ".txt");
                mac.createNewFile();
                FileWriter writer = new FileWriter(DirectoryManager.Get().GetCurrentDirectory() + "/Summit/Macros/" + K + ".txt");
                
                V.Text.forEach(s ->
                {
                    try
                    {
                        writer.write(s + "\n");
                    }
                    catch (IOException e)
                    {
                        e.printStackTrace();
                    }
                });
                
                writer.close();
            }
            catch (IOException e)
            {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        });
    }

    public void OnKeyPress(String key)
    {
        macros.forEach((K, V) ->
        {
            if (K.equals(key))
            {
                V.process();
            }
        });
    }
    
    public static MacroManager Get()
    {
        return Summit.GetMacroManager();
    }
    
    public class Macro extends MinecraftInstance
    {
        private ArrayList<String> Text = new ArrayList<>();
        
        public Macro()
        {
            
        }
        
        public Macro(String string2)
        {
            Text.add(string2);
        }

        public void process()
        {
            Text.forEach(t -> mc.player.sendChatMessage(t));
        }
        
        public void removeText(String text)
        {
            if (Text.contains(text))
                Text.remove(text);
        }
        
        public void addText(String text)
        {
            Text.add(text);
        }
    }

    public void addMacro(String string, String string2)
    {
        if (macros.containsKey(string))
        {
            final Macro mac = macros.get(string);
            mac.addText(string2);
        }
        else
            macros.put(string, new Macro(string2));
        
        save();
    }

    public void removeMacro(String string)
    {
        if (macros.containsKey(string))
            macros.remove(string);
        
        save();
    }
}
