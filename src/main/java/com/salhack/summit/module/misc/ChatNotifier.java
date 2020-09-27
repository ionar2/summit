package com.salhack.summit.module.misc;

import com.mojang.realmsclient.gui.ChatFormatting;
import com.salhack.summit.events.bus.EventHandler;
import com.salhack.summit.events.bus.Listener;
import com.salhack.summit.events.salhack.EventSalHackModuleDisable;
import com.salhack.summit.events.salhack.EventSalHackModuleEnable;
import com.salhack.summit.managers.NotificationManager;
import com.salhack.summit.module.Module;

public class ChatNotifier extends Module
{
    public ChatNotifier()
    {
        super("ChatNotifier", new String[]
        { "" }, "Notifiys you in chat and notification system when a mod is enabled/disabled", "NONE", -1,
                ModuleType.MISC);
    }

    @EventHandler
    private Listener<EventSalHackModuleEnable> OnModEnable = new Listener<>(p_Event ->
    {
        String l_Msg = String.format("%s was enabled.",
                ChatFormatting.GREEN + p_Event.Mod.getDisplayName() + ChatFormatting.AQUA);

        SendMessage(l_Msg);
        NotificationManager.Get().AddNotification("ChatNotifier", l_Msg);
    });

    @EventHandler
    private Listener<EventSalHackModuleDisable> OnModDisable = new Listener<>(p_Event ->
    {
        String l_Msg = String.format("%s was disabled.",
                ChatFormatting.RED + p_Event.Mod.getDisplayName() + ChatFormatting.AQUA);

        SendMessage(l_Msg);
        NotificationManager.Get().AddNotification("ChatNotifier", l_Msg);
    });
}
