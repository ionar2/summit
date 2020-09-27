package com.salhack.summit.module.misc;

import com.salhack.summit.main.SummitStatic;
import com.salhack.summit.main.Wrapper;
import com.salhack.summit.managers.DiscordManager;
import com.salhack.summit.module.Module;
import com.salhack.summit.module.Value;
import com.salhack.summit.util.entity.PlayerUtil;

public class DiscordRPC extends Module
{
    public final Value<Boolean> Username = new Value<Boolean>("Username", new String[] {"U"}, "Displays your username in the rich presence", true);
    public final Value<Boolean> ServerIP = new Value<Boolean>("ServerIP", new String[] {"S"}, "Displays your current playing server in the rich presence", true);
    public final Value<String> DetailsAddon = new Value<String>("DetailsAddon", new String[] {"D"}, "Displays a custom message after the previous", "Gaming");
    public final Value<Boolean> Speed = new Value<Boolean>("Speed", new String[] {"U"}, "Displays your speed in the rich presence", true);
    public final Value<Boolean> Movement = new Value<Boolean>("Movement", new String[] {"U"}, "Displays if you're flying/onground in the rich presence", true);
    public final Value<Boolean> Crystalling = new Value<Boolean>("Crystalling", new String[] {"U"}, "Displays the current target from autocrystal", true);
    public final Value<Boolean> Health = new Value<Boolean>("Health", new String[] {"U"}, "Displays your Health in the rich presence", true);

    public DiscordRPC()
    {
        super("DiscordRPC", new String[] {"RPC"}, "Shows discord rich presence for this mod", "NONE", -1, ModuleType.MISC);
        setEnabled(true);
    }
    
    @Override
    public void init()
    {
        if (isEnabled())
            DiscordManager.Get().enable();
    }
    
    @Override
    public void onEnable()
    {
        super.onEnable();
        
        DiscordManager.Get().enable();
    }

    @Override
    public void onDisable()
    {
        super.onDisable();
        
        try
        {
            DiscordManager.Get().disable();
        } catch (InterruptedException e)
        {
            e.printStackTrace();
        }
    }
    
    public String generateDetails()
    {
        String result = DetailsAddon.getValue();
        
        if (result == null)
            result = "";

        if(DetailsAddon.getValue() != "")
        {
            if(ServerIP.getValue() && Username.getValue())
            {
                result = Wrapper.GetMC().session.getUsername() + " | " + (Wrapper.GetMC().getCurrentServerData() != null ? Wrapper.GetMC().getCurrentServerData().serverIP : "none") + " | " + result;
            }

            else if(Username.getValue())
            {
                result = Wrapper.GetMC().session.getUsername() + " | " + result;
            }
            else if(ServerIP.getValue())
            {
                result = (Wrapper.GetMC().getCurrentServerData() != null ? Wrapper.GetMC().getCurrentServerData().serverIP : "none") + " | " + result;
            }
        }
        else
            if(Username.getValue() && ServerIP.getValue())
            {
                result = Wrapper.GetMC().session.getUsername() + " | " + (Wrapper.GetMC().getCurrentServerData() != null ? Wrapper.GetMC().getCurrentServerData().serverIP : "none");
            }
            else if(Username.getValue())
            {
                result = Wrapper.GetMC().session.getUsername();
            }
            else if(ServerIP.getValue())
            {
                result = (Wrapper.GetMC().getCurrentServerData() != null ? Wrapper.GetMC().getCurrentServerData().serverIP : "none");
            }
            // Should prevent " | " from being shown unless Result != ""
        
        return result;
    }

    public String generateState()
    {
        if (mc.player == null)
            return "Loading...";
        
        String result = "";
        
        if (Crystalling.getValue() && SummitStatic.AUTOCRYSTAL.isEnabled() && SummitStatic.AUTOCRYSTAL.getTarget() != null)
            return "Crystalling " + SummitStatic.AUTOCRYSTAL.getTarget() + " with Summit!";
            // Removing "Auto Crystal" makes it appear cleaner on the rpc
        
        if (Movement.getValue())
        {
            result = mc.player.onGround ? "On the ground" : "Airborne";
            
            if (mc.player.isElytraFlying())
                result = "Zooming";
        }
        
        if (Speed.getValue())
        {
            float speed = PlayerUtil.getSpeedInKM();
            
            if (result.isEmpty())
                result = "Moving " + speed + " km/h";
            else
            {
                if (result.equals("Zooming"))
                    result += " at " + speed + " km/h";
                else
                    result += " going " + speed + " km/h";
            }
        }
        
        if (Health.getValue())
        {
            if (!result.isEmpty())
                result += " ";
            
            result += Math.floor(mc.player.getHealth() + mc.player.getAbsorptionAmount()) + " hp";
        }
        
        return result;
    }

}
