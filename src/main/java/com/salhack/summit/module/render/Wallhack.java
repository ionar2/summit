package com.salhack.summit.module.render;

import java.util.Arrays;

import com.salhack.summit.events.blocks.EventBlockGetRenderLayer;
import com.salhack.summit.events.bus.EventHandler;
import com.salhack.summit.events.bus.Listener;
import com.salhack.summit.events.player.EventPlayerUpdate;
import com.salhack.summit.events.render.EventRenderPutColorMultiplier;
import com.salhack.summit.module.Module;
import com.salhack.summit.module.Value;
import com.salhack.summit.module.ValueListeners;
import com.salhack.summit.util.BlockListValue;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.common.ForgeModContainer;

public class Wallhack extends Module
{
    public final Value<String> Mode = new Value<>("Mode", new String[] {"M"}, "Mode to use", "Normal");
    public final Value<Float> Opacity = new Value<Float>("Opacity", new String[]
            {"O"}, "Opacity level", 128.0f, 0.0f, 255.0f, 10.0f);
    public final Value<Boolean> HandBlock = new Value<Boolean>("HandBlock", new String[]
            {"H"}, "Only display hand block", false);
    private BlockListValue blocks = new BlockListValue("Blocks", Arrays.asList("minecraft:gold_ore",
            "minecraft:iron_ore",
            "minecraft:coal_ore",
            "minecraft:lapis_ore",
            "minecraft:diamond_ore",
            "minecraft:redstone_ore",
            "minecraft:lit_redstone_ore",
            "minecraft:tnt",
            "minecraft:emerald_ore",
            "minecraft:furnace",
            "minecraft:lit_furnace",
            "minecraft:diamond_block",
            "minecraft:iron_block",
            "minecraft:gold_block",
            "minecraft:quartz_ore",
            "minecraft:beacon",
            "minecraft:mob_spawner"));
    public final Value<Boolean> SoftReload = new Value<>("SoftReload", new String[] {"SR"}, "Reloads softly", false);
    
    public Wallhack()
    {
        super("Wallhack", new String[] {"JadeVision", "Xray"}, "Allows visiblity through blocks", "NONE", -1, ModuleType.RENDER);
        setMetaData(getMetaData());
        
        Mode.addString("Normal");
        Mode.addString("Circuits");
        
        Opacity.SetListener(new ValueListeners()
        {
            @Override
            public void OnValueChange(Value p_Val)
            {
                reloadWorld();
            }
        });
    }
    
    private Block _block;
    
    @Override
    public void toggleNoSave()
    {
        
    }
    
    public String getMetaData()
    {
        return String.valueOf(Mode.getValue());
    }

    @Override
    public void onEnable()
    {
        super.onEnable();
        mc.renderChunksMany = false;
        reloadWorld();
        ForgeModContainer.forgeLightPipelineEnabled = false;
        
        if (HandBlock.getValue())
        {
            ItemStack stack = mc.player.getHeldItemMainhand();
            
            if (stack.getItem() instanceof ItemBlock)
            {
                ItemBlock item = (ItemBlock) stack.getItem();
                
                _block = item.getBlock();
                SendMessage("Only displaying " + stack.getDisplayName());
            }
        }
    }

    @Override
    public void onDisable()
    {
        super.onDisable();
        mc.renderChunksMany = false;
        reloadWorld();
        ForgeModContainer.forgeLightPipelineEnabled = true;
    }

    private void reloadWorld()
    {
        if (mc.world == null || mc.renderGlobal == null)
            return;
        
        if (SoftReload.getValue())
        {
            mc.addScheduledTask(() -> 
            {
                int x = (int) mc.player.posX;
                int y = (int) mc.player.posY;
                int z = (int) mc.player.posZ;
                
                int distance = mc.gameSettings.renderDistanceChunks * 16;
                
                mc.renderGlobal.markBlockRangeForRenderUpdate(x - distance, y - distance, z - distance, x + distance, y + distance, z + distance);
            });
        }
        else
            mc.renderGlobal.loadRenderers();
    }
    
    private boolean containsBlock(Block block)
    {
        if (HandBlock.getValue() && _block != null)
            return block == _block;
            
        if (Mode.getValue().equals("Normal") && block != null)
        {
            return blocks.containsBlock(block);
        }

        return  block == Blocks.HEAVY_WEIGHTED_PRESSURE_PLATE ||
                block == Blocks.LIGHT_WEIGHTED_PRESSURE_PLATE ||
                block == Blocks.STONE_PRESSURE_PLATE          ||
                block == Blocks.WOODEN_PRESSURE_PLATE         ||
                block == Blocks.STONE_BUTTON                  ||
                block == Blocks.WOODEN_BUTTON                 ||
                block == Blocks.LEVER                         ||
                block == Blocks.COMMAND_BLOCK                 ||
                block == Blocks.CHAIN_COMMAND_BLOCK           ||
                block == Blocks.REPEATING_COMMAND_BLOCK       ||
                block == Blocks.DAYLIGHT_DETECTOR             ||
                block == Blocks.DAYLIGHT_DETECTOR_INVERTED    ||
                block == Blocks.DISPENSER                     ||
                block == Blocks.DROPPER                       ||
                block == Blocks.HOPPER                        ||
                block == Blocks.OBSERVER                      ||
                block == Blocks.TRAPDOOR                      ||
                block == Blocks.IRON_TRAPDOOR                 ||
                block == Blocks.REDSTONE_BLOCK                ||
                block == Blocks.REDSTONE_LAMP                 ||
                block == Blocks.REDSTONE_TORCH                ||
                block == Blocks.UNLIT_REDSTONE_TORCH          ||
                block == Blocks.REDSTONE_WIRE                 ||
                block == Blocks.POWERED_REPEATER              ||
                block == Blocks.UNPOWERED_REPEATER            ||
                block == Blocks.POWERED_COMPARATOR            ||
                block == Blocks.UNPOWERED_COMPARATOR          ||
                block == Blocks.LIT_REDSTONE_LAMP             ||
                block == Blocks.REDSTONE_ORE                  ||
                block == Blocks.LIT_REDSTONE_ORE              ||
                block == Blocks.ACACIA_DOOR                   ||
                block == Blocks.DARK_OAK_DOOR                 ||
                block == Blocks.BIRCH_DOOR                    ||
                block == Blocks.JUNGLE_DOOR                   ||
                block == Blocks.OAK_DOOR                      ||
                block == Blocks.SPRUCE_DOOR                   ||
                block == Blocks.DARK_OAK_DOOR                 ||
                block == Blocks.IRON_DOOR                     ||
                block == Blocks.OAK_FENCE                     ||
                block == Blocks.SPRUCE_FENCE                  ||
                block == Blocks.BIRCH_FENCE                   ||
                block == Blocks.JUNGLE_FENCE                  ||
                block == Blocks.DARK_OAK_FENCE                ||
                block == Blocks.ACACIA_FENCE                  ||
                block == Blocks.OAK_FENCE_GATE                ||
                block == Blocks.SPRUCE_FENCE_GATE             ||
                block == Blocks.BIRCH_FENCE_GATE              ||
                block == Blocks.JUNGLE_FENCE_GATE             ||
                block == Blocks.DARK_OAK_FENCE_GATE           ||
                block == Blocks.ACACIA_FENCE_GATE             ||
                block == Blocks.JUKEBOX                       ||
                block == Blocks.NOTEBLOCK                     ||
                block == Blocks.PISTON                        ||
                block == Blocks.PISTON_EXTENSION              ||
                block == Blocks.PISTON_HEAD                   ||
                block == Blocks.STICKY_PISTON                 ||
                block == Blocks.TNT                           ||
                block == Blocks.SLIME_BLOCK                   ||
                block == Blocks.TRIPWIRE                      ||
                block == Blocks.TRIPWIRE_HOOK                 ||
                block == Blocks.RAIL                          ||
                block == Blocks.ACTIVATOR_RAIL                ||
                block == Blocks.DETECTOR_RAIL                 ||
                block == Blocks.GOLDEN_RAIL;
    }

    @EventHandler
    private Listener<EventPlayerUpdate> onPlayerUpdate  = new Listener<>(event ->
    {
        setMetaData(getMetaData());
    });
    
    @EventHandler
    private Listener<EventBlockGetRenderLayer> OnGetRenderLayer  = new Listener<>(event ->
    {
        if (!containsBlock(event.getBlock()))
        {
            event.cancel();
            event.setLayer(BlockRenderLayer.TRANSLUCENT);
        }
    });
    
    @EventHandler
    private Listener<EventRenderPutColorMultiplier> OnPutColorMultiplier = new Listener<>(event ->
    {
        event.cancel();
        event.setOpacity(Opacity.getValue() / 0xFF);
    });

    public void processShouldSideBeRendered(Block block, IBlockState blockState, IBlockAccess blockAccess, BlockPos pos,
            EnumFacing side, CallbackInfoReturnable<Boolean> callback)
    {
        if (containsBlock(block))
            callback.setReturnValue(true);
    }

    public void processGetLightValue(Block block, CallbackInfoReturnable<Integer> callback)
    {
        if (containsBlock(block))
        {
            callback.setReturnValue(1);
        }
    }
}
