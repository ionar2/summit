package com.salhack.summit.managers;

import com.salhack.summit.main.Summit;
import com.salhack.summit.main.SummitStatic;
import com.salhack.summit.module.combat.*;
import com.salhack.summit.module.exploit.*;
import com.salhack.summit.module.misc.*;
import com.salhack.summit.module.Module;
import com.salhack.summit.module.Module.ModuleType;
import com.salhack.summit.module.Value;
import com.salhack.summit.module.bot.DupeBot;
import com.salhack.summit.module.movement.*;
import com.salhack.summit.module.render.*;
import com.salhack.summit.module.ui.*;
import com.salhack.summit.module.world.*;
import com.salhack.summit.preset.Preset;
import net.minecraft.client.gui.GuiScreen;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import static com.salhack.summit.main.SummitStatic.*;

public class ModuleManager
{
    public static ModuleManager Get()
    {
        return Summit.GetModuleManager();
    }

    public ModuleManager()
    {
    }

    public List<Module> Mods = new CopyOnWriteArrayList<>();
    
    public void Init()
    {
        /// Combat

        Add(new AntiCityBoss());
        Add(AUTOARMOR = new AutoArmor());
        Add(AUTOCITY = new AutoCity());
        Add(AUTOCRYSTAL = new AutoCrystal());
        Add(new AutoLog());
        Add(AURA = new Aura());
        Add(AUTOTOTEM = new AutoTotem());
        Add(AUTOTRAP = new AutoTrap());
        Add(AUTOTRAPFEET = new AutoTrapFeet());
        Add(new BlinkDetect());
        Add(new BowAim());
        Add(new BowSpam());
        Add(HOLEFILLER = new HoleFiller());
        Add(OFFHAND = new Offhand());
        Add(SELFTRAP = new SelfTrap());
        Add(new SelfWeb());
        Add(SURROUND = new Surround());
        Add(new Trigger());
        Add(new Velocity());
        
        /// Exploit
        Add(new AntiHunger());
        Add(new CoordTPExploit());
        Add(ENTITYDESYNC = new EntityDesync());
        Add(LIQUIDINTERACT = new LiquidInteract());
        Add(new NoMiningTrace());
        Add(new NewChunks());
        Add(PACKETCANCELLER = new PacketCanceller());
        Add(PACKETFLY = new PacketFly());
        Add(new PortalGodMode());
        Add(REACH = new Reach());
        Add(new Swing());

        /// Misc
        Add(new Announcer());
        Add(ANTIAFK = new AntiAFK());
        Add(new AntiShulkerPlace());
        Add(new AutoEat());
        Add(new AutoFarmland());
        Add(AUTOMEND = new AutoMendArmor());
        Add(new AutoMount());
        Add(AUTORECONNECT = new AutoReconnect());
        Add(new AutoShear());
        Add(new AutoSign());
        Add(new AutoTame());
        Add(new AutoTend());
        Add(new BuildHeight());
        Add(CHATMODIFICATIONS = new ChatModifications());
        Add(new ChatNotifier());
        Add(CHESTSTEALER = new ChestStealer());
        Add(CHORUSFRUITBYPASS = new ChorusFruitBypass());
        Add(new ChestSwap());
        Add(DISCORDRPC = new DiscordRPC());
        Add(new FakePlayer());
        Add(FRIENDS = new Friends());
        Add(new GlobalLocation());
        Add(new HotbarCache());
        Add(new MiddleClickFriends());
        Add(new MiddleClickPearl());
        Add(new Notifications());
        Add(new PacketLogger());
        Add(STOPWATCH = new StopWatch());
        Add(new TotemPopNotifier());
        Add(new VisualRange());
        Add(new XCarry());
        
        /// Movement
        Add(AUTOWALK = new AutoWalk());
        Add(BOATFLY = new BoatFly());
        Add(BLINK = new Blink());
        Add(ELYTRAFLY = new ElytraFly());
        Add(new EntityControl());
        Add(new FastSwim());
        Add(FLIGHT = new Flight());
        Add(new HighJump());
        Add(new IceSpeed());
        Add(new LevitationControl());
        Add(new NoFall());
        Add(NOROTATE = new NoRotate());
        Add(NOSLOW = new NoSlow());
        Add(new Parkour());
        Add(new Jesus());
        Add(new SafeWalk());
        Add(new Sneak());
        Add(new Sprint());
        Add(new Yaw());
        Add(new WebSpeed());
        
        /// Render
        Add(new AntiFog());
        Add(new BlockHighlight());
        Add(new BreadCrumbs());
        Add(new BreakHighlight());
        Add(new CityESP());
        Add(new ContainerPreview());
        Add(ENTITYESP = new EntityESP());
        Add(new EntitySpeed());
        Add(new FarmESP());
        Add(FREECAM = new Freecam());
        Add(new Fullbright());
        Add(new HoleESP());
        Add(ITEMPHYSICS = new ItemPhysics());
        Add(new Nametags());
        Add(new MapTooltip());
        Add(new NoBob());
        Add(NORENDER = new NoRender());
        Add(new Search());
        Add(new ShulkerPreview());
        Add(new Skeleton());
        Add(new SmallShield());
        Add(STORAGEESP = new StorageESP());
        Add(new Tracers());
        Add(new Trajectories());
        Add(VIEWCLIP = new ViewClip());
        Add(new VoidESP());
        Add(WALLHACK = new Wallhack());
        Add(new Waypoints());
        Add(new WoWTooltips());

        /// UI
        Add(COLORS = new Colors());
        Add(new Commands());
        Add(new Console());
        Add(CLICKGUI = new ClickGui());
        Add(new HudEditor());
        Add(HUD = new Hud());
        Add(KEYBINDS = new Keybinds());
        
        /// World
        Add(new AutoBuilder());
        Add(new AutoNameTag());
        Add(new AutoMine());
        Add(new AutoTool());
        Add(AUTOTUNNEL = new AutoTunnel());
        Add(new AutoWither());
        Add(new Avoid());
        Add(new EnderChestFarmer());
        Add(new FastPlace());
        Add(new Lawnmower());
        Add(NOGLITCHBLOCKS = new NoGlitchBlocks());
        Add(new Nuker());
        Add(new Scaffold());
        Add(new SkyRender());
        Add(new SpeedyGonzales());

        Add(STASHLOGGER = new StashLogger());
        Add(TIMER = new TimerModule());
        Add(new Weather());
        
        Add(SummitStatic.DUPEBOT = new DupeBot());
        
        Mods.sort((p_Mod1, p_Mod2) -> p_Mod1.getDisplayName().compareTo(p_Mod2.getDisplayName()));

        final Preset preset = PresetsManager.Get().getActivePreset();
        
        Mods.forEach(mod ->
        {
            preset.initValuesForMod(mod);
        });
        
        Mods.forEach(mod ->
        {
            mod.init();
        });
    }

    public void Add(Module mod)
    {
        try
        {
            for (Field field : mod.getClass().getDeclaredFields())
            {
                if (Value.class.isAssignableFrom(field.getType()))
                {
                    if (!field.isAccessible())
                    {
                        field.setAccessible(true);
                    }
                    final Value<?> val = (Value<?>) field.get(mod);
                    val.InitalizeMod(mod);
                    mod.getValueList().add(val);
                }
            }
            Mods.add(mod);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
    
    public final List<Module> GetModuleList(ModuleType p_Type)
    {
        List<Module> list = new ArrayList<>();
        for (Module module : Mods)
        {
            if (module.getType().equals(p_Type))
            {
                list.add(module);
            }
        }
        // Organize alphabetically
        list.sort(Comparator.comparing(Module::getDisplayName));

        return list;
    }

    public final List<Module> GetModuleList()
    {
        return Mods;
    }

    public void OnKeyPress(String string)
    {
        if (string == null || string.isEmpty() || string.equalsIgnoreCase("NONE"))
            return;
        
        Mods.forEach(p_Mod ->
        {
            if (p_Mod.IsKeyPressed(string))
            {
                p_Mod.toggle();
            }
        });
    }

    public Module GetModLike(String p_String)
    {
        for (Module l_Mod : Mods)
        {
            if (l_Mod.GetArrayListDisplayName().toLowerCase().startsWith(p_String.toLowerCase()))
                return l_Mod;
        }
        
        return null;
    }

    public boolean IgnoreStrictKeybinds()
    {
        if (GuiScreen.isAltKeyDown() && !KEYBINDS.Alt.getValue())
            return true;
        if (GuiScreen.isCtrlKeyDown() && !KEYBINDS.Ctrl.getValue())
            return true;
        if (GuiScreen.isShiftKeyDown() && !KEYBINDS.Shift.getValue())
            return true;
        
        return false;
    }

    public int GetTotalModsOfCategory(ModuleType type)
    {
        int total = 0;
        
        for (Module mod : Mods)
        {
            if (mod.getType() == type)
                ++total;
        }
        
        return total;
    }
}
