package com.salhack.summit.main;

import com.salhack.summit.SummitMod;
import com.salhack.summit.events.client.EventClientTick;
import com.salhack.summit.events.minecraft.EventKeyInput;
import com.salhack.summit.events.render.EventRenderGetFOVModifier;
import com.salhack.summit.events.render.RenderEvent;
import com.salhack.summit.managers.MacroManager;
import com.salhack.summit.managers.ModuleManager;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraftforge.client.event.*;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.living.LivingEntityUseItemEvent;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.world.ChunkEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent.MouseInputEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.awt.image.BufferedImage;

import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

public class ForgeEventProcessor
{
    
    @SubscribeEvent
    public void onRender(RenderWorldLastEvent event)
    { 
        if (event.isCanceled())
            return;
        
        GlStateManager.disableTexture2D();
        GlStateManager.enableBlend();
        GlStateManager.disableAlpha();
        GlStateManager.tryBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, 1, 0);
        GlStateManager.shadeModel(GL11.GL_SMOOTH);
        GlStateManager.disableDepth();

        GlStateManager.glLineWidth(1f);
        SummitMod.EVENT_BUS.post(new RenderEvent(event.getPartialTicks()));
        GlStateManager.glLineWidth(1f);

        GlStateManager.shadeModel(GL11.GL_FLAT);
        GlStateManager.disableBlend();
        GlStateManager.enableAlpha();
        GlStateManager.enableTexture2D();
        GlStateManager.enableDepth();
        GlStateManager.enableCull();
    }
    
    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent event)
    {
        if (Wrapper.GetMC().player == null)
            return;

        SummitMod.EVENT_BUS.post(new EventClientTick());
    }

    @SubscribeEvent
    public void onEntitySpawn(EntityJoinWorldEvent event)
    {
        if (event.isCanceled())
            return;

        SummitMod.EVENT_BUS.post(event);
    }

    @SubscribeEvent(priority = EventPriority.NORMAL, receiveCanceled = true)
    public void onKeyInput(InputEvent.KeyInputEvent event)
    {
        if (Keyboard.getEventKeyState())
        {
            final String key = Keyboard.getKeyName(Keyboard.getEventKey());

            ModuleManager.Get().OnKeyPress(key);
            MacroManager.Get().OnKeyPress(key);
            
            if (!key.equals("NONE") && !key.isEmpty())
                SummitMod.EVENT_BUS.post(new EventKeyInput(key));
        }
    }

    @SubscribeEvent(priority = EventPriority.NORMAL, receiveCanceled = true)
    public void onKeyInput(MouseInputEvent event)
    {
        if (Mouse.getEventButtonState())
        {
            final String button = Mouse.getButtonName(Mouse.getEventButton());
            ModuleManager.Get().OnKeyPress(button);
            MacroManager.Get().OnKeyPress(button);
            SummitMod.EVENT_BUS.post(event);
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onPlayerDrawn(RenderPlayerEvent.Pre event)
    {
        SummitMod.EVENT_BUS.post(event);
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onPlayerDrawn(RenderPlayerEvent.Post event)
    {
        SummitMod.EVENT_BUS.post(event);
    }

    @SubscribeEvent
    public void onChunkLoaded(ChunkEvent.Load event)
    {
        SummitMod.EVENT_BUS.post(event);
    }

    @SubscribeEvent
    public void onChunkUnLoaded(ChunkEvent.Unload event)
    {
        SummitMod.EVENT_BUS.post(event);
    }

    @SubscribeEvent
    public void onInputUpdate(InputUpdateEvent event)
    {
        SummitMod.EVENT_BUS.post(event);
    }

    @SubscribeEvent
    public void onLivingEntityUseItemEventTick(LivingEntityUseItemEvent.Start entityUseItemEvent)
    {
        SummitMod.EVENT_BUS.post(entityUseItemEvent);
    }

    @SubscribeEvent
    public void onLivingDamageEvent(LivingDamageEvent event)
    {
        SummitMod.EVENT_BUS.post(event);
    }

    @SubscribeEvent
    public void onEntityJoinWorldEvent(EntityJoinWorldEvent entityJoinWorldEvent)
    {
        SummitMod.EVENT_BUS.post(entityJoinWorldEvent);
    }

    @SubscribeEvent
    public void onPlayerPush(PlayerSPPushOutOfBlocksEvent event)
    {
        SummitMod.EVENT_BUS.post(event);
    }

    @SubscribeEvent
    public void onLeftClickBlock(PlayerInteractEvent.LeftClickBlock event)
    {
        SummitMod.EVENT_BUS.post(event);
    }

    @SubscribeEvent
    public void onAttackEntity(AttackEntityEvent entityEvent)
    {
        SummitMod.EVENT_BUS.post(entityEvent);
    }

    @SubscribeEvent
    public void onRenderBlockOverlay(RenderBlockOverlayEvent event)
    {
        SummitMod.EVENT_BUS.post(event);
    }

    @SubscribeEvent
    public void onClientChat(ClientChatReceivedEvent event)
    {
        SummitMod.EVENT_BUS.post(event);
    }

    @SubscribeEvent
    public void getFOVModifier(EntityViewRenderEvent.FOVModifier p_Event)
    {
        EventRenderGetFOVModifier l_Event = new EventRenderGetFOVModifier((float) p_Event.getRenderPartialTicks(), true);
        SummitMod.EVENT_BUS.post(l_Event);
        if (l_Event.isCancelled())
        {
            p_Event.setFOV(l_Event.GetFOV());
        }
    }

    @SubscribeEvent
    public void LivingAttackEvent(LivingAttackEvent p_Event)
    {
        SummitMod.EVENT_BUS.post(p_Event);
    }
    
    @SubscribeEvent
    public void OnWorldChange(WorldEvent p_Event)
    {
        SummitMod.EVENT_BUS.post(p_Event);
    }
}
