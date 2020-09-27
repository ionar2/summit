package com.salhack.summit.mixin.client;

import com.salhack.summit.events.player.EventPlayerUpdateMoveState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.salhack.summit.SummitMod;
import com.salhack.summit.util.CameraUtils;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.util.MovementInput;
import net.minecraft.util.MovementInputFromOptions;

@Mixin(value = MovementInputFromOptions.class, priority = 10000) ///< wwe has 9999, we should be atleast 1 above
public abstract class MixinMovementInputFromOptions extends MovementInput
{
    @Inject(method = "updatePlayerMoveState", at = @At("RETURN"))
    public void updatePlayerMoveStateReturn(CallbackInfo callback)
    {
        SummitMod.EVENT_BUS.post(new EventPlayerUpdateMoveState());
    }
    @Redirect(method = "updatePlayerMoveState", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/settings/KeyBinding;isKeyDown()Z"))
    public boolean isKeyPressed(KeyBinding keyBinding)
    {
        if (CameraUtils.freecamEnabled())
            return false;
        
        return keyBinding.isKeyDown();
    }
}
