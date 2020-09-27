package com.salhack.summit.gui.hud.components;

import com.salhack.summit.gui.hud.components.util.CornerItem;
import com.salhack.summit.gui.hud.components.util.CornerList;
import com.salhack.summit.managers.HudManager;
import com.salhack.summit.module.Value;
import com.salhack.summit.module.ValueListeners;
import com.salhack.summit.util.render.RenderUtil;
import net.minecraft.client.gui.ScaledResolution;

public class OptionalListHudComponent extends DraggableHudComponent
{
    protected CornerList currentCornerList = null;
    protected CornerItem cornerItem;
    
    public final Value<String> currentSide = new Value<>("CurrentSide", new String[] { }, "The current side to go on", "BottomRight");
    public final Value<Boolean> RainbowVal = new Value<Boolean>("Rainbow", new String[]
    { "" }, "Makes a dynamic rainbow", true);

    public OptionalListHudComponent(String displayName, float x, float y, float width, float height, String string2)
    {
        super(displayName, x, y, width, height);
        cornerItem = new CornerItem(displayName, "", 0, false);
        addFlag(HudComponentFlags.IsInCornerList);
        setCurrentCornerList(HudManager.Get().GetModList("BottomRight"));
        this.getValueList().add(currentSide);
        this.getValueList().add(RainbowVal);

        currentSide.addString("TopRight");
        currentSide.addString("BottomRight");
        currentSide.addString("BottomLeft");
        currentSide.addString("TopLeft");
        currentSide.addString("None");
        
        currentSide.Listener = new ValueListeners()
        {
            @Override
            public void OnValueChange(Value<?> val)
            {
                onValChange(val);
            }
        };
    }
    
    public OptionalListHudComponent(String displayName, float x, float y, float width, float height)
    {
        this(displayName, x, y, width, height, "BottomRight");
    }
    
    @Override
    public void afterLoad(boolean enabled)
    {
        setEnabled(enabled);
        setCurrentCornerList(HudManager.Get().GetModList(currentSide.getValue()));
    }
    
    public OptionalListHudComponent(String displayName, float x, float y)
    {
        this(displayName, x, y, 0, 0);
    }

    public final CornerList getCurrentCornerList()
    {
        return currentCornerList;
    }
    
    protected void setCurrentCornerList(CornerList list)
    {
        if (currentCornerList != null)
            currentCornerList.removeCornerItem(cornerItem);
        
        currentCornerList = list;
        
        if (!hasFlag(HudComponentFlags.IsInCornerList))
            addFlag(HudComponentFlags.IsInCornerList);
        
        if (list != null)
        {
            if (isEnabled())
                list.addCornerItem(cornerItem);
        }
        else
            removeFlag(HudComponentFlags.IsInCornerList);
    }

    @Override
    public void onRender(ScaledResolution res, float mouseX, float mouseY, float partialTicks)
    {
        super.onRender(res, mouseX, mouseY, partialTicks);
        
        setWidth(cornerItem.getWidth());
        setHeight(cornerItem.getHeight());
        RenderUtil.drawStringWithShadow(cornerItem.getDisplayName(), getX(), getY(), RainbowVal.getValue() ? HudManager.Get().rainbow.GetRainbowColorAt(0) : 0xAAAAAA);
    }
    
    @Override
    public void setEnabled(boolean enabled)
    {
        super.setEnabled(enabled);
        
        if (currentCornerList == null)
            return;
        
        if (enabled)
        {
            if (!currentCornerList.contains(cornerItem))
                currentCornerList.addCornerItem(cornerItem);
        }
        else
            currentCornerList.removeCornerItem(cornerItem);
    }
    
    @Override
    public float getWidth()
    {
        return cornerItem.getWidth();
    }
    
    @Override
    public float getHeight()
    {
        return cornerItem.getHeight();
    }
    
    public void onValChange(Value<?> val)
    {
        setCurrentCornerList(HudManager.Get().GetModList(currentSide.getValue()));
    }
}
