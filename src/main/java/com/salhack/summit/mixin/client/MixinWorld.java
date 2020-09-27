package com.salhack.summit.mixin.client;

import com.salhack.summit.SummitMod;
import com.salhack.summit.events.entity.EventEntityAdded;
import com.salhack.summit.events.entity.EventEntityRemoved;
import com.salhack.summit.events.render.EventRenderRainStrength;
import com.salhack.summit.events.world.EventGetSkyColor;
import com.salhack.summit.events.world.EventWorldSetBlockState;
import com.salhack.summit.main.SummitStatic;
import com.salhack.summit.util.CrystalUtils2;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;

import java.util.List;

import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.world.EnumSkyBlock;
import net.minecraft.world.IWorldEventListener;
import net.minecraft.world.World;

@Mixin(World.class)
public class MixinWorld
{
    @Shadow
    protected List<IWorldEventListener> eventListeners;
    
    @Inject(method = "getRainStrength", at = @At("HEAD"), cancellable = true)
    public void getRainStrength(float delta, CallbackInfoReturnable<Float> p_Callback)
    {
        EventRenderRainStrength l_Event = new EventRenderRainStrength();
        
        SummitMod.EVENT_BUS.post(l_Event);
        
        if (l_Event.isCancelled())
        {
            p_Callback.cancel();
            p_Callback.setReturnValue(0.0f);
        }
    }

    @Inject(method = "setBlockState", at = @At("HEAD"), cancellable = true)
    public void setBlockState(BlockPos pos, IBlockState newState, int flags, CallbackInfoReturnable<Boolean> callback)
    {
        CrystalUtils2.onSetBlockState(pos, newState, flags);
        
        EventWorldSetBlockState event = new EventWorldSetBlockState(pos, newState, flags);
        SummitMod.EVENT_BUS.post(event);
        if (event.isCancelled())
        {
            callback.cancel();
            callback.setReturnValue(false);
        }
    }

    @Inject(method = "onEntityAdded", at = @At("HEAD"), cancellable = true)
    public void onEntityAdded(Entity p_Entity, CallbackInfo p_Info)
    {
        EventEntityAdded l_Event = new EventEntityAdded(p_Entity);

        SummitMod.EVENT_BUS.post(l_Event);

        if (l_Event.isCancelled())
            p_Info.cancel();
    }

    @Inject(method = "onEntityRemoved", at = @At("HEAD"), cancellable = true)
    public void onEntityRemoved(Entity p_Entity, CallbackInfo p_Info)
    {
        CrystalUtils2.onEntityRemoved(p_Entity);
        EventEntityRemoved l_Event = new EventEntityRemoved(p_Entity);

        SummitMod.EVENT_BUS.post(l_Event);

        if (l_Event.isCancelled())
            p_Info.cancel();
    }

    @Inject(method = "getSkyColor", at = @At("HEAD"), cancellable = true)
    public void getSkyColor(Entity entityIn, float partialTicks, CallbackInfoReturnable<Vec3d> callback)
    {
        EventGetSkyColor event = new EventGetSkyColor();
        SummitMod.EVENT_BUS.post(event);
        if (event.isCancelled())
        {
            callback.cancel();
            callback.setReturnValue(event.getVec3d());
        }
    }

    /**
     * Notifies all listening IWorldEventListeners of an update within the given bounds.
     */
    /*@Overwrite
    public void markBlockRangeForRenderUpdate(int x1, int y1, int z1, int x2, int y2, int z2)
    {
        CrystalUtils2.markBlockRangeForRenderUpdate(x1, y1, z1, x2, y2, z2);
        for (int i = 0; i < this.eventListeners.size(); ++i)
        {
            ((IWorldEventListener)this.eventListeners.get(i)).markBlockRangeForRenderUpdate(x1, y1, z1, x2, y2, z2);
        }
    }*/
    
    /*@Inject(method = "markBlockRangeForRenderUpdate", at = @At("HEAD"), cancellable = true)
    public void markBlockRangeForRenderUpdate(int x1, int y1, int z1, int x2, int y2, int z2, CallbackInfo info)
    {
        CrystalUtils2.markBlockRangeForRenderUpdate(x1, y1, z1, x2, y2, z2);
    }*/

    @Inject(method = "updateEntities", at = @At("RETURN"))
    public void updateEntities(CallbackInfo info)
    {
        CrystalUtils2.onUpdate();
    }
    
    @Inject(method = "checkLightFor", at = @At("HEAD"), cancellable = true)
    public void checkLightFor(EnumSkyBlock lightType, BlockPos pos, CallbackInfoReturnable<Boolean> callback)
    {
        if (SummitStatic.NORENDER != null && SummitStatic.NORENDER.isEnabled() && SummitStatic.NORENDER.Skylight.getValue())
        {
            callback.cancel();
            callback.setReturnValue(true);
        }
    }
}
