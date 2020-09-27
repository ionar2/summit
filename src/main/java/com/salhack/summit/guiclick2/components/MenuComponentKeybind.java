package com.salhack.summit.guiclick2.components;

import com.salhack.summit.main.Summit;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import com.salhack.summit.module.Module;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;

public class MenuComponentKeybind extends MenuComponentEditorItem
{
    public boolean Listening = false;
    final Module Mod;
    private String LastKey = "";

    public MenuComponentKeybind(final Module mod, float x, float y, float width, float height)
    {
        super("Keybind " + mod.getKey(), x, y, width, height, true);
        this.Mod = mod;
    }
    
    @Override
    public String getDisplayName()
    {
        if (Listening)
            return "Press a Key...";

        return "Keybind " + Mod.getKey();
    }

    @Override
    public void clicked(int p_MouseButton)
    {
        super.clicked(p_MouseButton);
        
        if (hovered)
        {
            LastKey = "";
            
            if (p_MouseButton == 0)
                Listening = !Listening;
            else if (p_MouseButton == 1)
                Listening = false;
            else if (p_MouseButton == 2)
            {
                Mod.setKey("NONE");
                Summit.SendMessage("Unbinded the module: " + Mod.getDisplayName());
                Listening = false;
            }
        }
    }
    
    @Override
    public void keyTyped(char typedChar, int keyCode)
    {
        if (Listening)
        {
            String l_Key = String.valueOf(Keyboard.getKeyName(keyCode)).toUpperCase();

            if (l_Key.length() < 1)
            {
                Listening = false;
                return;
            }
            
            if (l_Key.equals("END") || l_Key.equals("BACK") || l_Key.equals("DELETE"))
            {
                l_Key = "NONE";
            }
            
            if (!l_Key.equals("NONE") && !l_Key.contains("CONTROL") && !l_Key.contains("SHIFT") && !l_Key.contains("MENU"))
            {
                if (GuiScreen.isAltKeyDown())
                    l_Key = (Keyboard.isKeyDown(56) ? "LMENU" : "RMENU") + " + " + l_Key;
                else if (GuiScreen.isCtrlKeyDown())
                {
                    String l_CtrlKey = "";
                    
                    if (Minecraft.IS_RUNNING_ON_MAC)
                        l_CtrlKey = Keyboard.isKeyDown(219) ? "LCONTROL" : "RCONTROL";
                    else
                        l_CtrlKey = Keyboard.isKeyDown(29) ? "LCONTROL" : "RCONTROL";
                    
                    l_Key = l_CtrlKey + " + " + l_Key;
                }
                else if (GuiScreen.isShiftKeyDown())
                    l_Key = (Keyboard.isKeyDown(42) ? "LSHIFT" : "RSHIFT") + " + " + l_Key;
            }
            
            LastKey = l_Key;
        }
    }
    
    @Override
    public void onMouseInput()
    {
        if (Listening)
        {
            String mouse = Mouse.getButtonName(Mouse.getEventButton());
            
            if (mouse.equalsIgnoreCase("BUTTON0") || mouse.equalsIgnoreCase("BUTTON1") || mouse.equalsIgnoreCase("BUTTON2"))
                return;
            
            Mod.setKey(mouse);
            Summit.SendMessage("Set the key of " + Mod.getDisplayName() + " to " + mouse);
            Listening = false;
        }
    }

    @Override
    public void renderWith(float x, float y, float mouseX, float mouseY, float partialTicks)
    {
        super.renderWith(x, y, mouseX, mouseY, partialTicks);
        
        if (!Keyboard.getEventKeyState() && Listening && LastKey != "")
        {
            Mod.setKey(LastKey);
            Summit.SendMessage("Set the key of " + Mod.getDisplayName() + " to " + LastKey);
            Listening = false;
        }
    }
}
