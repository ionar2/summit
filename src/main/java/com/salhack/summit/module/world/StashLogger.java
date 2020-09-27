package com.salhack.summit.module.world;

import com.salhack.summit.events.MinecraftEvent;
import com.salhack.summit.events.bus.EventHandler;
import com.salhack.summit.events.bus.Listener;
import com.salhack.summit.events.entity.EventEntityAdded;
import com.salhack.summit.events.network.EventServerPacket;
import com.salhack.summit.managers.DirectoryManager;
import com.salhack.summit.module.Module;
import com.salhack.summit.module.Value;
import net.minecraft.entity.passive.AbstractChestHorse;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.play.server.SPacketChunkData;

import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class StashLogger extends Module
{
    public final Value<Integer> ChestNumberToImportantNotify = new Value<Integer>("MaxCount", new String[]
    { "ChestNumberToImportantNotify" },
            "Number of chests to inform you there was probably unnatural gen chests (a base!)", 5, 0, 20, 1);
    public final Value<Boolean> Chests = new Value<Boolean>("Chests", new String[] {""}, "Logs chests.", true);
    public final Value<Boolean> Shulkers = new Value<Boolean>("Shulkers", new String[] {""}, "Logs Shulkers.", true);
    public final Value<Boolean> ChestedAnimals = new Value<Boolean>("Donkeys", new String[] {""}, "Logs chested animals.", true);
    public final Value<Boolean> WriteToFile = new Value<Boolean>("WriteToFile", new String[] {""}, "Writes what this finds to a file.", true);

    public StashLogger()
    {
        super("StashLogger", new String[] {"SL"}, "Logs chests, chested donkeys, etc on chunk loads", "NONE", -1, ModuleType.WORLD);
    }
    
    private String WriterName = null;

    @Override
    public void toggleNoSave()
    {
        
    }
    
    @Override
    public void onEnable()
    {
        super.onEnable();
        
        if (WriteToFile.getValue())
        {
            String server = mc.getCurrentServerData() != null ? mc.getCurrentServerData().serverIP : "singleplayer";

            server = server.replaceAll("\\.", "");
            
            if (server.contains(":"))
                server = server.substring(0, server.indexOf(":"));
            
            String name = mc.player.getName();

            String file = name + "_" + System.currentTimeMillis();
            
            try
            {
                WriterName = DirectoryManager.Get().GetCurrentDirectory() + "/Summit/StashFinder/" + file + ".txt";
            } catch (IOException e)
            {
                e.printStackTrace();
            }
            
            SendMessage("Created the file named: " + file, false);
        }
    }
    
    @Override
    public void onDisable()
    {
        super.onDisable();
    }
    
    private void SendMessage(String message, boolean save)
    {
        if (WriteToFile.getValue() && save)
        {
            try
            {
                FileWriter writer = new FileWriter(WriterName, true);
                writer.write(message + "\n");
                writer.close();
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }
        
        super.SendMessage(message);
    }

    @EventHandler
    private Listener<EventServerPacket> onServerPacket = new Listener<>(event ->
    {
        if (event.getStage() != MinecraftEvent.Stage.Pre)
            return;
        
        if (event.getPacket() instanceof SPacketChunkData)
        {
            final SPacketChunkData l_Packet = (SPacketChunkData) event.getPacket();
            
            int l_ChestsCount = 0;
            int shulkers = 0;
            
            for (NBTTagCompound l_Tag : l_Packet.getTileEntityTags())
            {
                String l_Id = l_Tag.getString("id");
                
                if (l_Id.equals("minecraft:chest") && Chests.getValue())
                    ++l_ChestsCount;
                else if (l_Id.equals("minecraft:shulker_box") && Shulkers.getValue())
                    ++shulkers;
            }
            SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
            Date date = new Date();
            if (l_ChestsCount >= ChestNumberToImportantNotify.getValue())
                SendMessage(String.format("%s chests located at chunk [%s, %s] Dimension: %s Time [%s]", l_ChestsCount, l_Packet.getChunkX()*16, l_Packet.getChunkZ()*16, GetDimensionName(), date), true);
            if (shulkers > 0)
                SendMessage(String.format("%s shulker boxes at [%s, %s] Dimension: %s", shulkers, l_Packet.getChunkX()*16, l_Packet.getChunkZ()*16, GetDimensionName(), date), true);
        }
    });
    

    @EventHandler
    private Listener<EventEntityAdded> OnEntityAdded = new Listener<>(event ->
    {
        if (event.GetEntity() instanceof AbstractChestHorse && ChestedAnimals.getValue())
        {
            AbstractChestHorse horse = (AbstractChestHorse)event.GetEntity();
            
            if (horse.hasChest())
            {
                SendMessage(String.format("%s chested animal located at [%s, %s] Dimension: %s", horse.getName(), Math.floor(horse.posX), Math.floor(horse.posZ), GetDimensionName()), true);
            }
        }
    });
    
    private String GetDimensionName()
    {
        switch (mc.player.dimension)
        {
            case -1:
                return "Nether";
            case 0:
                return "Overworld";
            case 1:
                return "End";
        }
        
        return "Aether";
    }
}
