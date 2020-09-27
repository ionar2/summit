package com.salhack.summit.mixin.client;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.Entity;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(Render.class)
abstract class MixinRenderer<T extends Entity> {

    @Shadow
    protected abstract boolean bindEntityTexture(T entity);
    
    @Shadow
    protected abstract int getTeamColor(T entityIn);
    
    @Shadow
    protected boolean renderOutlines;

    @Shadow
    @Final
    protected RenderManager renderManager;
}