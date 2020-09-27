package com.salhack.summit.module.world;

import com.salhack.summit.events.bus.EventHandler;
import com.salhack.summit.events.bus.Listener;
import com.salhack.summit.events.world.EventGetSkyColor;
import com.salhack.summit.module.Module;
import com.salhack.summit.module.Value;
import net.minecraft.util.math.Vec3d;

public class SkyRender extends Module
{
    public final Value<Float> OWRed = new Value<>("OwRed", new String[]{"OwR"}, "Amount of red for the overworld sky.", 0.0f, 0.0f, 1.0f, 0.1f);
    public final Value<Float> OWGreen = new Value<>("OwGreen", new String[]{"OwG"}, "Amount of green for the overworld sky.", 0.0f, 0.0f, 1.0f, 0.1f);
    public final Value<Float> OWBlue = new Value<>("OwBlue", new String[]{"OwB"}, "Amount of blue for the overworld sky.", 0.0f, 0.0f, 1.0f, 0.1f);

    public final Value<Float> NERed = new Value<>("NetherRed", new String[]{"NeR"}, "Amount of red for the Nether sky.", 0.0f, 0.0f, 1.0f, 0.1f);
    public final Value<Float> NEGreen = new Value<>("NetherGreen", new String[]{"NeG"}, "Amount of green for the Nether sky.", 0.0f, 0.0f, 1.0f, 0.1f);
    public final Value<Float> NEBlue = new Value<>("NetherBlue", new String[]{"NeB"}, "Amount of blue for the Nether sky.", 0.0f, 0.0f, 1.0f, 0.1f);

    public final Value<Float> ENDRed = new Value<>("EndRed", new String[]{"EndR"}, "Amount of red for the end sky.", 0.0f, 0.0f, 1.0f, 0.1f);
    public final Value<Float> ENDGreen = new Value<>("EndGreen", new String[]{"EndG"}, "Amount of green for the end sky.", 0.0f, 0.0f, 1.0f, 0.1f);
    public final Value<Float> ENDBlue = new Value<>("EndBlue", new String[]{"EndB"}, "Amount of blue for the end sky.", 0.0f, 0.0f, 1.0f, 0.1f);

    public SkyRender()
    {
        super("SkyRender", new String[]{"SkyColor"}, "Changes color of the Sky.", "NONE", -1, Module.ModuleType.WORLD);
    }

    @EventHandler
    private Listener<EventGetSkyColor> onCollisionBoundingBox = new Listener<>(event ->
    {
        if (mc.world == null) return;

        event.cancel();

        // Nether
        if (mc.player.dimension == -1)
        {
            event.setColor(new Vec3d(NERed.getValue(), NEGreen.getValue(), NEBlue.getValue()));
        }

        // Over-world
        if (mc.player.dimension == 0)
        {
            event.setColor(new Vec3d(OWRed.getValue(), OWGreen.getValue(), OWBlue.getValue()));
        }

        // End
        if (mc.player.dimension == 1)
        {
            event.setColor(new Vec3d(ENDRed.getValue(), ENDGreen.getValue(), ENDBlue.getValue()));
        }
    });
}
