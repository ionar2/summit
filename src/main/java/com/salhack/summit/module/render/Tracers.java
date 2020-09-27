package com.salhack.summit.module.render;

import com.salhack.summit.events.MinecraftEvent;
import com.salhack.summit.events.bus.EventHandler;
import com.salhack.summit.events.bus.Listener;
import com.salhack.summit.events.network.EventServerPacket;
import com.salhack.summit.events.render.RenderEvent;
import com.salhack.summit.events.world.EventChunkLoad;
import com.salhack.summit.events.world.EventLoadWorld;
import com.salhack.summit.main.Summit;
import com.salhack.summit.managers.FriendManager;
import com.salhack.summit.module.Module;
import com.salhack.summit.module.Value;
import com.salhack.summit.util.MathUtil;
import com.salhack.summit.util.entity.EntityUtil;
import com.salhack.summit.util.render.RenderUtil;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import com.mojang.realmsclient.gui.ChatFormatting;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityBoat;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.item.EntityMinecart;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.network.play.server.SPacketJoinGame;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.event.HoverEvent;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.storage.ExtendedBlockStorage;

// Tracer mod by Hoodlands :P thanks to ionar and jumbo for holding my hand
public class Tracers extends Module
{
    public final Value<Boolean> Players = new Value<Boolean>("Players", new String[]
    { "Players" }, "Traces players", true);
    public final Value<Boolean> Friends = new Value<Boolean>("Friends", new String[]
    { "Friends" }, "Traces friends", true);
    public final Value<Boolean> Invisibles = new Value<Boolean>("Invisibles", new String[]
    { "Invisibles" }, "Traces invisibles", true);
    public final Value<Boolean> Monsters = new Value<Boolean>("Monsters", new String[]
    { "Monsters" }, "Traces monsters", false);
    public final Value<Boolean> Animals = new Value<Boolean>("Animals", new String[]
    { "Animals" }, "Traces animals", false);
    public final Value<Boolean> Vehicles = new Value<Boolean>("Vehicles", new String[]
    { "Vehicles" }, "Traces Vehicles", false);
    public final Value<Boolean> Items = new Value<Boolean>("Items", new String[]
    { "Items" }, "Traces items", true);
    public final Value<Boolean> Others = new Value<Boolean>("Others", new String[]
    { "Others" }, "Traces others", false);
    public final Value<Boolean> Portals = new Value<Boolean>("Portals", new String[]
    { "Portals" }, "Traces Portals", true);
    public final Value<Integer> Width = new Value<Integer>("Width", new String[]{"Width"}, "Width", 1,1, 30, 1);

    public Tracers()
    {
        super("Tracers", new String[]
        { "Tracers" }, "Draws tracer to a given entity", "NONE", -1, ModuleType.RENDER);
    }

    private final List<Vec3d> portals = new CopyOnWriteArrayList<>();

    @EventHandler
    private Listener<EventServerPacket> onServerPacket = new Listener<>(event ->
    {
        if (event.getStage() != MinecraftEvent.Stage.Pre)
            return;
        
        if (event.getPacket() instanceof SPacketJoinGame)
        {
            this.portals.clear();
        }
    });

    @EventHandler
    private Listener<RenderEvent> OnRenderEvent = new Listener<>(event ->
    {
        if (mc.getRenderManager() == null || mc.getRenderManager().options == null)
            return;

        for (Entity entity : mc.world.loadedEntityList)
        {
            if (shouldRenderTracer(entity))
            {
                final Vec3d pos = MathUtil.interpolateEntity(entity, event.getPartialTicks()).subtract(
                        mc.getRenderManager().renderPosX, mc.getRenderManager().renderPosY,
                        mc.getRenderManager().renderPosZ);

                if (pos != null)
                {
                    final boolean bobbing = mc.gameSettings.viewBobbing;
                    mc.gameSettings.viewBobbing = false;
                    mc.entityRenderer.setupCameraTransform(event.getPartialTicks(), 0);
                    final Vec3d forward = new Vec3d(0, 0, 1)
                            .rotatePitch(-(float) Math.toRadians(Minecraft.getMinecraft().player.rotationPitch))
                            .rotateYaw(-(float) Math.toRadians(Minecraft.getMinecraft().player.rotationYaw));
                    RenderUtil.drawLine3D((float) forward.x, (float) forward.y + mc.player.getEyeHeight(),
                            (float) forward.z, (float) pos.x, (float) pos.y, (float) pos.z, Width.getValue(), getColor(entity));
                    mc.gameSettings.viewBobbing = bobbing;
                    mc.entityRenderer.setupCameraTransform(event.getPartialTicks(), 0);
                }
            }
        }

        if (Portals.getValue())
        {
            for (Vec3d portal : this.portals)
            {
                GlStateManager.pushMatrix();
                final boolean bobbing = mc.gameSettings.viewBobbing;
                mc.gameSettings.viewBobbing = false;
                mc.entityRenderer.setupCameraTransform(event.getPartialTicks(), 0);

                final Vec3d forward = new Vec3d(0, 0, 1)
                        .rotatePitch(-(float) Math.toRadians(Minecraft.getMinecraft().player.rotationPitch))
                        .rotateYaw(-(float) Math.toRadians(Minecraft.getMinecraft().player.rotationYaw));

                // Line
                RenderUtil.drawLine3D((float) forward.x, (float) forward.y + mc.player.getEyeHeight(),
                        (float) forward.z, (float) (portal.x - mc.getRenderManager().renderPosX),
                        (float) (portal.y - mc.getRenderManager().renderPosY),
                        (float) (portal.z - mc.getRenderManager().renderPosZ) , Width.getValue(), 0xFFFFFF);

                // Info
                RenderUtil.glBillboardDistanceScaled((float) portal.x, (float) portal.y, (float) portal.z, mc.player,
                        1f);
                GlStateManager.disableDepth();
                this.drawPortalInfoText(portal, 0, 0);
                GlStateManager.enableDepth();

                mc.gameSettings.viewBobbing = bobbing;
                mc.entityRenderer.setupCameraTransform(event.getPartialTicks(), 0);
                GlStateManager.popMatrix();
            }
        }
    });

    @EventHandler
    private Listener<EventChunkLoad> onChunkLoad = new Listener<>(event ->
    {
        switch (event.getType())
        {
            case LOAD:
                final Chunk chunk = event.getChunk();
                final ExtendedBlockStorage[] blockStoragesLoad = chunk.getBlockStorageArray();
                for (int i = 0; i < blockStoragesLoad.length; i++)
                {
                    final ExtendedBlockStorage extendedBlockStorage = blockStoragesLoad[i];
                    if (extendedBlockStorage == null)
                    {
                        continue;
                    }

                    for (int x = 0; x < 16; ++x)
                    {
                        for (int y = 0; y < 16; ++y)
                        {
                            for (int z = 0; z < 16; ++z)
                            {
                                final IBlockState blockState = extendedBlockStorage.get(x, y, z);
                                final int worldY = y + extendedBlockStorage.getYLocation();
                                if (blockState.getBlock().equals(Blocks.PORTAL))
                                {
                                    BlockPos position = new BlockPos(event.getChunk().getPos().getXStart() + x, worldY,
                                            event.getChunk().getPos().getZStart() + z);
                                    if (!isPortalCached(position.getX(), position.getY(), position.getZ(), 0))
                                    {
                                        final Vec3d portal = new Vec3d(position.getX(), position.getY(),
                                                position.getZ());
                                        this.portals.add(portal);
                                        if (Portals.getValue())
                                            this.printPortalToChat(portal);
                                        return;
                                    }
                                }
                                if (blockState.getBlock().equals(Blocks.END_PORTAL))
                                {
                                    BlockPos position = new BlockPos(event.getChunk().getPos().getXStart() + x, worldY,
                                            event.getChunk().getPos().getZStart() + z);
                                    if (!isPortalCached(position.getX(), position.getY(), position.getZ(), 3))
                                    {
                                        final Vec3d portal = new Vec3d(position.getX(), position.getY(),
                                                position.getZ());
                                        this.portals.add(portal);
                                        if (Portals.getValue())
                                            this.printEndPortalToChat(portal);
                                        return;
                                    }
                                }
                            }
                        }
                    }
                }
                break;
            case UNLOAD:
                for (Vec3d portal : this.portals)
                {
                    if (mc.player.getDistance(portal.x, portal.y, portal.z) > 200f)
                    {
                        this.portals.remove(portal);
                    }
                }
                break;
        }
    });

    @EventHandler
    private Listener<EventLoadWorld> onLoadWorld = new Listener<>(event ->
    {
        this.portals.clear();
    });

    private boolean isPortalCached(int x, int y, int z, float dist)
    {
        for (int i = this.portals.size() - 1; i >= 0; i--)
        {
            Vec3d searchPortal = this.portals.get(i);

            if (searchPortal.distanceTo(new Vec3d(x, y, z)) <= dist)
                return true;

            if (searchPortal.x == x && searchPortal.y == y && searchPortal.z == z)
                return true;
        }
        return false;
    }

    private void printEndPortalToChat(Vec3d portal)
    {
        final TextComponentString portalTextComponent = new TextComponentString("End Portal found!");

        String coords = String.format("X: %s, Y: %s, Z: %s", (int) portal.x, (int) portal.y, (int) portal.z);
        int playerDistance = (int) Minecraft.getMinecraft().player.getDistance(portal.x, portal.y, portal.z);
        String distance = ChatFormatting.GRAY + "" + playerDistance + "m away";

        String hoverText = coords + "\n" + distance;
        portalTextComponent.setStyle(new Style()
                .setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new TextComponentString(hoverText))));

        Summit.SendMessage(portalTextComponent);
    }

    private void printPortalToChat(Vec3d portal)
    {
        final TextComponentString portalTextComponent = new TextComponentString("Portal found!");

        String overworld = "";
        String nether = "";

        if (Minecraft.getMinecraft().player.dimension == 0)
        { // overworld
            overworld = String.format("Overworld: X: %s, Y: %s, Z: %s", (int) portal.x, (int) portal.y, (int) portal.z);
            nether = String.format("Nether: X: %s, Y: %s, Z: %s", (int) portal.x / 8, (int) portal.y,
                    (int) portal.z / 8);
        } else if (Minecraft.getMinecraft().player.dimension == -1)
        { // nether
            overworld = String.format("Overworld: X: %s, Y: %s, Z: %s", (int) portal.x * 8, (int) portal.y,
                    (int) portal.z * 8);
            nether = String.format("Nether: X: %s, Y: %s, Z: %s", (int) portal.x, (int) portal.y, (int) portal.z);
        }

        int playerDistance = (int) Minecraft.getMinecraft().player.getDistance(portal.x, portal.y, portal.z);
        String distance = ChatFormatting.GRAY + "" + playerDistance + "m away";

        String hoverText = overworld + "\n" + nether + "\n" + distance;
        portalTextComponent.setStyle(new Style()
                .setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new TextComponentString(hoverText))));

        Summit.SendMessage(portalTextComponent);
    }

    private void drawPortalInfoText(Vec3d portal, float x, float y)
    {
        Minecraft.getMinecraft().fontRenderer.drawStringWithShadow(
                (int) Minecraft.getMinecraft().player.getDistance(portal.x, portal.y, portal.z) + "m", x, y,
                0xFFAAAAAA);
    }

    public List<Vec3d> getPortals()
    {
        return portals;
    }

    public boolean shouldRenderTracer(Entity e)
    {
        if (e == Minecraft.getMinecraft().player)
            return false;
        if (e instanceof EntityPlayer)
            return Players.getValue();
        if ((EntityUtil.isHostileMob(e) || EntityUtil.isNeutralMob(e)))
            return Monsters.getValue();
        if (EntityUtil.isPassive(e))
            return Animals.getValue();
        if ((e instanceof EntityBoat || e instanceof EntityMinecart))
            return Vehicles.getValue();
        if (e instanceof EntityItem)
            return Items.getValue();
        return Others.getValue();
    }

    private int getColor(Entity e)
    {
        if (e instanceof EntityPlayer)
        {
            if (FriendManager.Get().IsFriend(e))
                return 0xFF00FFEE;
            return 0xFF00FF00;
        }
        if (e.isInvisible())
            return 0xFF000000;
        if ((EntityUtil.isHostileMob(e) || EntityUtil.isNeutralMob(e)))
            return 0xFFFF0000;
        if (EntityUtil.isPassive(e))
            return 0xFFFF8D00;
        if ((e instanceof EntityBoat || e instanceof EntityMinecart))
            return 0xFFFFFF00;
        if (e instanceof EntityItem)
            return 0xFFAA00FF;
        return 0xFFFFFFFF;
    }
}
