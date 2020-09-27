package com.salhack.summit.module.combat;

import com.salhack.summit.events.MinecraftEvent;
import com.salhack.summit.events.bus.EventHandler;
import com.salhack.summit.events.bus.Listener;
import com.salhack.summit.events.player.EventPlayerMotionUpdate;
import com.salhack.summit.events.player.EventPlayerUpdate;
import com.salhack.summit.main.Summit;
import com.salhack.summit.module.Module;
import com.salhack.summit.module.Value;
import com.salhack.summit.util.BlockInteractionHelper;
import com.salhack.summit.util.MathUtil;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;

import java.util.ArrayList;
import java.util.function.Consumer;

public class Surround extends Module
{
    public final Value<String> Mode = new Value<>("Mode", new String[]
    { "M" }, "Mode of surrounding to use", "Normal");
    public final Value<Boolean> ToggleOffGround = new Value<Boolean>("ToggleOffGround", new String[]
    { "Toggles", "Disables" }, "Will toggle off after a place", false);
    public final Value<String> CenterMode = new Value<>("Center", new String[]
    { "Center" }, "Moves you to center of block", "NCP");
    
    public final Value<Boolean> ActivateOnlyOnShift = new Value<Boolean>("ActivateOnlyOnShift", new String[]
    { "AoOS" }, "Activates only when shift is pressed.", false);
    
    public Surround()
    {
        super("Surround", new String[]
        { "NoCrystal" }, "Automatically surrounds you with obsidian in the four cardinal direrctions", "NONE", 0x5324DB, ModuleType.COMBAT);
        setMetaData(getMetaData());
        
        Mode.addString("Normal");
        Mode.addString("Full");
        CenterMode.addString("Teleport");
        CenterMode.addString("NCP");
        CenterMode.addString("None");
    }
    
    private Vec3d Center = Vec3d.ZERO;
    
    public String getMetaData()
    {
        return CenterMode.getValue().toString();
    }

    @EventHandler
    private Listener<EventPlayerUpdate> onPlayerUpdate  = new Listener<>(event ->
    {
        setMetaData(getMetaData());
    });
    
    @Override
    public void onEnable()
    {
        super.onEnable();
        
        if (mc.player == null)
        {
            toggle();
            return;
        }
        
        if (ActivateOnlyOnShift.getValue())
            return;

        Center = GetCenter(mc.player.posX, mc.player.posY, mc.player.posZ);
        
        if (!CenterMode.getValue().equals("None"))
        {
            mc.player.motionX = 0;
            mc.player.motionZ = 0;
        }
        
        if (CenterMode.getValue().equals("Teleport"))
        {
            mc.player.connection.sendPacket(new CPacketPlayer.Position(Center.x, Center.y, Center.z, true));
            mc.player.setPosition(Center.x, Center.y, Center.z);
        }
    }

    @Override
    public void toggleNoSave()
    {
        
    }

    @EventHandler
    private Listener<EventPlayerMotionUpdate> OnPlayerUpdate = new Listener<>(p_Event ->
    {
        if (p_Event.getStage() != MinecraftEvent.Stage.Pre || p_Event.isCancelled())
            return;
        
        if (ActivateOnlyOnShift.getValue())
        {
            if (!mc.gameSettings.keyBindSneak.isKeyDown())
            {
                Center = Vec3d.ZERO;
                return;
            }
            
            if (Center == Vec3d.ZERO)
            {
                Center = GetCenter(mc.player.posX, mc.player.posY, mc.player.posZ);
                
                if (!CenterMode.getValue().equals("None"))
                {
                    mc.player.motionX = 0;
                    mc.player.motionZ = 0;
                }
                
                if (CenterMode.getValue().equals("Teleport"))
                {
                    mc.player.connection.sendPacket(new CPacketPlayer.Position(Center.x, Center.y, Center.z, true));
                    mc.player.setPosition(Center.x, Center.y, Center.z);
                }
            }
        }
        
        /// NCP Centering
        if (Center != Vec3d.ZERO && CenterMode.getValue().equals("NCP"))
        {
            double l_XDiff = Math.abs(Center.x - mc.player.posX);
            double l_ZDiff = Math.abs(Center.z - mc.player.posZ);
            
            if (l_XDiff <= 0.1 && l_ZDiff <= 0.1)
            {
                Center = Vec3d.ZERO;
            }
            else
            {
                double l_MotionX = Center.x-mc.player.posX;
                double l_MotionZ = Center.z-mc.player.posZ;
                
                mc.player.motionX = l_MotionX/2;
                mc.player.motionZ = l_MotionZ/2;
            }
        }
        
        if (!mc.player.onGround && !mc.player.prevOnGround && !ActivateOnlyOnShift.getValue())
        {
            if (ToggleOffGround.getValue())
            {
                toggle();
                Summit.SendMessage("[Surround]: You are off ground! toggling!");
                return;
            }
        }

        final Vec3d pos = MathUtil.interpolateEntity(mc.player, mc.getRenderPartialTicks());

        final BlockPos interpPos = new BlockPos(pos.x, pos.y, pos.z);

        ArrayList<BlockPos> surroundBlocks = new ArrayList<>();
        surroundBlocks.add(interpPos.north());
        surroundBlocks.add(interpPos.south());
        surroundBlocks.add(interpPos.east());
        surroundBlocks.add(interpPos.west());
        
        if (Mode.getValue().equals("Full"))
        {
            surroundBlocks.add(interpPos.north().east());
            surroundBlocks.add(interpPos.north().west());
            surroundBlocks.add(interpPos.south().east());
            surroundBlocks.add(interpPos.south().west());
        }
        
        /// We don't need to do anything if we are not surrounded
        if (IsSurrounded())
            return;
        
        int lastSlot;
        final int slot = findStackHotbar(Blocks.OBSIDIAN);
        if (hasStack(Blocks.OBSIDIAN) || slot != -1)
        {
            if ((mc.player.onGround))
            {
                lastSlot = mc.player.inventory.currentItem;
                mc.player.inventory.currentItem = slot;
                mc.playerController.updateController();

                for (BlockPos l_Pos : surroundBlocks)
                {
                    BlockInteractionHelper.ValidResult l_Result = BlockInteractionHelper.valid(l_Pos);
                    
                    if (l_Result == BlockInteractionHelper.ValidResult.AlreadyBlockThere && !mc.world.getBlockState(l_Pos).getMaterial().isReplaceable())
                        continue;
                    
                    if (l_Result == BlockInteractionHelper.ValidResult.NoEntityCollision)
                        continue;
                    
                    if (l_Result == BlockInteractionHelper.ValidResult.NoNeighbors)
                    {
                        final BlockPos[] l_Test = {  l_Pos.down(), l_Pos.north(), l_Pos.south(), l_Pos.east(), l_Pos.west(), l_Pos.up(), };

                        for (BlockPos l_Pos2 : l_Test)
                        {
                            BlockInteractionHelper.ValidResult l_Result2 = BlockInteractionHelper.valid(l_Pos2);

                            if (l_Result2 == BlockInteractionHelper.ValidResult.NoNeighbors || l_Result2 == BlockInteractionHelper.ValidResult.NoEntityCollision)
                                continue;

                            placeAtPos(p_Event, l_Pos2, lastSlot);
                            return;
                        }
                        
                        continue;
                    }

                    placeAtPos(p_Event, l_Pos, lastSlot);
                    return;
                }

                if (!p_Event.isCancelled())
                {
                    if (!slotEqualsBlock(lastSlot, Blocks.OBSIDIAN))
                    {
                        mc.player.inventory.currentItem = lastSlot;
                    }
                    mc.playerController.updateController();
                }
            }
        }
    });
    
    private void placeAtPos(EventPlayerMotionUpdate event, BlockPos pos, int lastSlot)
    {
        final Vec3d eyesPos = new Vec3d(mc.player.posX, mc.player.posY + mc.player.getEyeHeight(), mc.player.posZ);
        
        for (final EnumFacing side : EnumFacing.values())
        {
            final BlockPos neighbor = pos.offset(side);
            final EnumFacing side2 = side.getOpposite();

            if (mc.world.getBlockState(neighbor).getBlock().canCollideCheck(mc.world.getBlockState(neighbor), false))
            {
                final Vec3d hitVec = new Vec3d((Vec3i) neighbor).add(0.5, 0.5, 0.5).add(new Vec3d(side2.getDirectionVec()).scale(0.5));
                if (eyesPos.distanceTo(hitVec) <= 5.0f)
                {
                    float[] rotations = BlockInteractionHelper.getFacingRotations(pos.getX(), pos.getY(), pos.getZ(), side);
                    
                    event.cancel();
                    event.setPitch(rotations[1]);
                    event.setYaw(rotations[0]);
                    break;
                }
            }
        }

        if (event.isCancelled())
        {
            Consumer<EntityPlayerSP> post = p -> 
            {
                BlockInteractionHelper.place (pos, 5.0f, false, false);
                
                if (!slotEqualsBlock(lastSlot, Blocks.OBSIDIAN))
                {
                    mc.player.inventory.currentItem = lastSlot;
                }
                mc.playerController.updateController();
            };
            
            event.setFunct(post);
        }
    }
    
    public boolean IsSurrounded()
    {
        final Vec3d localPos = MathUtil.interpolateEntity(mc.player, mc.getRenderPartialTicks());

        final BlockPos interloptedPos = new BlockPos(localPos.x, localPos.y, localPos.z);

        ArrayList<BlockPos> surroundBlocks = new ArrayList<>();
        surroundBlocks.add(interloptedPos.north());
        surroundBlocks.add(interloptedPos.south());
        surroundBlocks.add(interloptedPos.east());
        surroundBlocks.add(interloptedPos.west());
        
        if (Mode.getValue().equals("Full"))
        {
            surroundBlocks.add(interloptedPos.north().east());
            surroundBlocks.add(interloptedPos.north().west());
            surroundBlocks.add(interloptedPos.south().east());
            surroundBlocks.add(interloptedPos.south().west());
        }
        
        for (BlockPos l_Pos : surroundBlocks)
        {
            if (BlockInteractionHelper.valid(l_Pos) != BlockInteractionHelper.ValidResult.AlreadyBlockThere)
            {
                return false;
            }
        }
        
        return true;
    }
    
    public boolean hasStack(Block type)
    {
        if (mc.player.inventory.getCurrentItem().getItem() instanceof ItemBlock)
        {
            final ItemBlock block = (ItemBlock) mc.player.inventory.getCurrentItem().getItem();
            return block.getBlock() == type;
        }
        return false;
    }

    private boolean slotEqualsBlock(int slot, Block type)
    {
        if (mc.player.inventory.getStackInSlot(slot).getItem() instanceof ItemBlock)
        {
            final ItemBlock block = (ItemBlock) mc.player.inventory.getStackInSlot(slot).getItem();
            return block.getBlock() == type;
        }

        return false;
    }

    private int findStackHotbar(Block type)
    {
        for (int i = 0; i < 9; i++)
        {
            final ItemStack stack = Minecraft.getMinecraft().player.inventory.getStackInSlot(i);
            if (stack.getItem() instanceof ItemBlock)
            {
                final ItemBlock block = (ItemBlock) stack.getItem();

                if (block.getBlock() == type)
                {
                    return i;
                }
            }
        }
        return -1;
    }

    public static Vec3d GetCenter(double posX, double posY, double posZ)
    {
        double x = Math.floor(posX) + 0.5D;
        double y = Math.floor(posY);
        double z = Math.floor(posZ) + 0.5D ;
        
        return new Vec3d(x, y, z);
    }

    public boolean HasObsidian()
    {
        return findStackHotbar(Blocks.OBSIDIAN) != -1;
    }
}
