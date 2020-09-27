package com.salhack.summit.managers;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import com.salhack.summit.SummitMod;
import com.salhack.summit.events.bus.EventListener;
import com.salhack.summit.gui.hud.GuiHudEditor;
import com.salhack.summit.gui.hud.components.HudComponentFlags;
import com.salhack.summit.gui.hud.components.HudComponentItem;
import com.salhack.summit.gui.hud.items.*;
import com.salhack.summit.gui.hud.components.util.CornerList;
import com.salhack.summit.main.Summit;
import com.salhack.summit.main.SummitStatic;
import com.salhack.summit.main.Wrapper;
import com.salhack.summit.module.Value;
import com.salhack.summit.module.ValueListeners;
import com.salhack.summit.util.colors.SalRainbowUtil;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;

public class HudManager implements EventListener
{
    public HudManager()
    {
    }
    
    public void Init()
    {
        cornerLists[0] = new CornerList(0);
        cornerLists[1] = new CornerList(1);
        cornerLists[2] = new CornerList(2);
        cornerLists[3] = new CornerList(3);

        Add(SummitStatic.ARRAYLIST = new ArrayListComponent());
        Add(new ArmorComponent());
        Add(new BiomeComponent());
        Add(new WatermarkComponent());
        Add(new CoordsComponent());
        Add(new InventoryComponent());
        Add(new PingComponent());
        Add(new PlayerFrameComponent());
        Add(new SpeedComponent());
        Add(new ChestCountComponent());
        Add(new TimeComponent());
        Add(new TabGUIComponent());
        Add(new NotificationComponent());
        Add(new TPSComponent());
        Add(new FPSComponent());
        Add(new DirectionComponent());
        Add(new TooltipComponent());
        Add(new PlayerCountComponent());
        Add(new NearestEntityFrameComponent());
        Add(new YawComponent());
        Add(new TotemCountComponent());
        Add(new TrueDurabilityComponent());
        Add(new StopwatchComponent());
        Add(new PvPInfoComponent());
        Add(new LagNotifierComponent());

        /// MUST be last in list
        Add(SummitStatic.SELECTORMENU = new SelectorMenuComponent());
        
        LoadSettings();
        
        SummitMod.EVENT_BUS.subscribe(this);
    }

    private CornerList[] cornerLists = new CornerList[4];
    public List<HudComponentItem> Items = new CopyOnWriteArrayList<>();
    public SalRainbowUtil rainbow = new SalRainbowUtil(9);

    public void Add(HudComponentItem item)
    {
        try
        {
            for (Field field : item.getClass().getDeclaredFields())
            {
                if (Value.class.isAssignableFrom(field.getType()))
                {
                    if (!field.isAccessible())
                    {
                        field.setAccessible(true);
                    }
                    
                    final Value<?> val = (Value<?>) field.get(item);
                    
                    ValueListeners listener = new ValueListeners()
                    {
                        @Override
                        public void OnValueChange(Value<?> val)
                        {
                            ScheduleSave(item);
                        }
                    };
                    
                    val.Listener = listener;
                    item.getValueList().add(val);
                }
            }
            
            Items.add(item);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public void OnRender(float partialTicks)
    {
        GuiScreen l_CurrScreen = Wrapper.GetMC().currentScreen;
        
        if (l_CurrScreen != null)
        {
            if (l_CurrScreen instanceof GuiHudEditor)
            {
                return;
            }
        }
        
        rainbow.OnRender();
        
        GlStateManager.pushMatrix();
        
        final ScaledResolution res = new ScaledResolution(Wrapper.GetMC());

        if (cornerLists != null) {
            for (CornerList list : cornerLists)
                list.render(res, true, true, 20);
        }
        
        Items.forEach(item ->
        {
            if (item.isEnabled() && !item.hasFlag(HudComponentFlags.OnlyVisibleInHudEditor) && !item.hasFlag(HudComponentFlags.IsInCornerList))
            {
                try
                {
                    item.onRender(res, 0f, 0f, partialTicks);
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
            }
        });
        
        GlStateManager.popMatrix();
    }
    
    public static HudManager Get()
    {
        return Summit.GetHudManager();
    }

    private void LoadSettings()
    {
        Items.forEach(item ->
        {
            boolean enabled = false;
            
            File l_Exists = new File("Summit/HUD/" + item.getDisplayName() + ".json");
            if (l_Exists.exists())
            {
                try 
                {
                    System.out.println("Loading " + item.getDisplayName());
                    // create Gson instance
                    Gson gson = new Gson();
        
                    // create a reader
                    Reader reader = Files.newBufferedReader(Paths.get("Summit/HUD/" + item.getDisplayName() + ".json"));
        
                    // convert JSON file to map
                    Map<?, ?> map = gson.fromJson(reader, Map.class);
                    
                    // print map entries
                    for (Map.Entry<?, ?> entry : map.entrySet())
                    {
                        String l_Key = (String)entry.getKey();
                        String l_Value = (String)entry.getValue();
        
                        if (l_Key.equalsIgnoreCase("displayname"))
                        {
                            item.setDisplayName(l_Value);
                            continue;
                        }
                        
                        if (l_Key.equalsIgnoreCase("visible"))
                        {
                            enabled = l_Value.equalsIgnoreCase("true");
                            continue;
                        }
        
                        if (l_Key.equalsIgnoreCase("PositionX"))
                        {
                            item.setX(Float.parseFloat(l_Value));
                            continue;
                        }
        
                        if (l_Key.equalsIgnoreCase("PositionY"))
                        {
                            item.setY(Float.parseFloat(l_Value));
                            continue;
                        }
                        
                        for (Value l_Val : item.getValueList())
                        {
                            if (l_Val.getName().equalsIgnoreCase((String) entry.getKey()))
                            {
                                if (l_Val.getValue() instanceof Number && !(l_Val.getValue() instanceof Enum))
                                {
                                    if (l_Val.getValue() instanceof Integer)
                                        l_Val.SetForcedValue(Integer.parseInt(l_Value));
                                    else if (l_Val.getValue() instanceof Float)
                                        l_Val.SetForcedValue(Float.parseFloat(l_Value));
                                    else if (l_Val.getValue() instanceof Double)
                                        l_Val.SetForcedValue(Double.parseDouble(l_Value));
                                }
                                else if (l_Val.getValue() instanceof Boolean)
                                {
                                    l_Val.SetForcedValue(l_Value.equalsIgnoreCase("true"));
                                }
                                else if (l_Val.getValue() instanceof String)
                                    l_Val.SetForcedValue(l_Value);
                                
                                break;
                            }
                        }
                    }
                    
                    item.afterLoad(enabled);
        
                    // close reader
                    reader.close();
        
                }
                catch (Exception ex)
                {
                    ex.printStackTrace();
                }
            }
        });
    }
    
    public void ScheduleSave(HudComponentItem p_Item)
    {
        try
        {
            System.out.println("Saving " + p_Item.getDisplayName());
            GsonBuilder builder = new GsonBuilder();
            
            Gson gson = builder.setPrettyPrinting().create();

            Writer writer = Files.newBufferedWriter(Paths.get("Summit/HUD/" + p_Item.getDisplayName() + ".json"));
            Map<String, String> map = new HashMap<>();

            map.put("displayname", p_Item.getDisplayName());
            map.put("visible", p_Item.isEnabled() ? "true" : "false");
            map.put("PositionX", String.valueOf(p_Item.getX()));
            map.put("PositionY", String.valueOf(p_Item.getY()));
            
            for (Value<?> l_Val : p_Item.getValueList())
            {
                map.put(l_Val.getName().toString(), l_Val.getValue().toString());
            }
            
            gson.toJson(map, writer);
            writer.close();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    public final CornerList GetModList(String string)
    {
        switch (string)
        {
            case "TopRight":
                return cornerLists[0];
            case "BottomRight":
                return cornerLists[1];
            case "BottomLeft":
                return cornerLists[2];
            case "TopLeft":
                return cornerLists[3];
            default:
                break;
        }
        
        return null;
    }

    public void onHudEditorClosed()
    {
        new Thread(() ->
        {
            Items.forEach(item ->
            {
                ScheduleSave(item);
            });
        }).start();
    }
}
