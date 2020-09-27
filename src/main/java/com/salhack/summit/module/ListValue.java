package com.salhack.summit.module;

public class ListValue extends Value<String>
{
    public ListValue(String name, String[] alias, String desc, String[] vals)
    {
        super(name, alias, desc, vals[0]);
        
        for (String v : vals)
            this.addString(v);
    }
    
    public ListValue(String name, String desc, String[] vals)
    {
        this(name, new String[] {}, desc, vals);
    }
}
