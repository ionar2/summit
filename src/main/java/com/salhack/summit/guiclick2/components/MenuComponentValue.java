package com.salhack.summit.guiclick2.components;

import java.math.BigDecimal;

import com.salhack.summit.module.Value;

public class MenuComponentValue extends MenuComponentEditorItem
{
    private Value value;
    private boolean canSlide;
    private boolean isSliding;

    public MenuComponentValue(Value<?> val, float x, float y, float width, float height)
    {
        super(val.getName(), x, y, width, height, needRectDisplay(val));
        this.value = val;
        this.outlineRectInstead = val.getValue() instanceof Number;
        this.sliderWidth = calculateSliderWidth();
        this.toggled = isToggled();
        
        if (val.getValue() instanceof Boolean)
        {
            this.displayRect = !this.toggled;
        }
        this.canSlide = val.getValue() instanceof Number;
    }
    
    private float calculateSliderWidth()
    {
        if (!(value.getValue() instanceof Number))
            return 0;

        float minX = getX();
        float maxX = getX() + getWidth();

        if (value.getMax() == null)
            return minX;

        Number numberVal = (Number) value.getValue();
        Number max = (Number) value.getMax();

        return Math.min(getWidth(), (maxX - minX) * (numberVal.floatValue() / max.floatValue()));
    }
    
    private boolean isToggled()
    {
        if (value.getValue() instanceof Boolean)
            return (Boolean)value.getValue();
        
        return true;
    }
    
    private static boolean needRectDisplay(final Value<?> val)
    {
        return true;//!(val.getValue() instanceof Boolean);
    }
    
    @Override
    public String getDisplayName()
    {
        if (value.getValue() instanceof Number || value.getValue() instanceof Enum || value.getValue() instanceof String)
            return new StringBuilder(super.getDisplayName()).append(" ").append(value.getValue()).toString();
        
        return super.getDisplayName();
    }

    @Override
    public void renderWith(float x, float y, float mouseX, float mouseY, float partialTicks)
    {
        super.renderWith(x, y, mouseX, mouseY, partialTicks);
        
        if (isSliding)
            handleSlide(x, y, mouseX, mouseY);
    }
    
    @Override
    public void clicked(int mouseButton)
    {
        super.clicked(mouseButton);
        
        if (hovered)
        {
            if (canSlide && mouseButton == 0)
                isSliding = true;
            if (value.getValue() instanceof String)
                value.setValue(value.getNextStringValue(mouseButton == 1));
        }
    }
    
    @Override
    public void toggle()
    {
        super.toggle();

        if (value.getValue() instanceof Boolean)
        {
            value.setValue(!(Boolean)value.getValue());
            this.displayRect = !this.displayRect;
        }
    }
    
    public void handleSlide(float currX, float currY, float mouseX, float mouseY)
    {
        float x = currX + getX();

        if (mouseX >= x && mouseX <= currX + getX() + getWidth())
            x = mouseX;

        if (mouseX > currX + getX() + getWidth())
            x = currX + getX() + getWidth();

        x -= currX;

        this.sliderWidth = x - getX();

        float l_Pct = (x - getX()) / getWidth();

        if (value.getValue().getClass() == Float.class)
        {
            BigDecimal l_Decimal = new BigDecimal(
                    (this.value.getMax().getClass() == Float.class ? (Float) this.value.getMax() : this.value.getMax().getClass() == Double.class ? (Double) this.value.getMax() : (Integer) value.getMax())
                            * l_Pct);
            
            this.value.setValue(l_Decimal.setScale(2, BigDecimal.ROUND_HALF_EVEN).floatValue());
        }
        else if (value.getValue().getClass() == Double.class)
        {
            BigDecimal l_Decimal = new BigDecimal(
                    (this.value.getMax().getClass() == Double.class ? (Double) this.value.getMax() : this.value.getMax().getClass() == Float.class ? (Float) this.value.getMax() : (Integer) value.getMax())
                            * l_Pct);

            this.value.setValue(l_Decimal.setScale(2, BigDecimal.ROUND_HALF_EVEN).doubleValue());
        }
        else if (value.getValue().getClass() == Integer.class)
            this.value.setValue((int) ((int) this.value.getMax() * l_Pct));
    }
    
    @Override
    public void onReleased(int mouseX, int mouseY, int state)
    {
        isSliding = false;
    }
}
