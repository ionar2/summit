package com.salhack.summit.managers;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import com.salhack.summit.main.Summit;
import org.apache.commons.io.FileUtils;

import com.salhack.summit.main.SummitStatic;
import com.salhack.summit.preset.Preset;

public class PresetsManager
{
    private List<Preset> _presets = new CopyOnWriteArrayList<>();
    
    public void LoadPresets()
    {
        try
        {
            File[] directories = new File(DirectoryManager.Get().GetCurrentDirectory() + "/Summit/Presets/").listFiles(File::isDirectory);
            
            for (File file : directories)
            {
                System.out.println("" + file.getName().toString());
                Preset preset = new Preset(file.getName().toString());
                preset.load(file);
                _presets.add(preset);
            }
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        
        Preset defaultPreset = null;
        boolean alreadyEnabled = false;
        
        for (Preset p : _presets)
        {
            if (p.getName().equalsIgnoreCase("default"))
                defaultPreset = p;
            else if (p.isActive())
            {
                alreadyEnabled = true;
                break;
            }
        }
        
        if (!alreadyEnabled && defaultPreset != null)
        {
            defaultPreset.setActive(true);
        }
    }
    
    public void CreatePreset(String presetName)
    {
        try
        {
            new File(DirectoryManager.Get().GetCurrentDirectory() + "/Summit/Presets/" + presetName).mkdirs();
            new File(DirectoryManager.Get().GetCurrentDirectory() + "/Summit/Presets/" + presetName + "/Modules").mkdirs();
            Preset preset = new Preset(presetName);
            _presets.add(preset);
            preset.initNewPreset();
            preset.save();
            SetPresetActive(preset);
            
        } catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public void RemovePreset(String presetName)
    {
        Preset toRemove = null;
        
        for (Preset p : _presets)
        {
            if (p.getName().equalsIgnoreCase(presetName))
            {
                toRemove = p;
                break;
            }
        }
        
        if (toRemove != null)
        {
            try
            {
                FileUtils.deleteDirectory(new File(DirectoryManager.Get().GetCurrentDirectory() + "/Summit/Presets/" + toRemove.getName()));
            }
            catch (IOException e)
            {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            _presets.remove(toRemove);
        }
    }
    
    public Preset getActivePreset()
    {
        for (Preset p : _presets)
        {
            if (p.isActive())
                return p;
        }
        
        // default MUST always be available
        return _presets.get(0);
    }

    public void SetPresetActive(Preset preset)
    {
        for (Preset p : _presets)
        {
            p.setActive(false);
        }
        
        preset.setActive(true);

        ModuleManager.Get().Mods.forEach(mod ->
        {
            preset.initValuesForMod(mod);
        });
    }
    
    public final List<Preset> GetItems()
    {
        return _presets;
    }
    
    public static PresetsManager Get()
    {
        return Summit.GetPresetsManager();
    }
}
