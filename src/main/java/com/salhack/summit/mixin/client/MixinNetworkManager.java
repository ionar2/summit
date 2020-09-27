package com.salhack.summit.mixin.client;

import com.salhack.summit.events.MinecraftEvent;
import com.salhack.summit.events.network.EventClientPacket;
import com.salhack.summit.events.network.EventServerPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import io.netty.channel.ChannelHandlerContext;
import com.salhack.summit.SummitMod;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;

@Mixin(NetworkManager.class)
public class MixinNetworkManager
{
    @Inject(method = "sendPacket(Lnet/minecraft/network/Packet;)V", at = @At("HEAD"), cancellable = true)
    private void sendPacketPre(Packet<?> packet, CallbackInfo info)
    {
        EventClientPacket event = new EventClientPacket(packet, MinecraftEvent.Stage.Pre);
        SummitMod.EVENT_BUS.post(event);
        if (event.isCancelled())
            info.cancel();
    }

    @Inject(method = "sendPacket(Lnet/minecraft/network/Packet;)V", at = @At("RETURN"))
    private void sendPacketPost(Packet<?> packet, CallbackInfo callbackInfo)
    {
        SummitMod.EVENT_BUS.post(new EventClientPacket(packet, MinecraftEvent.Stage.Post));
    }

    @Inject(method = "channelRead0", at = @At("HEAD"), cancellable = true)
    private void channelReadPre(ChannelHandlerContext context, Packet<?> packet, CallbackInfo info)
    {
        EventServerPacket event = new EventServerPacket(packet, MinecraftEvent.Stage.Pre);
        SummitMod.EVENT_BUS.post(event);
        if (event.isCancelled())
            info.cancel();
    }

    @Inject(method = "channelRead0", at = @At("RETURN"))
    private void channelReadPost(ChannelHandlerContext context, Packet<?> packet, CallbackInfo info)
    {
        EventServerPacket event = new EventServerPacket(packet, MinecraftEvent.Stage.Post);
        SummitMod.EVENT_BUS.post(event);
        if (event.isCancelled())
            info.cancel();
    }
}
