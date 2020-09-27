package com.salhack.summit.module.render;

import com.salhack.summit.events.MinecraftEvent.Stage;
import com.salhack.summit.events.bus.EventHandler;
import com.salhack.summit.events.bus.Listener;
import com.salhack.summit.events.network.EventServerPacket;
import com.salhack.summit.events.player.EventPlayerUpdate;
import com.salhack.summit.module.Module;
import com.salhack.summit.module.Value;
import net.minecraft.init.MobEffects;
import net.minecraft.network.play.server.SPacketEntityEffect;
import net.minecraft.potion.PotionEffect;
import net.minecraft.world.World;

public final class Fullbright extends Module
{
    public final Value<String> mode = new Value<>("Mode", new String[]
    { "Mode", "M" }, "The brightness mode to use.", "Gamma");

    public final Value<Boolean> effects = new Value<Boolean>("Effects", new String[]
    { "Eff" }, "Blocks blindness & nausea effects if enabled.", true);

    private float lastGamma;

    private World world;

    public Fullbright()
    {
        super("Fullbright", new String[]
        { "FullBright", "Bright", "Brightness" }, "Makes the world brighter", "NONE", 0xF19C66, ModuleType.RENDER);
        setMetaData(getMetaData());
        
        mode.addString("Gamma");
        mode.addString("Potion");
        mode.addString("Table");
    }
    
    public String getMetaData()
    {
        return String.valueOf(mode.getValue());
    }

    @Override
    public void onEnable()
    {
        super.onEnable();
        if (this.mode.getValue().equals("Gamma"))
        {
            this.lastGamma = mc.gameSettings.gammaSetting;
        }
    }

    @Override
    public void onDisable()
    {
        super.onDisable();

        if (this.mode.getValue().equals("Gamma"))
        {
            mc.gameSettings.gammaSetting = this.lastGamma;
        }

        if (this.mode.getValue().equals("Potion") && mc.player != null)
        {
            mc.player.removePotionEffect(MobEffects.NIGHT_VISION);
        }

        if (this.mode.getValue().equals("Table"))
        {
            if (mc.world != null)
            {
                for (int i = 0; i <= 15; ++i)
                {
                    float f1 = 1.0F - (float) i / 15.0F;
                    mc.world.provider.getLightBrightnessTable()[i] = (1.0F - f1) / (f1 * 3.0F + 1.0F) * 1.0F + 0.0F;
                }
            }
        }
    }

    @EventHandler
    private Listener<EventPlayerUpdate> OnPlayerUpdate = new Listener<>(p_Event ->
    {
        setMetaData(getMetaData());

        switch (this.mode.getValue())
        {
            case "Gamma":
                if (mc.gameSettings.gammaSetting <= 100F)
                {
                    mc.gameSettings.gammaSetting += 0.1;
                }
                mc.player.removePotionEffect(MobEffects.NIGHT_VISION);
                break;
            case "Potion":
                mc.gameSettings.gammaSetting = 1.0f;
                mc.player.addPotionEffect(new PotionEffect(MobEffects.NIGHT_VISION, 5210));
                break;
            case "Table":
                if (this.world != mc.world)
                {
                    if (mc.world != null)
                    {
                        for (int i = 0; i <= 15; ++i)
                        {
                            mc.world.provider.getLightBrightnessTable()[i] = 1.0f;
                        }
                    }
                    this.world = mc.world;
                }
                break;
        }
    });

    @EventHandler
    private Listener<EventServerPacket> onServerPacket = new Listener<>(event ->
    {
        if (event.getStage() != Stage.Pre)
            return;
        
        if (event.getPacket() instanceof SPacketEntityEffect)
        {
            if (this.effects.getValue())
            {
                final SPacketEntityEffect packet = (SPacketEntityEffect) event.getPacket();
                if (mc.player != null && packet.getEntityId() == mc.player.getEntityId())
                {
                    if (packet.getEffectId() == 9 || packet.getEffectId() == 15)
                    {
                        event.cancel();
                    }
                }
            }
        }
    });

}
