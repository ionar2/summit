package com.salhack.summit;

import com.salhack.summit.main.ForgeEventProcessor;
import com.salhack.summit.main.Summit;
import com.mojang.realmsclient.gui.ChatFormatting;
import com.salhack.summit.events.bus.EventBus;
import com.salhack.summit.events.bus.EventManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;

@Mod(modid = "summit", name = "Summit", version = SummitMod.VERSION)
public final class SummitMod
{
    public static final String NAME = "Summit";
    public static final String VERSION = "beta-v2.2";
    public static final String WATERMARK = "Summit " + VERSION;

    public static final Logger log = LogManager.getLogger("sal");

    public static final EventBus EVENT_BUS = new EventManager();

    @Mod.EventHandler
    public void init(FMLInitializationEvent event)
    {
        log.info("init salhack v: " + VERSION);

        Summit.Init();

        MinecraftForge.EVENT_BUS.register(new ForgeEventProcessor());
    }
}
