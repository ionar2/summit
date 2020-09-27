package com.salhack.summit.module.misc;

import java.util.ArrayList;
import java.util.Random;

import com.salhack.summit.managers.FriendManager;
import com.salhack.summit.events.MinecraftEvent.Stage;
import com.salhack.summit.events.bus.EventHandler;
import com.salhack.summit.events.bus.Listener;
import com.salhack.summit.events.network.EventClientPacket;
import com.salhack.summit.events.player.EventPlayerJoin;
import com.salhack.summit.events.player.EventPlayerLeave;
import com.salhack.summit.events.player.EventPlayerUpdate;
import com.salhack.summit.module.Module;
import com.salhack.summit.module.Value;
import com.salhack.summit.util.Timer;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.CPacketPlayerDigging;
import net.minecraft.network.play.client.CPacketPlayerTryUseItemOnBlock;
import net.minecraft.util.math.BlockPos;

public class Announcer extends Module
{
    public final Value<Boolean> Join = new Value<Boolean>("Join", new String[] {}, "Notifies when a player joins", true);
    public final Value<Boolean> Leave = new Value<Boolean>("Leave", new String[] {}, "Notifies when a player leaves", true);
    public final Value<Boolean> Place = new Value<Boolean>("Place", new String[] {}, "Notifies when you places", true);
    public final Value<Boolean> Break = new Value<Boolean>("Break", new String[] {}, "Notifies when you breaks a block", true);
    public final Value<Boolean> Food = new Value<Boolean>("Food", new String[] {}, "Notifies when you eat food", true);
    public final Value<Boolean> WorldTime = new Value<Boolean>("WorldTime", new String[] {}, "Notifies when the time changes", true);
    public final Value<Boolean> ClientSideOnly = new Value<Boolean>("ClientSideOnly", new String[] {}, "Only display clientside", true);
    
    public Announcer()
    {
        super("Announcer", new String[] {"Announcer", "Announcer", "Anounce", "Greeter", "Greet"}, "Announces when you do a task", "NONE", -1, ModuleType.MISC);
    }
    
    private String lastMsg = null;
    private ArrayList<String> stringsToChose = new ArrayList<>();
    private Timer timer = new Timer();
    private Random rand = new Random();
    
    private final String[] leaveMsgs = {"See you later, ", "Catch ya later, ", "See you next time, ", "Farewell, ", "Bye, ", "Good bye, ", "Later, " };
    private final String[] joinMsgs = { "Good to see you, ", "Greetings, ", "Hello, ", "Howdy, ", "Hey, ", "Good evening, ", "Welcome to SERVERIP1D5A9E, " };
    
    private final String[] morningMsgs = { "Good morning!", "Top of the morning to you!", "Good day!", "You survived another night!", "Good morning everyone!", "The sun is rising in the east, hurrah, hurrah!" };
    private final String[] noonMsgs = { "Let's go tanning!", "Let's go to the beach!", "Grab your sunglasses!", "Enjoy the sun outside! It is currently very bright!", "It's the brightest time of the day!" };
    private final String[] afternoonMsgs = { "Good afternoon!", "Let's grab lunch!", "Lunch time, kids!", "Good afternoon everyone!", "IT'S HIGH NOON!" };
    private final String[] dinnerMsgs = { "Happy hour!", "Let's get crunk!", "Enjoy the sunset everyone!" };
    private final String[] nightMsgs = { "Let's get comfy!", "Netflix and chill!", "You survived another day!", "Time to go to bed kids!" };
    private final String[] sunsetMsgs = { "Sunset has now ended! You may eat your lunch now if you are a muslim." };
    private final String[] midNight = { "It's so dark outside...", "It's the opposite of noon!" };
    private final String[] daylightMsgs = { "Good bye, zombies!", "Monsters are now burning!", "Burn baby, burn!" };

    // for breaking blocks
    private String lastBlockBroken = null;
    private int blocksBroken = 0;
    private ArrayList<BlockPos> blocksBrokenAtPos = new ArrayList<BlockPos>();
    
    // for placing
    private String lastBlockPlaced = null;
    private int blocksPlaced = 0;
    
    @Override
    public void onEnable()
    {
        super.onEnable();
        stringsToChose.clear();
    }
    
    private void sendToChat(String msg)
    {
        if (ClientSideOnly.getValue())
            SendMessage(msg);
        else
            mc.player.sendChatMessage(msg);
    }
    
    @EventHandler
    private Listener<EventPlayerUpdate> onPlayerUpdate = new Listener<>(event ->
    {
        if (!timer.passed(10000))
            return;
        
        if (lastBlockBroken != null && Break.getValue())
        {
            stringsToChose.add(new StringBuilder("I just mined ").append(String.valueOf(blocksBroken)).append(" ").append(lastBlockBroken).append("!").toString());
            blocksBroken = 0;
            lastBlockBroken = null;
            blocksBrokenAtPos = null;
        }
        
        if (lastBlockPlaced != null && Place.getValue())
        {
            stringsToChose.add(new StringBuilder("I just placed ").append(String.valueOf(blocksPlaced)).append(" ").append(lastBlockPlaced).append("!").toString());
            lastBlockPlaced = null;
            blocksPlaced = 0;
        }
        
        if (!stringsToChose.isEmpty())
        {
            int index = rand.nextInt(stringsToChose.size()); // java is dumb and doesn't let me not declare it as an int?
            final String rand = stringsToChose.get(index);
            stringsToChose.remove(rand);
            timer.reset();
            sendToChat(rand);
        }
    });

    @EventHandler
    private Listener<EventClientPacket> onClientPacket = new Listener<>(event ->
    {
        if (event.getStage() != Stage.Pre)
            return;
        
        if (event.getPacket() instanceof CPacketPlayerDigging && Break.getValue())
        {
            CPacketPlayerDigging packet = (CPacketPlayerDigging) event.getPacket();
            
            String block = mc.world.getBlockState(packet.getPosition()).getBlock().getLocalizedName();
            
            if (packet.getAction() == CPacketPlayerDigging.Action.START_DESTROY_BLOCK && (lastBlockBroken == null || lastBlockBroken.equals(block)))
            {
                if (!blocksBrokenAtPos.contains(packet.getPosition()))
                {
                    lastBlockBroken = block;
                    blocksBrokenAtPos.add(packet.getPosition());
                    ++blocksBroken;
                }
            }
        }
        else if (event.getPacket() instanceof CPacketPlayerTryUseItemOnBlock && Place.getValue())
        {
            CPacketPlayerTryUseItemOnBlock packet = (CPacketPlayerTryUseItemOnBlock) event.getPacket();
            
            ItemStack stack = mc.player.getHeldItem(packet.getHand());
            
            if (stack.isEmpty())
                return;
            
            if (stack.getItem() instanceof ItemBlock)
            {
                if (lastBlockPlaced == null || stack.getDisplayName().equals(lastBlockPlaced))
                {
                    lastBlockPlaced = stack.getDisplayName();
                    ++blocksPlaced;
                }
            }
        }
    });
    
    @EventHandler
    private Listener<EventPlayerJoin> onPlayerJoin = new Listener<>(event ->
    {
        if (!timer.passed(2000))
            return;
        
        stringsToChose.clear();
        timer.reset();
        if (FriendManager.Get().IsFriend(event.getName()))
            sendToChat(new StringBuilder("My friend ").append(event.getName()).append(" just joined the server!").toString());
        else
            sendToChat(new StringBuilder(joinMsgs[rand.nextInt(joinMsgs.length - 1)].replace("SERVERIP1D5A9E", mc.getCurrentServerData() != null ? mc.getCurrentServerData().serverIP : "127.0.0.1")).append(event.getName()).toString());
    });
    
    @EventHandler
    private Listener<EventPlayerLeave> onPlayerLeave = new Listener<>(event ->
    {
        if (!timer.passed(2000))
            return;

        stringsToChose.clear();
        timer.reset();
        sendToChat(new StringBuilder(leaveMsgs[rand.nextInt(joinMsgs.length - 1)]).append(event.getName()).toString());
    });
}
