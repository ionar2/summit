package com.salhack.summit.module.world;

import com.salhack.summit.events.MinecraftEvent;
import com.salhack.summit.events.bus.EventHandler;
import com.salhack.summit.events.bus.Listener;
import com.salhack.summit.events.player.EventPlayerMotionUpdate;
import com.salhack.summit.events.render.RenderEvent;
import com.salhack.summit.module.Module;
import com.salhack.summit.module.Value;
import com.salhack.summit.util.BlockInteractionHelper;
import com.salhack.summit.util.MathUtil;
import com.salhack.summit.util.Pair;
import com.salhack.summit.util.Timer;
import com.salhack.summit.util.entity.PlayerUtil;
import com.salhack.summit.util.render.RenderUtil;
import net.minecraft.block.Block;
import net.minecraft.block.BlockObsidian;
import net.minecraft.block.BlockSlab;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.CPacketEntityAction;
import net.minecraft.network.play.client.CPacketPlayerTryUseItemOnBlock;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayList;
import java.util.Iterator;

import static org.lwjgl.opengl.GL11.*;

public class AutoBuilder extends Module
{
    public final Value<String> Mode = new Value<>("Mode", new String[] {""}, "Mode", "Highway");
    public final Value<String> BuildingMode = new Value<>("BuildingMode", new String[] {""}, "Dynamic will update source block while walking, static keeps same position and resets on toggle", "Dynamic");
    public final Value<Integer> BlocksPerTick = new Value<Integer>("BlocksPerTick", new String[] {"BPT"}, "Blocks per tick", 4, 1, 10, 1);
    public final Value<Float> Delay = new Value<Float>("Delay", new String[] {"Delay"}, "Delay of the place", 0f, 0.0f, 1.0f, 0.1f);
    public final Value<Boolean> Visualize = new Value<Boolean>("Visualize", new String[] {"Render"}, "Visualizes where blocks are to be placed", true);
    
    public enum Modes
    {
        Highway,
        Swastika,
        HighwayTunnel,
        Portal,
        Flat,
        Tower,
        Cover,
        Wall,
        HighwayWall,
        Stair,
    }
    
    public enum BuildingModes
    {
        Dynamic,
        Static,
    }

    public AutoBuilder()
    {
        super("AutoBuilder", new String[]
        { "AutoSwastika" }, "Builds cool things at your facing block", "NONE", 0x96DB24, ModuleType.WORLD);
        setMetaData(Mode.getValue().toString() + " - " + BuildingMode.getValue().toString());
        
        Mode.addString("Highway");
        Mode.addString("Swastika");
        Mode.addString("HighwayTunnel");
        Mode.addString("Portal");
        Mode.addString("Flat");
        Mode.addString("Tower");
        Mode.addString("Cover");
        Mode.addString("Wall");
        Mode.addString("HighwayWall");
        Mode.addString("Stair");
        
        BuildingMode.addString("Dynamic");
        BuildingMode.addString("Static");
    }
    
    private Timer timer = new Timer();
    private Timer NetherPortalTimer = new Timer();
    @Override
    public void onEnable()
    {
        super.onEnable();
        
        if (mc.player == null)
        {
            toggle();
            return;
        }
        
        timer.reset();
        BlockArray.clear();
    }
    
    private boolean SentPacket = false;

    ArrayList<BlockPos> BlockArray = new ArrayList<BlockPos>();
    
    @EventHandler
    private Listener<EventPlayerMotionUpdate> OnPlayerUpdate = new Listener<>(p_Event ->
    {
        setMetaData(Mode.getValue().toString() + " - " + BuildingMode.getValue().toString());
        
        if (p_Event.getStage() != MinecraftEvent.Stage.Pre)
            return;
        
        if (!timer.passed(Delay.getValue() * 1000f))
            return;
        
        timer.reset();
        
        final Vec3d pos = MathUtil.interpolateEntity(mc.player, mc.getRenderPartialTicks());

        BlockPos orignPos = new BlockPos(pos.x, pos.y+0.5f, pos.z);

        int lastSlot;
        Pair<Integer, Block> l_Pair = findStackHotbar();
        
        int slot = -1;
        double l_Offset = pos.y - orignPos.getY();
        
        if (l_Pair != null)
        {
            slot = l_Pair.getFirst();
            
            if (l_Pair.getSecond() instanceof BlockSlab)
            {
                if (l_Offset == 0.5f)
                {
                    orignPos = new BlockPos(pos.x, pos.y+0.5f, pos.z);
                }
            }
        }
        
        if (BuildingMode.getValue().equals("Dynamic"))
            BlockArray.clear();
        
        if (BlockArray.isEmpty())
            FillBlockArrayAsNeeded(pos, orignPos, l_Pair);
        
        boolean l_NeedPlace = false;

        float[] rotations = null;
        
        if (slot != -1)
        {
            if ((mc.player.onGround))
            {
                lastSlot = mc.player.inventory.currentItem;
                mc.player.inventory.currentItem = slot;
                mc.playerController.updateController();
                
                int l_BlocksPerTick = BlocksPerTick.getValue();

                for (BlockPos l_Pos : BlockArray)
                {
                    /*ValidResult l_Result = BlockInteractionHelper.valid(l_Pos);
                    
                    if (l_Result == ValidResult.AlreadyBlockThere && !mc.world.getBlockState(l_Pos).getMaterial().isReplaceable())
                        continue;
                    
                    if (l_Result == ValidResult.NoNeighbors)
                        continue;*/
                    
                    BlockInteractionHelper.PlaceResult l_Place = BlockInteractionHelper.place (l_Pos, 5.0f, false, l_Offset == -0.5f);
                    
                    if (l_Place != BlockInteractionHelper.PlaceResult.Placed)
                        continue;
                    
                    l_NeedPlace = true;
                    rotations = BlockInteractionHelper.getLegitRotations(new Vec3d(l_Pos.getX(), l_Pos.getY(), l_Pos.getZ()));
                    if (--l_BlocksPerTick <= 0)
                        break;
                }

                if (!slotEqualsBlock(lastSlot, l_Pair.getSecond()))
                {
                    mc.player.inventory.currentItem = lastSlot;
                }
                mc.playerController.updateController();
            }
        }
        
        if (!l_NeedPlace && Mode.getValue().equals("Portal"))
        {
            if (mc.world.getBlockState(BlockArray.get(0).up()).getBlock() == Blocks.PORTAL || !VerifyPortalFrame(BlockArray))
                return;
            
            if (mc.player.getHeldItemMainhand().getItem() != Items.FLINT_AND_STEEL)
            {
                for (int l_I = 0; l_I < 9; ++l_I)
                {
                    ItemStack l_Stack = mc.player.inventory.getStackInSlot(l_I);
                    if (l_Stack.isEmpty())
                        continue;
                    
                    if (l_Stack.getItem() == Items.FLINT_AND_STEEL)
                    {
                        mc.player.inventory.currentItem = l_I;
                        mc.playerController.updateController();
                        NetherPortalTimer.reset();
                        break;
                    }
                }
            }
            
            if (!NetherPortalTimer.passed(500))
            {
                if (SentPacket)
                {
                    mc.player.swingArm(EnumHand.MAIN_HAND);
                    mc.getConnection().sendPacket(new CPacketPlayerTryUseItemOnBlock(BlockArray.get(0), EnumFacing.UP, EnumHand.MAIN_HAND, 0f, 0f, 0f));
                }
                
                rotations = BlockInteractionHelper.getLegitRotations(new Vec3d(BlockArray.get(0).getX(), BlockArray.get(0).getY()+0.5f, BlockArray.get(0).getZ()));
                l_NeedPlace = true;
            }
            else
                return;
        }
        else if (l_NeedPlace && Mode.getValue().equals("Portal"))
            NetherPortalTimer.reset();
        
        if (!l_NeedPlace || rotations == null)
        {
            SentPacket = false;
            return;
        }
        
        p_Event.cancel();
        
        /// @todo: clean this up

        boolean l_IsSprinting = mc.player.isSprinting();

        if (l_IsSprinting != mc.player.serverSprintState)
        {
            if (l_IsSprinting)
            {
                mc.player.connection.sendPacket(new CPacketEntityAction(mc.player, CPacketEntityAction.Action.START_SPRINTING));
            }
            else
            {
                mc.player.connection.sendPacket(new CPacketEntityAction(mc.player, CPacketEntityAction.Action.STOP_SPRINTING));
            }

            mc.player.serverSprintState = l_IsSprinting;
        }

        boolean l_IsSneaking = mc.player.isSneaking();

        if (l_IsSneaking != mc.player.serverSneakState)
        {
            if (l_IsSneaking)
            {
                mc.player.connection.sendPacket(new CPacketEntityAction(mc.player, CPacketEntityAction.Action.START_SNEAKING));
            }
            else
            {
                mc.player.connection.sendPacket(new CPacketEntityAction(mc.player, CPacketEntityAction.Action.STOP_SNEAKING));
            }

            mc.player.serverSneakState = l_IsSneaking;
        }

        p_Event.setYaw(rotations[0]);
        p_Event.setPitch(rotations[1]);
        SentPacket = true;
    });
    
    
    @EventHandler
    private Listener<RenderEvent> OnRenderEvent = new Listener<>(p_Event ->
    {
        if (!Visualize.getValue())
            return;
        
        Iterator l_Itr = BlockArray.iterator();

        while (l_Itr.hasNext()) 
        {
            BlockPos l_Pos = (BlockPos) l_Itr.next();
            
            IBlockState l_State = mc.world.getBlockState(l_Pos);
            
            if (l_State != null && l_State.getBlock() != Blocks.AIR && l_State.getBlock() != Blocks.WATER)
                continue;
            
            final AxisAlignedBB bb = new AxisAlignedBB(l_Pos.getX() - mc.getRenderManager().viewerPosX,
                    l_Pos.getY() - mc.getRenderManager().viewerPosY, l_Pos.getZ() - mc.getRenderManager().viewerPosZ,
                    l_Pos.getX() + 1 - mc.getRenderManager().viewerPosX,
                    l_Pos.getY() + (1) - mc.getRenderManager().viewerPosY,
                    l_Pos.getZ() + 1 - mc.getRenderManager().viewerPosZ);
    
            RenderUtil.camera.setPosition(mc.getRenderViewEntity().posX, mc.getRenderViewEntity().posY,
                    mc.getRenderViewEntity().posZ);
    
            if (RenderUtil.camera.isBoundingBoxInFrustum(new AxisAlignedBB(bb.minX + mc.getRenderManager().viewerPosX,
                    bb.minY + mc.getRenderManager().viewerPosY, bb.minZ + mc.getRenderManager().viewerPosZ,
                    bb.maxX + mc.getRenderManager().viewerPosX, bb.maxY + mc.getRenderManager().viewerPosY,
                    bb.maxZ + mc.getRenderManager().viewerPosZ)))
            {
                GlStateManager.pushMatrix();
                GlStateManager.enableBlend();
                GlStateManager.disableDepth();
                GlStateManager.tryBlendFuncSeparate(770, 771, 0, 1);
                GlStateManager.disableTexture2D();
                GlStateManager.depthMask(false);
                glEnable(GL_LINE_SMOOTH);
                glHint(GL_LINE_SMOOTH_HINT, GL_NICEST);
                glLineWidth(1.5f);
    
                final double dist = mc.player.getDistance(l_Pos.getX() + 0.5f, l_Pos.getY() + 0.5f, l_Pos.getZ() + 0.5f)
                        * 0.75f;
    
                MathUtil.clamp((float) (dist * 255.0f / 5.0f / 255.0f), 0.0f, 0.3f);

                //  public static void drawBoundingBox(AxisAlignedBB bb, float width, int color)
                
                
                int l_Color = 0x9000FFFF;
                
                RenderUtil.drawBoundingBox(bb, 1.0f, l_Color);
                RenderUtil.drawFilledBox(bb, l_Color);
                glDisable(GL_LINE_SMOOTH);
                GlStateManager.depthMask(true);
                GlStateManager.enableDepth();
                GlStateManager.enableTexture2D();
                GlStateManager.disableBlend();
                GlStateManager.popMatrix();
            }
        }
    });
    
    private boolean slotEqualsBlock(int slot, Block type)
    {
        if (mc.player.inventory.getStackInSlot(slot).getItem() instanceof ItemBlock)
        {
            final ItemBlock block = (ItemBlock) mc.player.inventory.getStackInSlot(slot).getItem();
            return block.getBlock() == type;
        }

        return false;
    }

    private void FillBlockArrayAsNeeded(final Vec3d pos, final BlockPos orignPos, final Pair<Integer, Block> p_Pair)
    {
        BlockPos interpPos = null;
        
        switch (Mode.getValue())
        {
            case "Highway":
                switch (PlayerUtil.GetFacing())
                {
                    case East:
                        BlockArray.add(orignPos.down());
                        BlockArray.add(orignPos.down().east());
                        BlockArray.add(orignPos.down().east().north());
                        BlockArray.add(orignPos.down().east().south());
                        BlockArray.add(orignPos.down().east().north().north());
                        BlockArray.add(orignPos.down().east().south().south());
                        BlockArray.add(orignPos.down().east().north().north().north());
                        BlockArray.add(orignPos.down().east().south().south().south());
                        BlockArray.add(orignPos.down().east().north().north().north().up());
                        BlockArray.add(orignPos.down().east().south().south().south().up());
                        break;
                    case North:
                        BlockArray.add(orignPos.down());
                        BlockArray.add(orignPos.down().north());
                        BlockArray.add(orignPos.down().north().east());
                        BlockArray.add(orignPos.down().north().west());
                        BlockArray.add(orignPos.down().north().east().east());
                        BlockArray.add(orignPos.down().north().west().west());
                        BlockArray.add(orignPos.down().north().east().east().east());
                        BlockArray.add(orignPos.down().north().west().west().west());
                        BlockArray.add(orignPos.down().north().east().east().east().up());
                        BlockArray.add(orignPos.down().north().west().west().west().up());
                        break;
                    case South:
                        BlockArray.add(orignPos.down());
                        BlockArray.add(orignPos.down().south());
                        BlockArray.add(orignPos.down().south().east());
                        BlockArray.add(orignPos.down().south().west());
                        BlockArray.add(orignPos.down().south().east().east());
                        BlockArray.add(orignPos.down().south().west().west());
                        BlockArray.add(orignPos.down().south().east().east().east());
                        BlockArray.add(orignPos.down().south().west().west().west());
                        BlockArray.add(orignPos.down().south().east().east().east().up());
                        BlockArray.add(orignPos.down().south().west().west().west().up());
                        break;
                    case West:
                        BlockArray.add(orignPos.down());
                        BlockArray.add(orignPos.down().west());
                        BlockArray.add(orignPos.down().west().north());
                        BlockArray.add(orignPos.down().west().south());
                        BlockArray.add(orignPos.down().west().north().north());
                        BlockArray.add(orignPos.down().west().south().south());
                        BlockArray.add(orignPos.down().west().north().north().north());
                        BlockArray.add(orignPos.down().west().south().south().south());
                        BlockArray.add(orignPos.down().west().north().north().north().up());
                        BlockArray.add(orignPos.down().west().south().south().south().up());
                        break;
                    default:
                        break;
                }
                break;
            case "HighwayTunnel":
                BlockArray.add(orignPos.down());
                BlockArray.add(orignPos.down().north());
                BlockArray.add(orignPos.down().north().east());
                BlockArray.add(orignPos.down().north().west());
                BlockArray.add(orignPos.down().north().east().east());
                BlockArray.add(orignPos.down().north().west().west());
                BlockArray.add(orignPos.down().north().east().east().east());
                BlockArray.add(orignPos.down().north().west().west().west());
                BlockArray.add(orignPos.down().north().east().east().east().up());
                BlockArray.add(orignPos.down().north().west().west().west().up());
                BlockArray.add(orignPos.down().north().east().east().east().up().up());
                BlockArray.add(orignPos.down().north().west().west().west().up().up());
                BlockArray.add(orignPos.down().north().east().east().east().up().up().up());
                BlockArray.add(orignPos.down().north().west().west().west().up().up().up());
                BlockArray.add(orignPos.down().north().east().east().east().up().up().up().up());
                BlockArray.add(orignPos.down().north().west().west().west().up().up().up().up());
                BlockArray.add(orignPos.down().north().east().east().east().up().up().up().up().west());
                BlockArray.add(orignPos.down().north().west().west().west().up().up().up().up().east());
                BlockArray.add(orignPos.down().north().east().east().east().up().up().up().up().west().west());
                BlockArray.add(orignPos.down().north().west().west().west().up().up().up().up().east().east());
                BlockArray.add(orignPos.down().north().east().east().east().up().up().up().up().west().west().west());
                BlockArray.add(orignPos.down().north().west().west().west().up().up().up().up().east().east().east());
                break;
            case "Swastika":
                switch (PlayerUtil.GetFacing())
                {
                    case East:
                        interpPos = new BlockPos(pos.x, pos.y, pos.z).east().east();
                        BlockArray.add(interpPos);
                        BlockArray.add(interpPos.north());
                        BlockArray.add(interpPos.north().north());
                        BlockArray.add(interpPos.up());
                        BlockArray.add(interpPos.up().up());
                        BlockArray.add(interpPos.up().up().north());
                        BlockArray.add(interpPos.up().up().north().north());
                        BlockArray.add(interpPos.up().up().north().north().up());
                        BlockArray.add(interpPos.up().up().north().north().up().up());
                        BlockArray.add(interpPos.up().up().south());
                        BlockArray.add(interpPos.up().up().south().south());
                        BlockArray.add(interpPos.up().up().south().south().down());
                        BlockArray.add(interpPos.up().up().south().south().down().down());
                        BlockArray.add(interpPos.up().up().up());
                        BlockArray.add(interpPos.up().up().up().up());
                        BlockArray.add(interpPos.up().up().up().up().south());
                        BlockArray.add(interpPos.up().up().up().up().south().south());
                        break;
                    case North:
                        interpPos = new BlockPos(pos.x, pos.y, pos.z).north().north();
                        BlockArray.add(interpPos);
                        BlockArray.add(interpPos.west());
                        BlockArray.add(interpPos.west().west());
                        BlockArray.add(interpPos.up());
                        BlockArray.add(interpPos.up().up());
                        BlockArray.add(interpPos.up().up().west());
                        BlockArray.add(interpPos.up().up().west().west());
                        BlockArray.add(interpPos.up().up().west().west().up());
                        BlockArray.add(interpPos.up().up().west().west().up().up());
                        BlockArray.add(interpPos.up().up().east());
                        BlockArray.add(interpPos.up().up().east().east());
                        BlockArray.add(interpPos.up().up().east().east().down());
                        BlockArray.add(interpPos.up().up().east().east().down().down());
                        BlockArray.add(interpPos.up().up().up());
                        BlockArray.add(interpPos.up().up().up().up());
                        BlockArray.add(interpPos.up().up().up().up().east());
                        BlockArray.add(interpPos.up().up().up().up().east().east());
                        break;
                    case South:
                        interpPos = new BlockPos(pos.x, pos.y, pos.z).south().south();
                        BlockArray.add(interpPos);
                        BlockArray.add(interpPos.east());
                        BlockArray.add(interpPos.east().east());
                        BlockArray.add(interpPos.up());
                        BlockArray.add(interpPos.up().up());
                        BlockArray.add(interpPos.up().up().east());
                        BlockArray.add(interpPos.up().up().east().east());
                        BlockArray.add(interpPos.up().up().east().east().up());
                        BlockArray.add(interpPos.up().up().east().east().up().up());
                        BlockArray.add(interpPos.up().up().west());
                        BlockArray.add(interpPos.up().up().west().west());
                        BlockArray.add(interpPos.up().up().west().west().down());
                        BlockArray.add(interpPos.up().up().west().west().down().down());
                        BlockArray.add(interpPos.up().up().up());
                        BlockArray.add(interpPos.up().up().up().up());
                        BlockArray.add(interpPos.up().up().up().up().west());
                        BlockArray.add(interpPos.up().up().up().up().west().west());
                        break;
                    case West:
                        interpPos = new BlockPos(pos.x, pos.y, pos.z).west().west();
                        BlockArray.add(interpPos);
                        BlockArray.add(interpPos.south());
                        BlockArray.add(interpPos.south().south());
                        BlockArray.add(interpPos.up());
                        BlockArray.add(interpPos.up().up());
                        BlockArray.add(interpPos.up().up().south());
                        BlockArray.add(interpPos.up().up().south().south());
                        BlockArray.add(interpPos.up().up().south().south().up());
                        BlockArray.add(interpPos.up().up().south().south().up().up());
                        BlockArray.add(interpPos.up().up().north());
                        BlockArray.add(interpPos.up().up().north().north());
                        BlockArray.add(interpPos.up().up().north().north().down());
                        BlockArray.add(interpPos.up().up().north().north().down().down());
                        BlockArray.add(interpPos.up().up().up());
                        BlockArray.add(interpPos.up().up().up().up());
                        BlockArray.add(interpPos.up().up().up().up().north());
                        BlockArray.add(interpPos.up().up().up().up().north().north());
                        break;
                    default:
                        break;
                }
                break;
            case "Portal":

                switch (PlayerUtil.GetFacing())
                {
                    case East:
                        interpPos = new BlockPos(pos.x, pos.y, pos.z).east().east();
                        BlockArray.add(interpPos.south());
                        BlockArray.add(interpPos.south().south());
                        BlockArray.add(interpPos);
                        BlockArray.add(interpPos.south().south().up());
                        BlockArray.add(interpPos.south().south().up().up());
                        BlockArray.add(interpPos.south().south().up().up().up());
                        BlockArray.add(interpPos.south().south().up().up().up().up());
                        BlockArray.add(interpPos.south().south().up().up().up().up().north());
                        BlockArray.add(interpPos.south().south().up().up().up().up().north().north());
                        BlockArray.add(interpPos.south().south().up().up().up().up().north().north().north());
                        BlockArray.add(interpPos.south().south().up().up().up().up().north().north().north().down());
                        BlockArray.add(interpPos.south().south().up().up().up().up().north().north().north().down().down());
                        BlockArray.add(interpPos.south().south().up().up().up().up().north().north().north().down().down().down());
                        BlockArray.add(interpPos.south().south().up().up().up().up().north().north().north().down().down().down().down());
                        break;
                    case North:
                        interpPos = new BlockPos(pos.x, pos.y, pos.z).north().north();
                        BlockArray.add(interpPos.east());
                        BlockArray.add(interpPos.east().east());
                        BlockArray.add(interpPos);
                        BlockArray.add(interpPos.east().east().up());
                        BlockArray.add(interpPos.east().east().up().up());
                        BlockArray.add(interpPos.east().east().up().up().up());
                        BlockArray.add(interpPos.east().east().up().up().up().up());
                        BlockArray.add(interpPos.east().east().up().up().up().up().west());
                        BlockArray.add(interpPos.east().east().up().up().up().up().west().west());
                        BlockArray.add(interpPos.east().east().up().up().up().up().west().west().west());
                        BlockArray.add(interpPos.east().east().up().up().up().up().west().west().west().down());
                        BlockArray.add(interpPos.east().east().up().up().up().up().west().west().west().down().down());
                        BlockArray.add(interpPos.east().east().up().up().up().up().west().west().west().down().down().down());
                        BlockArray.add(interpPos.east().east().up().up().up().up().west().west().west().down().down().down().down());
                        break;
                    case South:
                        interpPos = new BlockPos(pos.x, pos.y, pos.z).south().south();
                        BlockArray.add(interpPos.east());
                        BlockArray.add(interpPos.east().east());
                        BlockArray.add(interpPos);
                        BlockArray.add(interpPos.east().east().up());
                        BlockArray.add(interpPos.east().east().up().up());
                        BlockArray.add(interpPos.east().east().up().up().up());
                        BlockArray.add(interpPos.east().east().up().up().up().up());
                        BlockArray.add(interpPos.east().east().up().up().up().up().west());
                        BlockArray.add(interpPos.east().east().up().up().up().up().west().west());
                        BlockArray.add(interpPos.east().east().up().up().up().up().west().west().west());
                        BlockArray.add(interpPos.east().east().up().up().up().up().west().west().west().down());
                        BlockArray.add(interpPos.east().east().up().up().up().up().west().west().west().down().down());
                        BlockArray.add(interpPos.east().east().up().up().up().up().west().west().west().down().down().down());
                        BlockArray.add(interpPos.east().east().up().up().up().up().west().west().west().down().down().down().down());
                        break;
                    case West:
                        interpPos = new BlockPos(pos.x, pos.y, pos.z).west().west();
                        BlockArray.add(interpPos.south());
                        BlockArray.add(interpPos.south().south());
                        BlockArray.add(interpPos);
                        BlockArray.add(interpPos.south().south().up());
                        BlockArray.add(interpPos.south().south().up().up());
                        BlockArray.add(interpPos.south().south().up().up().up());
                        BlockArray.add(interpPos.south().south().up().up().up().up());
                        BlockArray.add(interpPos.south().south().up().up().up().up().north());
                        BlockArray.add(interpPos.south().south().up().up().up().up().north().north());
                        BlockArray.add(interpPos.south().south().up().up().up().up().north().north().north());
                        BlockArray.add(interpPos.south().south().up().up().up().up().north().north().north().down());
                        BlockArray.add(interpPos.south().south().up().up().up().up().north().north().north().down().down());
                        BlockArray.add(interpPos.south().south().up().up().up().up().north().north().north().down().down().down());
                        BlockArray.add(interpPos.south().south().up().up().up().up().north().north().north().down().down().down().down());
                        break;
                    default:
                        break;
                }
                break;
            case "Flat":
                
                for (int l_X = -3; l_X <= 3; ++l_X)
                    for (int l_Y = -3; l_Y <= 3; ++l_Y)
                    {
                        BlockArray.add(orignPos.down().add(l_X, 0, l_Y));
                    }
                
                break;
            case "Cover":
                if (p_Pair == null)
                    return;
                
                for (int l_X = -3; l_X < 3; ++l_X)
                    for (int l_Y = -3; l_Y < 3; ++l_Y)
                    {
                        int l_Tries = 5;
                        BlockPos l_Pos = orignPos.down().add(l_X, 0, l_Y);
                        
                        if (mc.world.getBlockState(l_Pos).getBlock() == p_Pair.getSecond() || mc.world.getBlockState(l_Pos.down()).getBlock() == Blocks.AIR || mc.world.getBlockState(l_Pos.down()).getBlock() == p_Pair.getSecond())
                            continue;
                        
                        while (mc.world.getBlockState(l_Pos).getBlock() != Blocks.AIR && mc.world.getBlockState(l_Pos).getBlock() != Blocks.FIRE)
                        {
                            if (mc.world.getBlockState(l_Pos).getBlock() == p_Pair.getSecond())
                                break;
                            
                            l_Pos = l_Pos.up();
                            
                            if (--l_Tries <= 0)
                                break;
                        }
                        
                        BlockArray.add(l_Pos);
                    }
                break;
            case "Tower":
                BlockArray.add(orignPos.up());
                BlockArray.add(orignPos);
                BlockArray.add(orignPos.down());
                break;
            case "Wall":

                switch (PlayerUtil.GetFacing())
                {
                    case East:
                        interpPos = new BlockPos(pos.x, pos.y, pos.z).east().east();
                        
                        for (int l_X = -3; l_X <= 3; ++l_X)
                        {
                            for (int l_Y = -3; l_Y <= 3; ++l_Y)
                            {
                                BlockArray.add(interpPos.add(0, l_Y, l_X));
                            }
                        }
                        break;
                    case North:
                        interpPos = new BlockPos(pos.x, pos.y, pos.z).north().north();
                        
                        for (int l_X = -3; l_X <= 3; ++l_X)
                        {
                            for (int l_Y = -3; l_Y <= 3; ++l_Y)
                            {
                                BlockArray.add(interpPos.add(l_X, l_Y, 0));
                            }
                        }
                        break;
                    case South:
                        interpPos = new BlockPos(pos.x, pos.y, pos.z).south().south();
                        
                        for (int l_X = -3; l_X <= 3; ++l_X)
                        {
                            for (int l_Y = -3; l_Y <= 3; ++l_Y)
                            {
                                BlockArray.add(interpPos.add(l_X, l_Y, 0));
                            }
                        }
                        break;
                    case West:
                        interpPos = new BlockPos(pos.x, pos.y, pos.z).west().west();
                        
                        for (int l_X = -3; l_X <= 3; ++l_X)
                        {
                            for (int l_Y = -3; l_Y <= 3; ++l_Y)
                            {
                                BlockArray.add(interpPos.add(0, l_Y, l_X));
                            }
                        }
                        break;
                    default:
                        break;
                }
                break;
            case "HighwayWall":
                switch (PlayerUtil.GetFacing())
                {
                    case East:
                        interpPos = new BlockPos(pos.x, pos.y, pos.z).east().east();
                        
                        for (int l_X = -2; l_X <= 3; ++l_X)
                        {
                            for (int l_Y = 0; l_Y < 3; ++l_Y)
                            {
                                BlockArray.add(interpPos.add(0, l_Y, l_X));
                            }
                        }
                        break;
                    case North:
                        interpPos = new BlockPos(pos.x, pos.y, pos.z).north().north();
                        
                        for (int l_X = -2; l_X <= 3; ++l_X)
                        {
                            for (int l_Y = 0; l_Y < 3; ++l_Y)
                            {
                                BlockArray.add(interpPos.add(l_X, l_Y, 0));
                            }
                        }
                        break;
                    case South:
                        interpPos = new BlockPos(pos.x, pos.y, pos.z).south().south();
                        
                        for (int l_X = -2; l_X <= 3; ++l_X)
                        {
                            for (int l_Y = 0; l_Y < 3; ++l_Y)
                            {
                                BlockArray.add(interpPos.add(l_X, l_Y, 0));
                            }
                        }
                        break;
                    case West:
                        interpPos = new BlockPos(pos.x, pos.y, pos.z).west().west();
                        
                        for (int l_X = -2; l_X <= 3; ++l_X)
                        {
                            for (int l_Y = 0; l_Y < 3; ++l_Y)
                            {
                                BlockArray.add(interpPos.add(0, l_Y, l_X));
                            }
                        }
                        break;
                    default:
                        break;
                }
                break;
            case "Stair":
                
                interpPos = orignPos.down();
                
                switch (PlayerUtil.GetFacing())
                {
                    case East:
                        BlockArray.add(interpPos.east());
                        BlockArray.add(interpPos.east().up());
                        break;
                    case North:
                        BlockArray.add(interpPos.north());
                        BlockArray.add(interpPos.north().up());
                        break;
                    case South:
                        BlockArray.add(interpPos.south());
                        BlockArray.add(interpPos.south().up());
                        break;
                    case West:
                        BlockArray.add(interpPos.west());
                        BlockArray.add(interpPos.west().up());
                        break;
                    default:
                        break;
                }
                break;
            default:
                break;
            
        }
    }

    private Pair<Integer, Block> findStackHotbar()
    {
        if (mc.player.getHeldItemMainhand().getItem() instanceof ItemBlock)
            return new Pair<Integer, Block>(mc.player.inventory.currentItem, ((ItemBlock)mc.player.getHeldItemMainhand().getItem()).getBlock());
        
        for (int i = 0; i < 9; i++)
        {
            final ItemStack stack = Minecraft.getMinecraft().player.inventory.getStackInSlot(i);
            if (stack.getItem() instanceof ItemBlock)
            {
                final ItemBlock block = (ItemBlock) stack.getItem();
                
                return new Pair<Integer, Block>(i, block.getBlock());
            }
        }
        return null;
    }

    /// Verifies the array is all obsidian
    private boolean VerifyPortalFrame(ArrayList<BlockPos> p_Blocks)
    {
        for (BlockPos l_Pos : p_Blocks)
        {
            IBlockState l_State = mc.world.getBlockState(l_Pos);
            
            if (l_State == null || !(l_State.getBlock() instanceof BlockObsidian))
                return false;
        }
        
        return true;
    }
}
