package com.salhack.summit.util.entity;

import java.text.DecimalFormat;

import com.salhack.summit.events.player.EventPlayerMotionUpdate;
import com.salhack.summit.main.Wrapper;
import com.salhack.summit.util.MinecraftInstance;

import net.minecraft.block.Block;
import net.minecraft.block.BlockLiquid;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.passive.EntityChicken;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemElytra;
import net.minecraft.item.ItemFood;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.CPacketEntityAction;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

public class PlayerUtil extends MinecraftInstance
{
    public static int GetItemSlot(Item input)
    {
        if (mc.player == null)
            return 0;

        for (int i = 0; i < mc.player.inventoryContainer.getInventory().size(); ++i)
        {
            if (i == 0 || i == 5 || i == 6 || i == 7 || i == 8)
                continue;

            ItemStack s = mc.player.inventoryContainer.getInventory().get(i);

            if (s.isEmpty())
                continue;

            if (s.getItem() == input)
            {
                return i;
            }
        }
        return -1;
    }

    public static int GetRecursiveItemSlot(Item input)
    {
        if (mc.player == null)
            return 0;

        for (int i = mc.player.inventoryContainer.getInventory().size() - 1; i > 0; --i)
        {
            if (i == 0 || i == 5 || i == 6 || i == 7 || i == 8)
                continue;

            ItemStack s = mc.player.inventoryContainer.getInventory().get(i);

            if (s.isEmpty())
                continue;

            if (s.getItem() == input)
            {
                return i;
            }
        }
        return -1;
    }

    public static int GetItemSlotNotHotbar(Item input)
    {
        if (mc.player == null)
            return 0;

        for (int i = 9; i < 36; i++)
        {
            final Item item = mc.player.inventory.getStackInSlot(i).getItem();
            if (item == input)
            {
                return i;
            }
        }
        return -1;
    }

    public static int GetItemCount(Item input)
    {
        if (mc.player == null)
            return 0;

        int items = 0;

        for (int i = 0; i < 45; i++)
        {
            final ItemStack stack = mc.player.inventory.getStackInSlot(i);
            if (stack.getItem() == input)
            {
                items += stack.getCount();
            }
        }

        return items;
    }

    private static Entity en = null;

    public static boolean CanSeeBlock(BlockPos p_Pos)
    {
        if (mc.player == null)
            return false;

        if (en == null && mc.world != null)
            en = new EntityChicken(mc.player.world);

        en.setPosition(p_Pos.getX()+0.5, p_Pos.getY()+0.5, p_Pos.getZ()+0.5);

        return mc.world.rayTraceBlocks(new Vec3d(mc.player.posX, mc.player.posY + (double)mc.player.getEyeHeight(), mc.player.posZ), new Vec3d(en.posX, en.posY, en.posZ), false, true, false) == null;
    }

    public static boolean isCurrentViewEntity()
    {
        return mc.getRenderViewEntity() == mc.player;
    }

    public static boolean IsEating()
    {
        return mc.player != null && mc.player.getHeldItemMainhand().getItem() instanceof ItemFood && mc.player.isHandActive();
    }

    public static int GetItemInHotbar(Item p_Item)
    {
        for (int l_I = 0; l_I < 9; ++l_I)
        {
            ItemStack l_Stack = mc.player.inventory.getStackInSlot(l_I);

            if (l_Stack != ItemStack.EMPTY)
            {
                if (l_Stack.getItem() == p_Item)
                {
                    return l_I;
                }
            }
        }

        return -1;
    }

    public static BlockPos GetLocalPlayerPosFloored()
    {
        if (mc.player == null)
            return BlockPos.ORIGIN;

        return new BlockPos(Math.floor(mc.player.posX), Math.floor(mc.player.posY), Math.floor(mc.player.posZ));
    }

    public static BlockPos EntityPosToFloorBlockPos(Entity e)
    {
        return new BlockPos(Math.floor(e.posX), Math.floor(e.posY), Math.floor(e.posZ));
    }

    public static float GetHealthWithAbsorption()
    {
        return mc.player.getHealth() + mc.player.getAbsorptionAmount();
    }

    public static boolean IsPlayerInHole()
    {
        BlockPos blockPos = GetLocalPlayerPosFloored();

        IBlockState blockState = mc.world.getBlockState(blockPos);

        if (blockState.getBlock() != Blocks.AIR)
            return false;

        if (mc.world.getBlockState(blockPos.up()).getBlock() != Blocks.AIR)
            return false;

        if (mc.world.getBlockState(blockPos.down()).getBlock() == Blocks.AIR)
            return false;

        final BlockPos[] touchingBlocks = new BlockPos[]
        { blockPos.north(), blockPos.south(), blockPos.east(), blockPos.west() };

        int validHorizontalBlocks = 0;
        for (BlockPos touching : touchingBlocks)
        {
            final IBlockState touchingState = mc.world.getBlockState(touching);
            if ((touchingState.getBlock() != Blocks.AIR) && touchingState.isFullBlock())
                validHorizontalBlocks++;
        }

        if (validHorizontalBlocks < 4)
            return false;

        return true;
    }

    public static boolean IsPlayerInHole(EntityPlayer who)
    {
        BlockPos blockPos = new BlockPos(Math.floor(who.posX), Math.floor(who.posY), Math.floor(who.posZ));

        IBlockState blockState = mc.world.getBlockState(blockPos);

        if (blockState.getBlock() != Blocks.AIR)
            return false;

        if (mc.world.getBlockState(blockPos.up()).getBlock() != Blocks.AIR)
            return false;

        if (mc.world.getBlockState(blockPos.down()).getBlock() == Blocks.AIR)
            return false;

        final BlockPos[] touchingBlocks = new BlockPos[]
        { blockPos.north(), blockPos.south(), blockPos.east(), blockPos.west() };

        int validHorizontalBlocks = 0;
        for (BlockPos touching : touchingBlocks)
        {
            final IBlockState touchingState = mc.world.getBlockState(touching);
            if ((touchingState.getBlock() != Blocks.AIR) && touchingState.isFullBlock())
                validHorizontalBlocks++;
        }

        if (validHorizontalBlocks < 4)
            return false;

        return true;
    }

    public static boolean isPlayerInHole(Block block)
    {
        BlockPos blockPos = GetLocalPlayerPosFloored();

        final BlockPos[] touchingBlocks = new BlockPos[]{blockPos.north(), blockPos.south(), blockPos.east(), blockPos.west()};

        int validHorizontalBlocks = 0;
        for (BlockPos touching : touchingBlocks)
        {
            final IBlockState touchingState = mc.world.getBlockState(touching);
            if (touchingState.getBlock() != Blocks.AIR && touchingState.isFullBlock())
            {
                if (block.equals(Blocks.OBSIDIAN))
                {
                    if (touchingState.getBlock().equals(Blocks.OBSIDIAN) || touchingState.getBlock().equals(Blocks.BEDROCK))
                    {
                        validHorizontalBlocks++;
                    }
                }
                else if (touchingState.getBlock().equals(block)) validHorizontalBlocks++;
            }
        }
        if (validHorizontalBlocks < 4) return false;

        return true;
    }

    public static boolean isEntityInHole(EntityPlayer who, Block block)
    {
        BlockPos blockPos = new BlockPos(Math.floor(who.posX), Math.floor(who.posY), Math.floor(who.posZ));

        final BlockPos[] touchingBlocks = new BlockPos[]{blockPos.north(), blockPos.south(), blockPos.east(), blockPos.west()};

        int validHorizontalBlocks = 0;
        for (BlockPos touching : touchingBlocks)
        {
            final IBlockState touchingState = mc.world.getBlockState(touching);
            if (touchingState.getBlock() != Blocks.AIR && touchingState.isFullBlock())
            {
                if (block.equals(Blocks.OBSIDIAN))
                {
                    if (touchingState.getBlock().equals(Blocks.OBSIDIAN) || touchingState.getBlock().equals(Blocks.BEDROCK))
                        validHorizontalBlocks++;
                }
                else if (touchingState.getBlock().equals(block)) validHorizontalBlocks++;
            }
        }
        if (validHorizontalBlocks < 4) return false;

        return true;
    }

    public static boolean IsPlayerTrapped()
    {
        BlockPos l_PlayerPos = GetLocalPlayerPosFloored();

        final BlockPos[] l_TrapPositions = {
                l_PlayerPos.down(),
                l_PlayerPos.up().up(),
                l_PlayerPos.north(),
                l_PlayerPos.south(),
                l_PlayerPos.east(),
                l_PlayerPos.west(),
                l_PlayerPos.north().up(),
                l_PlayerPos.south().up(),
                l_PlayerPos.east().up(),
                l_PlayerPos.west().up(),
                };

        for (BlockPos l_Pos : l_TrapPositions)
        {
            IBlockState l_State = mc.world.getBlockState(l_Pos);

            if (l_State.getBlock() != Blocks.OBSIDIAN && mc.world.getBlockState(l_Pos).getBlock() != Blocks.BEDROCK)
                return false;
        }

        return true;
    }

    public static boolean IsEntityTrapped(Entity e)
    {
        BlockPos l_PlayerPos = EntityPosToFloorBlockPos(e);

        final BlockPos[] l_TrapPositions = {
                l_PlayerPos.up().up(),
                l_PlayerPos.north(),
                l_PlayerPos.south(),
                l_PlayerPos.east(),
                l_PlayerPos.west(),
                l_PlayerPos.north().up(),
                l_PlayerPos.south().up(),
                l_PlayerPos.east().up(),
                l_PlayerPos.west().up(),
                };

        for (BlockPos l_Pos : l_TrapPositions)
        {
            IBlockState l_State = mc.world.getBlockState(l_Pos);

            if (l_State.getBlock() != Blocks.OBSIDIAN && mc.world.getBlockState(l_Pos).getBlock() != Blocks.BEDROCK)
                return false;
        }

        return true;
    }

    public static boolean isBlockAbovePlayerHead(EntityPlayer who, boolean tall)
    {
        BlockPos l_PlayerPos = new BlockPos(Math.floor(who.posX), Math.floor(who.posY), Math.floor(who.posZ));

        final BlockPos posOne = l_PlayerPos.up().up();
        final BlockPos posTwo = l_PlayerPos.up().up().up();

        IBlockState stateOne = mc.world.getBlockState(posOne);
        IBlockState stateTwo = mc.world.getBlockState(posTwo);

        Block blockOne = stateOne.getBlock();
        Block blockTwo = stateTwo.getBlock();

        return blockOne != Blocks.AIR && stateOne.isFullBlock() || tall && blockTwo != Blocks.AIR && stateTwo.isFullBlock();
    }

    public static void switchToItem(Item item) {
        if (mc.player.getHeldItemMainhand().getItem() != item) {
            for (int i = 0; i < 9; ++i) {
                ItemStack stack = mc.player.inventory.getStackInSlot(i);
                if (!stack.isEmpty()) {
                    if (stack.getItem() == item) {
                        mc.player.inventory.currentItem = i;
                        mc.playerController.updateController();
                        break;
                    }
                }
            }
        }
    }

    public enum FacingDirection
    {
        North,
        South,
        East,
        West,
    }

    public static FacingDirection GetFacing()
    {
        switch (MathHelper.floor((double) (mc.player.rotationYaw * 8.0F / 360.0F) + 0.5D) & 7)
        {
            case 0:
            case 1:
                return FacingDirection.South;
            case 2:
            case 3:
                return FacingDirection.West;
            case 4:
            case 5:
                return FacingDirection.North;
            case 6:
            case 7:
                return FacingDirection.East;
        }
        return FacingDirection.North;
    }

    public enum CardinalFacingDirection
    {
        North,
        NorthWest,
        NorthEast,
        South,
        SouthWest,
        SouthEast,
        East,
        West,
    }

    public static CardinalFacingDirection getCardinalFacingDirection()
    {
        switch (MathHelper.floor((double) (mc.player.rotationYaw * 8.0F / 360.0F) + 0.5D) & 7)
        {
            case 0:
                return CardinalFacingDirection.South;
            case 1:
                return CardinalFacingDirection.SouthWest;
            case 2:
                return CardinalFacingDirection.West;
            case 3:
                return CardinalFacingDirection.NorthWest;
            case 4:
                return CardinalFacingDirection.North;
            case 5:
                return CardinalFacingDirection.NorthEast;
            case 6:
                return CardinalFacingDirection.East;
            case 7:
                return CardinalFacingDirection.SouthEast;
        }
        return CardinalFacingDirection.North;
    }

    final static DecimalFormat Formatter = new DecimalFormat("#.#");

    public static float getSpeedInKM()
    {
        final double deltaX = mc.player.posX - mc.player.prevPosX;
        final double deltaZ = mc.player.posZ - mc.player.prevPosZ;

        float l_Distance = MathHelper.sqrt(deltaX * deltaX + deltaZ * deltaZ);

        double l_KMH = Math.floor(( l_Distance/1000.0f ) / ( 0.05f/3600.0f ));

        String l_Formatter = Formatter.format(l_KMH);

        if (!l_Formatter.contains("."))
            l_Formatter += ".0";

        return Float.valueOf(l_Formatter);
    }

    public static boolean isInLiquid()
    {
        if (Wrapper.GetPlayer() == null)
            return false;
        boolean inLiquid = false;
        int y = (int) ((Wrapper.GetPlayer()).getEntityBoundingBox().minY + 0.02D);
        for (int x = MathHelper.floor((Wrapper.GetPlayer()).getEntityBoundingBox().minX); x < MathHelper
                .floor((Wrapper.GetPlayer()).getEntityBoundingBox().maxX) + 1; x++)
        {
            for (int z = MathHelper.floor((Wrapper.GetPlayer()).getEntityBoundingBox().minZ); z < MathHelper
                    .floor((Wrapper.GetPlayer()).getEntityBoundingBox().maxZ) + 1; z++)
            {
                Block block = Wrapper.GetMC().world.getBlockState(new BlockPos(x, y, z)).getBlock();
                if (block != null && !(block instanceof net.minecraft.block.BlockAir))
                {
                    if (!(block instanceof net.minecraft.block.BlockLiquid))
                        return false;
                    inLiquid = true;
                }
            }
        }
        return inLiquid;
    }

    public static boolean isOnLiquid()
    {
        final float offset = 0.05f;
        final Minecraft mc = Minecraft.getMinecraft();

        if (mc.player == null)
            return false;

        if (mc.player.fallDistance >= 3.0f) {
            return false;
        }

        if (mc.player != null) {
            final AxisAlignedBB bb = mc.player.getRidingEntity() != null ? mc.player.getRidingEntity().getEntityBoundingBox().contract(0.0d, 0.0d, 0.0d).offset(0.0d, -offset, 0.0d) : mc.player.getEntityBoundingBox().contract(0.0d, 0.0d, 0.0d).offset(0.0d, -offset, 0.0d);
            boolean onLiquid = false;
            int y = (int) bb.minY;
            for (int x = MathHelper.floor(bb.minX); x < MathHelper.floor(bb.maxX + 1.0D); x++) {
                for (int z = MathHelper.floor(bb.minZ); z < MathHelper.floor(bb.maxZ + 1.0D); z++) {
                    final Block block = mc.world.getBlockState(new BlockPos(x, y, z)).getBlock();
                    if (block != Blocks.AIR) {
                        if (!(block instanceof BlockLiquid)) {
                            return false;
                        }
                        onLiquid = true;
                    }
                }
            }
            return onLiquid;
        }

        return false;
    }

    public static boolean isWearingUseableElytra()
    {
        ItemStack itemstack = mc.player.getItemStackFromSlot(EntityEquipmentSlot.CHEST);

        if (itemstack.getItem() == Items.ELYTRA && ItemElytra.isUsable(itemstack))
            return true;

        return false;
    }

    public static int GetItemSlotInHotbar(Block web)
    {
        for (int l_I = 0; l_I < 9; ++l_I)
        {
            ItemStack l_Stack = mc.player.inventory.getStackInSlot(l_I);

            if (l_Stack != ItemStack.EMPTY)
            {
                if (l_Stack.getItem() instanceof ItemBlock)
                {
                    ItemBlock block = (ItemBlock) l_Stack.getItem();

                    if (block.getBlock().equals(web))
                        return l_I;
                }
            }
        }

        return -1;
    }

    public static void sendMovementPackets(EventPlayerMotionUpdate event)
    {
        final Minecraft mc = Wrapper.GetMC();

        boolean flag = mc.player.isSprinting();

        if (flag != mc.player.serverSprintState)
        {
            if (flag)
            {
                mc.player.connection.sendPacket(new CPacketEntityAction(mc.player, CPacketEntityAction.Action.START_SPRINTING));
            }
            else
            {
                mc.player.connection.sendPacket(new CPacketEntityAction(mc.player, CPacketEntityAction.Action.STOP_SPRINTING));
            }

            mc.player.serverSprintState = flag;
        }

        boolean flag1 = mc.player.isSneaking();

        if (flag1 != mc.player.serverSneakState)
        {
            if (flag1)
            {
                mc.player.connection.sendPacket(new CPacketEntityAction(mc.player, CPacketEntityAction.Action.START_SNEAKING));
            }
            else
            {
                mc.player.connection.sendPacket(new CPacketEntityAction(mc.player, CPacketEntityAction.Action.STOP_SNEAKING));
            }

            mc.player.serverSneakState = flag1;
        }

        if (mc.getRenderViewEntity() == mc.player)
        {
            double d0 = mc.player.posX - mc.player.lastReportedPosX;
            double d1 = event.getY() - mc.player.lastReportedPosY;
            double d2 = mc.player.posZ - mc.player.lastReportedPosZ;
            double d3 = (double)(event.getYaw() - mc.player.lastReportedYaw);
            double d4 = (double)(event.getPitch() - mc.player.lastReportedPitch);
            ++mc.player.positionUpdateTicks;
            boolean flag2 = d0 * d0 + d1 * d1 + d2 * d2 > 9.0E-4D || mc.player.positionUpdateTicks >= 20;
            boolean flag3 = d3 != 0.0D || d4 != 0.0D;

            if (mc.player.isRiding())
            {
                mc.player.connection.sendPacket(new CPacketPlayer.PositionRotation(mc.player.motionX, -999.0D, mc.player.motionZ, event.getYaw(), event.getPitch(), mc.player.onGround));
                flag2 = false;
            }
            else if (flag2 && flag3)
            {
                mc.player.connection.sendPacket(new CPacketPlayer.PositionRotation(mc.player.posX, event.getY(), mc.player.posZ, event.getYaw(), event.getPitch(), mc.player.onGround));
            }
            else if (flag2)
            {
                mc.player.connection.sendPacket(new CPacketPlayer.Position(mc.player.posX, event.getY(), mc.player.posZ, mc.player.onGround));
            }
            else if (flag3)
            {
                mc.player.connection.sendPacket(new CPacketPlayer.Rotation(event.getYaw(), event.getPitch(), mc.player.onGround));
            }
            else if (mc.player.prevOnGround != mc.player.onGround)
            {
                mc.player.connection.sendPacket(new CPacketPlayer(mc.player.onGround));
            }

            if (flag2)
            {
                mc.player.lastReportedPosX = mc.player.posX;
                mc.player.lastReportedPosY = event.getY();
                mc.player.lastReportedPosZ = mc.player.posZ;
                mc.player.positionUpdateTicks = 0;
            }

            if (flag3)
            {
                mc.player.lastReportedYaw = event.getYaw();
                mc.player.lastReportedPitch = event.getPitch();
            }

            mc.player.prevOnGround = mc.player.onGround;
            mc.player.autoJumpEnabled = mc.player.mc.gameSettings.autoJump;
        }
    }

}
