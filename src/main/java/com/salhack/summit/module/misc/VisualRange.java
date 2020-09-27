package com.salhack.summit.module.misc;

import com.salhack.summit.events.bus.EventHandler;
import com.salhack.summit.events.bus.Listener;
import com.salhack.summit.events.entity.EventEntityAdded;
import com.salhack.summit.events.entity.EventEntityRemoved;
import com.salhack.summit.events.player.EventPlayerUpdate;
import com.salhack.summit.managers.FriendManager;
import com.salhack.summit.managers.NotificationManager;
import com.salhack.summit.module.Module;
import com.salhack.summit.module.Value;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;

import java.util.ArrayList;
import java.util.List;

public class VisualRange extends Module
{
    public final Value<String> Mode = new Value<>("Mode", new String[] {"M"}, "Mode of notifying to use", "Both");
    public final Value<Boolean> Friends = new Value<Boolean>("Friends", new String[]
            { "Friend" }, "Notifies if a friend comes in range", true);
    public final Value<Boolean> Enter = new Value<Boolean>("Enter", new String[]
            { "Enters" }, "Notifies when the entity enters range", true);
    public final Value<Boolean> Leave = new Value<Boolean>("Leave", new String[]
            { "Leaves" }, "Notifies when the entity leaves range", true);

    public VisualRange()
    {
        super("VisualRange", new String[]
                { "VR" }, "Notifies you when one enters or leaves your visual range.", "NONE", -1, Module.ModuleType.MISC);
        setMetaData(getMetaData());
        
        Mode.addString("Chat");
        Mode.addString("Notification");
        Mode.addString("Both");
    }

    private List<String> Entities = new ArrayList<String>();

    public String getMetaData()
    {
        return String.valueOf(Mode.getValue());
    }

    @Override
    public void onEnable()
    {
        super.onEnable();

        Entities.clear();
    }

    @EventHandler
    private Listener<EventPlayerUpdate> onPlayerUpdate  = new Listener<>(event ->
    {
        setMetaData(getMetaData());
    });
    
    @EventHandler
    private Listener<EventEntityAdded> OnEntityAdded = new Listener<>(p_Event ->
    {
        if (!Enter.getValue())
            return;

        if (!VerifyEntity(p_Event.GetEntity()))
            return;

        if (!Entities.contains(p_Event.GetEntity().getName()))
        {
            Entities.add(p_Event.GetEntity().getName());
            Notify(String.format("%s has entered your visual range.", p_Event.GetEntity().getName()));
        }
    });

    @EventHandler
    private Listener<EventEntityRemoved> OnEntityRemove = new Listener<>(p_Event ->
    {
        if (!Leave.getValue())
            return;

        if (!VerifyEntity(p_Event.GetEntity()))
            return;

        if (Entities.contains(p_Event.GetEntity().getName()))
        {
            Entities.remove(p_Event.GetEntity().getName());
            Notify(String.format("%s has left your visual range.", p_Event.GetEntity().getName()));
        }
    });

    private boolean VerifyEntity(Entity p_Entity)
    {
        if (!(p_Entity instanceof EntityPlayer))
            return false;

        if (p_Entity == mc.player)
            return false;

        if (!Friends.getValue() && FriendManager.Get().IsFriend(p_Entity))
            return false;

        return true;
    }

    private void Notify(String p_Msg)
    {
        switch (Mode.getValue())
        {
            case "Chat":
                SendMessage(p_Msg);
                break;
            case "Notification":
                NotificationManager.Get().AddNotification("VisualRange", p_Msg);
                break;
            case "Both":
                SendMessage(p_Msg);
                NotificationManager.Get().AddNotification("VisualRange", p_Msg);
                break;
        }
    }
}
