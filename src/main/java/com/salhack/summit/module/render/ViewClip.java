package com.salhack.summit.module.render;

import com.salhack.summit.module.Module;
import com.salhack.summit.module.Value;

public class ViewClip extends Module
{
    public final Value<Float> Distance = new Value<>("Distance", new String[] {"Length", "Lenght", "Far", "d", "l"}, "How much distance should viewclip give", 3.5f, 0.0f, 10.0f, 1.0f);
    
    public ViewClip()
    {
        super("ViewClip", new String[] {"F5", "CameraClip"}, "Prevents the third person camera from ray-tracing", "NONE", 0xD7935D, ModuleType.RENDER);
    }
}
