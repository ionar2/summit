package com.salhack.summit.managers;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import com.salhack.summit.command.Command;
import com.salhack.summit.command.impl.*;
import com.salhack.summit.command.util.ModuleCommandListener;
import com.salhack.summit.main.Summit;

public class CommandManager
{
    public CommandManager()
    {
    }
    
    public void InitializeCommands()
    {
        Commands.add(new FriendCommand());
        Commands.add(new HelpCommand());
        Commands.add(new SoundReloadCommand());
        Commands.add(new HClipCommand());
        Commands.add(new VClipCommand());
        Commands.add(new ToggleCommand());
        Commands.add(new BindCommand());
        Commands.add(new UnbindCommand());
        Commands.add(new ResetGUICommand());
        Commands.add(new PresetsCommand());
        Commands.add(new WaypointCommand());
        Commands.add(new DonkeyChestCommand());
        Commands.add(new MacroCommand());
        
        ModuleManager.Get().GetModuleList().forEach(p_Mod ->
        {
            ModuleCommandListener l_Listener = new ModuleCommandListener()
            {
                @Override
                public void onHide()
                {
                    p_Mod.setHidden(!p_Mod.isHidden());
                }

                @Override
                public void onToggle()
                {
                    p_Mod.toggle();
                }

                @Override
                public void onRename(String newName)
                {
                    p_Mod.setDisplayName(newName);
                }
            };
            
            Commands.add(new ModuleCommand(p_Mod.getDisplayName(), p_Mod.getDesc(), l_Listener, p_Mod.getValueList()));
        });

        HudManager.Get().Items.forEach(p_Item ->
        {
            ModuleCommandListener l_Listener = new ModuleCommandListener()
            {
                @Override
                public void onHide()
                {
                    p_Item.setEnabled(!p_Item.isEnabled());
                }

                @Override
                public void onToggle()
                {
                    p_Item.setEnabled(!p_Item.isEnabled());
                }

                @Override
                public void onRename(String newName)
                {
                    //p_Item.SetDisplayName(p_NewName, true);
                }
            };
            
            Commands.add(new ModuleCommand(p_Item.getDisplayName(), "NYI", l_Listener, p_Item.getValueList()));
        });
        
        /// Sort by alphabet
        Commands.sort(Comparator.comparing(Command::getName));
    }
    
    private ArrayList<Command> Commands = new ArrayList<Command>();
    
    public final ArrayList<Command> GetCommands()
    {
        return Commands;
    }
    
    public final List<Command> GetCommandsLike(String p_Like)
    {
        return Commands.stream()
                .filter(p_Command -> p_Command.getName().toLowerCase().startsWith(p_Like.toLowerCase()))
                .collect(Collectors.toList());
    }
    
    public static CommandManager Get()
    {
        return Summit.GetCommandManager();
    }

    public final Command GetCommandLike(String p_Like)
    {
        for (Command l_Command : Commands)
        {
            if (l_Command.getName().toLowerCase().startsWith(p_Like.toLowerCase()))
                return l_Command;
        }
        
        return null;
    }

    public void Reload()
    {
        Commands.clear();
        InitializeCommands();
    }

    public void processCommand(String substring)
    {
        String[] split = substring.split(" ");
        
        final Command cmd = GetCommandLike(split != null ? split[0] : substring);
        
        if (cmd == null)
        {
            Summit.SendMessage("Invalid Command: " + substring);
            return;
        }
        
        cmd.processCommand(substring, split);
    }
}
