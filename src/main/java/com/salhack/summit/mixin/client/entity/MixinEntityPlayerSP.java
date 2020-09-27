package com.salhack.summit.mixin.client.entity;

import com.salhack.summit.SummitMod;
import com.salhack.summit.events.MinecraftEvent;
import com.salhack.summit.events.MinecraftEvent.Stage;
import com.salhack.summit.events.player.*;
import com.salhack.summit.main.SummitStatic;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.entity.Entity;
import net.minecraft.entity.MoverType;
import net.minecraft.network.play.client.CPacketEntityAction;
import net.minecraft.network.play.client.CPacketInput;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.network.play.client.CPacketVehicleMove;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.GameType;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import com.salhack.summit.main.Wrapper;
import com.salhack.summit.managers.CommandManager;
import com.salhack.summit.mixin.client.MixinAbstractClientPlayer;
import com.salhack.summit.module.movement.ElytraFly;
import com.salhack.summit.module.ui.Commands;
import com.salhack.summit.util.CameraUtils;
import com.salhack.summit.util.entity.PlayerUtil;

@Mixin(EntityPlayerSP.class)
public abstract class MixinEntityPlayerSP extends MixinAbstractClientPlayer
{
    @Shadow
    protected abstract boolean isCurrentViewEntity();

    @Shadow
    public net.minecraft.util.MovementInput movementInput;
    
    @Redirect(method = "onLivingUpdate", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/entity/EntityPlayerSP;closeScreen()V"))
    public void closeScreen(EntityPlayerSP entityPlayerSP)
    {
    }

    @Redirect(method = "onLivingUpdate", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Minecraft;displayGuiScreen(Lnet/minecraft/client/gui/GuiScreen;)V"))
    public void closeScreen(Minecraft minecraft, GuiScreen screen)
    {
    }

    @Inject(method = "onLivingUpdate", at = @At("HEAD"))
    public void onLivingUpdate(CallbackInfo info)
    {
        SummitMod.EVENT_BUS.post(new EventPlayerLivingUpdate());
    }
    
    @Inject(method = "move", at = @At("HEAD"), cancellable = true)
    public void move(MoverType type, double x, double y, double z, CallbackInfo p_Info)
    {
        if ((EntityPlayerSP)(Object) this  == Wrapper.GetMC().player)
        {
            EventPlayerMove event = new EventPlayerMove(Stage.Pre, type, x, y, z);
            SummitMod.EVENT_BUS.post(event);
            if (event.isCancelled())
            {
                super.move(type, event.X, event.Y, event.Z);
                p_Info.cancel();
            }
        }
    }

    @Inject(method = "move", at = @At("RETURN"))
    public void movePost(MoverType type, double x, double y, double z, CallbackInfo p_Info)
    {
        if ((EntityPlayerSP)(Object) this  == Wrapper.GetMC().player)
        {
            SummitMod.EVENT_BUS.post(new EventPlayerMove(Stage.Post, type, x, y, z));
        }
    }
    
    private EventPlayerMotionUpdate _event;
    
    @Inject(method = "onUpdateWalkingPlayer", at = @At("HEAD"), cancellable = true)
    public void OnPreUpdateWalkingPlayer(CallbackInfo info)
    {
        _event = new EventPlayerMotionUpdate(MinecraftEvent.Stage.Pre, posX, getEntityBoundingBox().minY, posZ, onGround);
        SummitMod.EVENT_BUS.post(_event);
        
        if (_event.isCancelled())
        {
            SummitMod.EVENT_BUS.post(new EventPlayerMotionUpdateCancelled(MinecraftEvent.Stage.Pre, _event.getPitch(), _event.getYaw()));
            
            info.cancel();
            PlayerUtil.sendMovementPackets(_event);
            postWalkingUpdate();
        }
        
        if (_event.isForceCancelled())
            info.cancel();
    }
    
    private void postWalkingUpdate()
    {
        if (_event.getFunc() != null)
            _event.getFunc().accept((EntityPlayerSP)(Object) this);
        
        _event.setEra(MinecraftEvent.Stage.Post);
        
        SummitMod.EVENT_BUS.post(_event);
        if (_event.isCancelled())
            SummitMod.EVENT_BUS.post(new EventPlayerMotionUpdateCancelled(MinecraftEvent.Stage.Pre, _event.getPitch(), _event.getYaw()));
    }

    @Inject(method = "onUpdateWalkingPlayer", at = @At("RETURN"))
    public void OnPostUpdateWalkingPlayer(CallbackInfo info)
    {
        postWalkingUpdate();
    }

    @Inject(method = "onUpdate", at = @At("HEAD"))
    public void onUpdate(CallbackInfo info)
    {
        SummitMod.EVENT_BUS.post(new EventPlayerUpdate());
    }
    
    @Inject(method = "isCurrentViewEntity", at = @At("HEAD"), cancellable = true)
    private void allowPlayerMovementInFreeCameraMode(CallbackInfoReturnable<Boolean> cir)
    {
        if (CameraUtils.freecamEnabled())
        {
            cir.setReturnValue(true);
        }
    }

    // hack
    private boolean isSpec()
    {
        NetworkPlayerInfo networkplayerinfo = Minecraft.getMinecraft().getConnection().getPlayerInfo(Wrapper.GetPlayer().getGameProfile().getId());
        return networkplayerinfo != null && networkplayerinfo.getGameType() == GameType.SPECTATOR;
    }
    
    @Override
    public boolean isSpectator()
    {
        return isSpec() || CameraUtils.getFreeCameraSpectator();
    }
    
    @Inject(method = "swingArm", at = @At("HEAD"), cancellable = true)
    public void swingArm(EnumHand p_Hand, CallbackInfo p_Info)
    {
        EventPlayerSwingArm l_Event = new EventPlayerSwingArm(p_Hand);
        SummitMod.EVENT_BUS.post(l_Event);
        if (l_Event.isCancelled())
            p_Info.cancel();
    }

    @Inject(method = "pushOutOfBlocks(DDD)Z", at = @At("HEAD"), cancellable = true)
    public void pushOutOfBlocks(double x, double y, double z, CallbackInfoReturnable<Boolean> callbackInfo)
    {
        EventPlayerPushOutOfBlocks l_Event = new EventPlayerPushOutOfBlocks(x, y, z);
        SummitMod.EVENT_BUS.post(l_Event);
        if (l_Event.isCancelled())
            callbackInfo.setReturnValue(false);
    }

    @Inject(method = "sendChatMessage", at = @At("HEAD"), cancellable = true)
    public void swingArm(String p_Message, CallbackInfo p_Info)
    {
        EventPlayerSendChatMessage l_Event = new EventPlayerSendChatMessage(p_Message);
        SummitMod.EVENT_BUS.post(l_Event);
        if (l_Event.isCancelled())
            p_Info.cancel();
    }

    @Override
    public void jump()
    {
        try
        {
            EventPlayerJump event = new EventPlayerJump(motionX, motionZ);

            SummitMod.EVENT_BUS.post(event);
            
            if (!event.isCancelled())
                super.jump();
        }
        catch (Exception v3)
        {
            v3.printStackTrace();
        }
    }

    /*@Inject(method = "displayGUIChest", at = @At("HEAD"), cancellable = true)
    public void displayGUIChest(IInventory inventory, CallbackInfo ci)
    {
        /// @todo move to events
        String id = inventory instanceof IInteractionObject ? ((IInteractionObject) inventory).getGuiID() : "minecraft:container";
        if (id.equals("minecraft:chest"))
        {
            Wrapper.GetMC().displayGuiScreen(new SalGuiChest(Wrapper.GetPlayer().inventory, inventory));
            ci.cancel();
        }
    }*/
    
    @Inject(method = "startRiding", at = @At("HEAD"), cancellable = true)
    public void startRiding(Entity e, boolean force, CallbackInfoReturnable<Boolean> info)
    {
        SummitMod.EVENT_BUS.post(new EventPlayerStartRiding());
    }
    
    @Inject(method = "isHandActive", at = @At("HEAD"), cancellable = true)
    public void isHandActive(CallbackInfoReturnable<Boolean> info)
    {
        EventPlayerIsHandActive event = new EventPlayerIsHandActive();
        SummitMod.EVENT_BUS.post(event);
        
        if (event.isCancelled())
        {
            info.cancel();
            info.setReturnValue(false);
        }
    }
    
    @Override
    public boolean isElytraFlying()
    {
        return (SummitStatic.ELYTRAFLY != null && SummitStatic.ELYTRAFLY.isEnabled() && SummitStatic.ELYTRAFLY.Mode.getValue().equals("Packet")) ? false : getFlag(7);
    }

    @Inject(method = "sendChatMessage", at = @At("HEAD"), cancellable = true)
    public void sendChatMessage(String msg, CallbackInfo info)
    {
        if (msg.startsWith(Commands.Prefix.getValue()))
        {
            CommandManager.Get().processCommand(msg.substring(1));
            info.cancel();
        }
    }
}
