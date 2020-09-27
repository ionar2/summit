package com.salhack.summit.module.movement;

import com.salhack.summit.module.Module;

public final class BoatFly extends Module
{
    public BoatFly()
    {
        super("BoatFly", new String[]
        { "BF" }, "retracted", "NONE", 0xC224DB, ModuleType.MOVEMENT);
    }
}
