package com.salhack.summit.module.render;

import com.salhack.summit.module.Module;
import com.salhack.summit.module.Value;

public class ItemPhysics extends Module
{
    public final static Value<Float> Scaling = new Value<Float>("Scaling", new String[] {""}, "Scaling", 0.5f, 0f, 10f, 1f);

    public ItemPhysics()
    {
        super("ItemPhysics", new String[] {"IP"}, "Allows you to control physics of item entities", "NONE", -1, ModuleType.RENDER);
    }

}
