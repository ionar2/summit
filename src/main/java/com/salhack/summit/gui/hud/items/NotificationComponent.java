package com.salhack.summit.gui.hud.items;

import java.util.Iterator;

import com.salhack.summit.gui.hud.components.DraggableHudComponent;
import com.salhack.summit.managers.NotificationManager;
import com.salhack.summit.util.render.RenderUtil;
import com.salhack.summit.gui.hud.GuiHudEditor;
import net.minecraft.client.gui.ScaledResolution;

public class NotificationComponent extends DraggableHudComponent
{
    public NotificationComponent()
    {
        super("Notifications", 500, 500, 100, 100);
        setEnabled(true);
    }

    @Override
    public void onRender(ScaledResolution res, float p_MouseX, float p_MouseY, float p_PartialTicks)
    {
        super.onRender(res, p_MouseX, p_MouseY, p_PartialTicks);

        if (mc.currentScreen instanceof GuiHudEditor)
        {
            if (NotificationManager.Get().Notifications.isEmpty())
            {
                final String placeholder = "Notifications";
                setWidth(RenderUtil.getStringWidth(placeholder));
                setHeight(RenderUtil.getStringHeight(placeholder));
                RenderUtil.drawStringWithShadow(placeholder, getX(), getY(), 0xFFFFFF);
                return;
            }
        }
        
        Iterator<NotificationManager.Notification> l_Itr = NotificationManager.Get().Notifications.iterator();
        
        float l_Y = getY();
        float l_MaxWidth = 0f;
        
        while (l_Itr.hasNext())
        {
            NotificationManager.Notification l_Notification = l_Itr.next();
            
            if (l_Notification.IsDecayed())
                NotificationManager.Get().Notifications.remove(l_Notification);
            
            //l_Notification.OnRender();
            
            float l_Width = RenderUtil.getStringWidth(l_Notification.GetDescription()) + 1.5f;
            
            RenderUtil.drawRect(getX()-1.5f, l_Y, getX()+l_Width, l_Y+13, 0x75101010);
            RenderUtil.drawStringWithShadow(l_Notification.GetDescription(), getX(), l_Y+l_Notification.GetY(), 0xFFFFFF);
            
            if (l_Width >= l_MaxWidth)
                l_MaxWidth = l_Width;
            
            l_Y -= 13;
        }
        
        setHeight(10f);
        setWidth(l_MaxWidth);
    }
}
