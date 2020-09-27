package com.salhack.summit.module;

import java.util.ArrayList;
import java.util.List;

public class Value<T>
{
    private String name;
    private String[] alias;
    private String desc;
    private Module Mod;
    public ValueListeners Listener;

    private T value;

    private T min;
    private T max;
    private T inc;
    
    private List<String> modes = new ArrayList<>();

    public Value(String name, String[] alias, String desc)
    {
        this.name = name;
        this.alias = alias;
        this.desc = desc;
    }

    public Value(String name, String[] alias, String desc, T value)
    {
        this(name, alias, desc);
        this.value = value;
    }

    public Value(String name, String[] alias, String desc, T value, T min, T max, T inc)
    {
        this(name, alias, desc, value);
        this.min = min;
        this.max = max;
        this.inc = inc;
    }

    public Value(String string, T val)
    {
        this(string, new String[] {}, "", val);
    }

    public T getValue()
    {
        return this.value;
    }

    public void setValue(T value)
    {
        if (min != null && max != null)
        {
            final Number val = (Number) value;
            final Number min = (Number) this.min;
            final Number max = (Number) this.max;
            this.value = (T) val;
            // this.value = (T) this.clamp(val, min, max);
        }
        else
        {
            this.value = value;
        }

        if (Mod != null)
            Mod.SignalValueChange(this);
        if (Listener != null)
            Listener.OnValueChange(this);
    }
    
    public T getMin()
    {
        return min;
    }

    public void setMin(T min)
    {
        this.min = min;
    }

    public T getMax()
    {
        return max;
    }

    public void setMax(T max)
    {
        this.max = max;
    }

    public T getInc()
    {
        return inc;
    }

    public void setInc(T inc)
    {
        this.inc = inc;
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
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

    public void SetListener(ValueListeners p_VListener)
    {
        Listener = p_VListener;
    }

    public void InitalizeMod(Module p_Mod)
    {
        Mod = p_Mod;
    }

    public void SetForcedValue(T value)
    {
        if (min != null && max != null)
        {
            final Number val = (Number) value;
            final Number min = (Number) this.min;
            final Number max = (Number) this.max;
            this.value = (T) val;
            // this.value = (T) this.clamp(val, min, max);
        }
        else
        {
            this.value = value;
        }
    }

    public void addString(String string)
    {
        this.modes.add(string);
    }

    public String getNextStringValue(boolean recursive)
    {
        if (this.modes.isEmpty())
            return (String) getValue();
        
        int currIndex = this.modes.indexOf(getValue());
        
        if (currIndex == -1)
            return this.modes.get(0);
        
        if (currIndex == this.modes.size() - 1)
            return recursive ? this.modes.get(currIndex - 1) : this.modes.get(0);

        return recursive ? (currIndex == 0 ? this.modes.get(this.modes.size() - 1) : this.modes.get(currIndex - 1)) : this.modes.get(currIndex + 1);
            
    }

    public void setStringValue(String unknownValue)
    {
        if (this.modes.isEmpty())
            SetForcedValue((T) unknownValue);
        else if (this.modes.contains(unknownValue))
            SetForcedValue((T) unknownValue);
    }
}
