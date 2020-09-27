package com.salhack.summit.gui.hud.items;

import com.salhack.summit.gui.hud.components.OptionalListHudComponent;
import com.salhack.summit.managers.HudManager;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.Chunk;

public class BiomeComponent extends OptionalListHudComponent
{
    public BiomeComponent()
    {
        super("Biome", 2, 95, 50, 10);
        setCurrentCornerList(HudManager.Get().GetModList("BottomLeft"));
        setEnabled(true);
    }
    
    @Override
    public void onUpdate()
    {
        final BlockPos pos = mc.player.getPosition();
        final Chunk chunk = mc.world.getChunk(pos);
        final Biome biome = chunk.getBiome(pos, mc.world.getBiomeProvider());

        cornerItem.setName(biome.getBiomeName());
    }
}
