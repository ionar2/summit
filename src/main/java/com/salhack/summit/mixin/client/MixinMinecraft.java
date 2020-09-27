package com.salhack.summit.mixin.client;

import javax.annotation.Nullable;

import com.salhack.summit.events.minecraft.GuiScreenEvent;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.Inject;

import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.SoundHandler;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.GuiGameOver;
import net.minecraft.client.gui.GuiIngame;
import net.minecraft.client.gui.GuiMainMenu;
import net.minecraft.client.gui.GuiMultiplayer;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.multiplayer.PlayerControllerMP;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.particle.ParticleManager;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.util.math.RayTraceResult;

import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.salhack.summit.SummitMod;
import com.salhack.summit.events.world.EventLoadWorld;
import com.salhack.summit.gui.ingame.SalGuiIngame;
import com.salhack.summit.main.Wrapper;
import com.salhack.summit.util.CrystalUtils2;

@Mixin(Minecraft.class)
public class MixinMinecraft
{
    @Shadow
    WorldClient world;
    @Shadow
    EntityPlayerSP player;
    @Shadow
    GuiScreen currentScreen;
    @Shadow
    GameSettings gameSettings;
    @Shadow
    GuiIngame ingameGUI;
    @Shadow
    boolean skipRenderWorld;
    @Shadow
    SoundHandler soundHandler;
    @Shadow
    private int leftClickCounter;
    @Shadow
    public ParticleManager effectRenderer;
    @Shadow
    public RayTraceResult objectMouseOver;
    @Shadow
    public PlayerControllerMP playerController;

    @Inject(method = "init", at = @At("RETURN"))
    private void init(CallbackInfo callbackInfo)
    {
        ingameGUI = new SalGuiIngame(Wrapper.GetMC());
    }

   /* @Inject(method = "displayGuiScreen", at = @At("HEAD"), cancellable = true)
    public void displayGuiScreen(GuiScreen guiScreenIn, CallbackInfo info)
    {
        GuiScreenEvent.Closed screenEvent = new GuiScreenEvent.Closed(currentScreen);
        SummitMod.EVENT_BUS.post(screenEvent);
        GuiScreenEvent.Displayed screenEvent1 = new GuiScreenEvent.Displayed(guiScreenIn);
        SummitMod.EVENT_BUS.post(screenEvent1);
        guiScreenIn = screenEvent1.getScreen();

        if (guiScreenIn == null && this.world == null)
        {
            guiScreenIn = new GuiMainMenu();
        }
        else if (guiScreenIn == null && this.player.getHealth() <= 0.0F)
        {
            guiScreenIn = new GuiGameOver(null);
        }

        GuiScreen old = this.currentScreen;
        net.minecraftforge.client.event.GuiOpenEvent event = new net.minecraftforge.client.event.GuiOpenEvent(guiScreenIn);

        if (net.minecraftforge.common.MinecraftForge.EVENT_BUS.post(event))
            return;

        guiScreenIn = event.getGui();
        if (old != null && guiScreenIn != old)
        {
            old.onGuiClosed();
        }

        if (guiScreenIn instanceof GuiMainMenu || guiScreenIn instanceof GuiMultiplayer)
        {
            this.gameSettings.showDebugInfo = false;
            this.ingameGUI.getChatGUI().clearChatMessages(true);
        }

        this.currentScreen = guiScreenIn;

        if (guiScreenIn != null)
        {
            Minecraft.getMinecraft().setIngameNotInFocus();
            KeyBinding.unPressAllKeys();

            while (Mouse.next())
            {
            }

            while (Keyboard.next())
            {
            }

            ScaledResolution scaledresolution = new ScaledResolution(Minecraft.getMinecraft());
            int i = scaledresolution.getScaledWidth();
            int j = scaledresolution.getScaledHeight();
            guiScreenIn.setWorldAndResolution(Minecraft.getMinecraft(), i, j);
            this.skipRenderWorld = false;
        }
        else
        {
            this.soundHandler.resumeSounds();
            Minecraft.getMinecraft().setIngameFocus();
        }

        info.cancel();
    }*/

    @Inject(method = "loadWorld", at = @At("HEAD"))
    private void loadWorld(@Nullable WorldClient worldClientIn, CallbackInfo info)
    {
        CrystalUtils2.loadWorld();
        SummitMod.EVENT_BUS.post(new EventLoadWorld());
    }
}
