package com.salhack.summit.module;

import com.mojang.realmsclient.gui.ChatFormatting;
import com.salhack.summit.SummitMod;
import com.salhack.summit.main.SummitStatic;
import com.salhack.summit.managers.CommandManager;
import com.salhack.summit.managers.ModuleManager;
import com.salhack.summit.events.bus.EventListener;
import com.salhack.summit.events.salhack.EventSalHackModuleDisable;
import com.salhack.summit.events.salhack.EventSalHackModuleEnable;
import com.salhack.summit.main.Summit;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import org.lwjgl.input.Keyboard;

import java.util.ArrayList;
import java.util.List;

public class Module implements EventListener
{
    public String displayName;
    private String[] alias;
    private String desc;
    public String key;
    private int color;
    public boolean hidden = false;
    private boolean enabled = false;
    private ModuleType type;
    private boolean m_NeedsClickGuiValueUpdate;
    protected final Minecraft mc = Minecraft.getMinecraft();
    private String _metaData = null;

    public List<Value<?>> valueList = new ArrayList<Value<?>>();
    
    private Module(String displayName, String[] alias, String key, int color, ModuleType type)
    {
        this.displayName = displayName;
        this.alias = alias;
        this.key = key;
        this.color = color;
        this.type = type;
    }

    public Module(String displayName, String[] alias, String desc, String key, int color, ModuleType type)
    {
        this(displayName, alias, key, color, type);
        this.desc = desc;
    }

    public void onEnable()
    {
        /// allow events to be called
        SummitMod.EVENT_BUS.subscribe(this);
        SummitMod.EVENT_BUS.post(new EventSalHackModuleEnable(this));
        
        if (SummitStatic.ARRAYLIST != null && !isHidden())
            SummitStatic.ARRAYLIST.addMod(this);
    }

    public void onDisable()
    {
        /// disallow events to be called
        SummitMod.EVENT_BUS.unsubscribe(this);
        SummitMod.EVENT_BUS.post(new EventSalHackModuleDisable(this));

        if (SummitStatic.ARRAYLIST != null && !isHidden())
            SummitStatic.ARRAYLIST.removeMod(this);
    }

    public void onToggle()
    {

    }

    public void toggle()
    {
        this.setEnabled(!this.isEnabled());
        if (this.isEnabled())
        {
            this.onEnable();
        }
        else
        {
            this.onDisable();
        }
        this.onToggle();
    }

    public void toggleNoSave()
    {
        this.setEnabled(!this.isEnabled());
        if (this.isEnabled())
        {
            this.onEnable();
        }
        else
        {
            this.onDisable();
        }
        this.onToggle();
    }

    public void ToggleOnlySuper()
    {
        this.setEnabled(!this.isEnabled());
        this.onToggle();
    }

    public String getArrayListAddon()
    {
        return _metaData;
    }

    public void unload()
    {
        this.valueList.clear();
    }

    public enum ModuleType
    {
        COMBAT, EXPLOIT, MOVEMENT, RENDER, WORLD, MISC, HIDDEN, UI, BOT
    }

    public String getDisplayName()
    {
        return displayName;
    }

    public void setDisplayName(String displayName)
    {
        this.displayName = displayName;
        CommandManager.Get().Reload();
    }

    public String[] getAlias()
    {
        return alias;
    }

    public void setAlias(String[] alias)
    {
        this.alias = alias;
    }

    public String getDesc()
    {
        return desc;
    }

    public void setDesc(String desc)
    {
        this.desc = desc;
    }

    public String getKey()
    {
        return key;
    }

    public boolean IsKeyPressed(String p_KeyCode)
    {
        if (GuiScreen.isAltKeyDown() || GuiScreen.isCtrlKeyDown() || GuiScreen.isShiftKeyDown())
        {
            if (key.contains(" + "))
            {
                if (GuiScreen.isAltKeyDown() && key.contains("MENU"))
                {
                    String l_Result = key.replace(Keyboard.isKeyDown(56) ? "LMENU + " : "RMENU + ", "");
                    return l_Result.equals(p_KeyCode);
                }
                else if (GuiScreen.isCtrlKeyDown() && key.contains("CONTROL"))
                {
                    String l_CtrlKey = "";

                    if (Minecraft.IS_RUNNING_ON_MAC)
                        l_CtrlKey = Keyboard.isKeyDown(219) ? "LCONTROL" : "RCONTROL";
                    else
                        l_CtrlKey = Keyboard.isKeyDown(29) ? "LCONTROL" : "RCONTROL";

                    String l_Result = key.replace(l_CtrlKey + " + ", "");
                    return l_Result.equals(p_KeyCode);
                }
                else if (GuiScreen.isShiftKeyDown() && key.contains("SHIFT"))
                {
                    String l_Result = key.replace((Keyboard.isKeyDown(42) ? "LSHIFT" : "RSHIFT") + " + ", "");
                    return l_Result.equals(p_KeyCode);
                }
            }

            if (!ModuleManager.Get().IgnoreStrictKeybinds())
            {
                if (p_KeyCode.contains("SHIFT") || p_KeyCode.contains("CONTROL") || p_KeyCode.contains("MENU"))
                    return key.equals(p_KeyCode);

                return false;
            }
        }

        return key.equals(p_KeyCode);
    }

    public void setKey(String key)
    {
        this.key = key;
    }

    public int getColor()
    {
        return color;
    }

    public void setColor(int color)
    {
        this.color = color;
    }

    public boolean isHidden()
    {
        return hidden;
    }

    public void setHidden(boolean hidden)
    {
        this.hidden = hidden;
        
        if (!isEnabled())
            return;
        
        if (hidden)
        {
            if (SummitStatic.ARRAYLIST != null)
                SummitStatic.ARRAYLIST.removeMod(this);
        }
        else
        {
            if (SummitStatic.ARRAYLIST != null)
                SummitStatic.ARRAYLIST.addMod(this);
        }
    }

    public boolean isEnabled()
    {
        return enabled;
    }

    public void setEnabled(boolean enabled)
    {
        this.enabled = enabled;
    }

    public ModuleType getType()
    {
        return type;
    }

    public void setType(ModuleType type)
    {
        this.type = type;
    }

    public List<Value<?>> getValueList()
    {
        return valueList;
    }
    
    public void SignalEnumChange()
    {
    }

    public void SignalValueChange(Value<?> p_Val)
    {
    }

    public List<Value<?>> GetVisibleValueList()
    {
        return valueList;
    }

    /// functions for updating value in an async way :)
    public void SetClickGuiValueUpdate(boolean p_Val)
    {
        m_NeedsClickGuiValueUpdate = p_Val;
    }

    public boolean NeedsClickGuiValueUpdate()
    {
        return m_NeedsClickGuiValueUpdate;
    }

    public String GetNextStringValue(final Value<String> p_Val, boolean p_Recursive)
    {
        return null;
    }

    public String GetArrayListDisplayName()
    {
        return getDisplayName();
    }

    public String GetFullArrayListDisplayName()
    {
        return getDisplayName() + (getArrayListAddon() != null ? " " + ChatFormatting.GRAY + getArrayListAddon() : "");
    }

    protected void SendMessage(String p_Message)
    {
        if (mc.player != null)
            Summit.SendMessage(ChatFormatting.AQUA + "[" + GetArrayListDisplayName() + "]: " + ChatFormatting.RESET + p_Message);
        else
            System.out.println("[" + GetArrayListDisplayName() + "]: " + p_Message);
    }

    public void init()
    {
    }
    
    public void setMetaData(String meta)
    {
        _metaData = meta;
        
        if (SummitStatic.ARRAYLIST != null && !isHidden() && SummitStatic.ARRAYLIST.getCurrentCornerList() != null)
            SummitStatic.ARRAYLIST.getCurrentCornerList().setModMetaData(getDisplayName(), getMetaData());
    }

    public String getMetaData()
    {
        return _metaData;
    }
}
