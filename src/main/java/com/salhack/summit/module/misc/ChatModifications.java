package com.salhack.summit.module.misc;

import com.mojang.realmsclient.gui.ChatFormatting;

import com.salhack.summit.events.MinecraftEvent;
import com.salhack.summit.events.bus.EventHandler;
import com.salhack.summit.events.bus.Listener;
import com.salhack.summit.events.network.EventServerPacket;
import com.salhack.summit.events.player.EventPlayerUpdate;
import com.salhack.summit.main.Summit;
import com.salhack.summit.module.Module;
import com.salhack.summit.module.Value;
import net.minecraft.network.play.server.SPacketChat;
import net.minecraft.util.text.ChatType;
import net.minecraft.util.text.TextComponentString;

import java.text.SimpleDateFormat;
import java.util.Date;

public final class ChatModifications extends Module {

    public final Value<String> TimeMode = new Value<>("TimeMode", new String[]
            {"TimeModes", "Time"}, "Time format, 12 hour (NA) or 24 hour (EU).", "NA");
    public final Value<Boolean> AntiEZ = new Value<>("AntiEZ", new String[]{"NoEZ"}, "Prevents EZ from being rendered in chat, very useful for 2b2tpvp", true);
    public final Value<Boolean> NoDiscord = new Value<>("NoDiscord", new String[]{"NoEZ"}, "Prevents discord from being rendered in chat", true);
    public final Value<String> NameHighlight = new Value<>("NameHighlight", new String[]{"Highlight"}, "Highlights your name in the selected Color.", "None");
    public final Value<String> OtherPlayers = new Value<>("OtherPlayers", new String[]{"OtherPlayers"}, "Choose the color other player names shows up in.", "None");
    public final Value<Boolean> BetterFormat = new Value<>("BetterFormat", new String[]{"BetterFormat"}, "Removes Carrots in Chat.", true);
    public final Value<Integer> ChatLength = new Value<>("ChatLength", new String[]{"ChatLength"}, "ChatLength number for more chat length", 100, 0, 0xFFFFFF, 1000);

    public ChatModifications() {
        super("ChatMods", new String[]
                {"ChatStamp", "ChatStamps"}, "Allows for chat modifications", "NONE", 0xDB2450, ModuleType.MISC);
        setMetaData(getMetaData());
        
        TimeMode.addString("NA");
        TimeMode.addString("EU");
        TimeMode.addString("NONE");

        NameHighlight.addString("None");
        NameHighlight.addString("Gold");
        NameHighlight.addString("DarkRed");
        NameHighlight.addString("Red");
        NameHighlight.addString("DarkGreen");
        NameHighlight.addString("Green");
        NameHighlight.addString("DarkBlue");
        NameHighlight.addString("Blue");

        OtherPlayers.addString("None");
        OtherPlayers.addString("Gold");
        OtherPlayers.addString("DarkRed");
        OtherPlayers.addString("Red");
        OtherPlayers.addString("DarkGreen");
        OtherPlayers.addString("Green");
        OtherPlayers.addString("DarkBlue");
        OtherPlayers.addString("Blue");
        OtherPlayers.addString("DarkGray");
        OtherPlayers.addString("Gray");
        OtherPlayers.addString("White");
    }

    ChatFormatting nameHighlight = null;
    ChatFormatting otherPlayers = null;

    public String getMetaData()
    {
        return this.TimeMode.getValue();
    }
    
    @EventHandler
    private final Listener<EventPlayerUpdate> onPlayerUpdate  = new Listener<>(event -> setMetaData(getMetaData()));

    @EventHandler
    private final Listener<EventServerPacket> onServerPacket = new Listener<>(event -> {
        if (event.getStage() != MinecraftEvent.Stage.Pre)
            return;

        if (event.getPacket() instanceof SPacketChat) {
            final SPacketChat packet = (SPacketChat) event.getPacket();

            if (packet.getChatComponent() instanceof TextComponentString && packet.getType() != ChatType.GAME_INFO) {
                final TextComponentString component = (TextComponentString) packet.getChatComponent();

                String date = "";

                switch (this.TimeMode.getValue()) {
                    case "NA":
                        date = new SimpleDateFormat("h:mm a").format(new Date());
                        break;
                    case "EU":
                        date = new SimpleDateFormat("k:mm").format(new Date());
                        break;
                    case "NONE":
                        break;
                }

                if (!TimeMode.getValue().equals("None")) {
                    component.text = "\2477[" + date + "]\247r " + component.getText();
                }

                if (component.getFormattedText().contains("> ")) {
                    String l_Text = component.getFormattedText().substring(component.getFormattedText().indexOf("> "));

                    if (l_Text.toLowerCase().contains("ez") && AntiEZ.getValue()) event.cancel();

                    if (NoDiscord.getValue() && l_Text.toLowerCase().contains("discord")) event.cancel();

                    if (event.isCancelled()) return;
                }

                String l_Text = component.getFormattedText();

                switch (NameHighlight.getValue()) {
                    case "None": nameHighlight = ChatFormatting.RESET;
                        break;
                    case "Gold": nameHighlight = ChatFormatting.GOLD;
                        break;
                    case "DarkRed": nameHighlight = ChatFormatting.DARK_RED;
                        break;
                    case "Red": nameHighlight = ChatFormatting.RED;
                        break;
                    case "DarkGreen": nameHighlight = ChatFormatting.DARK_GREEN;
                        break;
                    case "Green": nameHighlight = ChatFormatting.GREEN;
                        break;
                    case "DarkBlue": nameHighlight = ChatFormatting.DARK_BLUE;
                        break;
                    case "Blue": nameHighlight = ChatFormatting.BLUE;
                        break;
                }

                switch (OtherPlayers.getValue()) {
                    case "None": otherPlayers = ChatFormatting.RESET;
                        break;
                    case "Gold": otherPlayers = ChatFormatting.GOLD;
                        break;
                    case "DarkRed": otherPlayers = ChatFormatting.DARK_RED;
                        break;
                    case "Red": otherPlayers = ChatFormatting.RED;
                        break;
                    case "DarkGreen": otherPlayers = ChatFormatting.DARK_GREEN;
                        break;
                    case "Green": otherPlayers = ChatFormatting.GREEN;
                        break;
                    case "DarkBlue": otherPlayers = ChatFormatting.DARK_BLUE;
                        break;
                    case "Blue": otherPlayers = ChatFormatting.BLUE;
                        break;
                    case "DarkGray": otherPlayers = ChatFormatting.DARK_GRAY;
                        break;
                    case "Gray": otherPlayers = ChatFormatting.GRAY;
                        break;
                    case "White": otherPlayers = ChatFormatting.WHITE;
                        break;
                }

                if (nameHighlight != null) {
                    l_Text = l_Text.replaceAll("(?i)" + mc.player.getName(), nameHighlight + mc.player.getName() + ChatFormatting.RESET);
                }

                if (!(l_Text.contains("to") && l_Text.contains("whisper") && l_Text.contains(":"))) {
                    if (BetterFormat.getValue()) {
                        if (otherPlayers != null) {
                            l_Text = l_Text.replaceFirst("<", "" + otherPlayers);
                        }
                        l_Text = l_Text.replaceFirst(">", ChatFormatting.GRAY + ":" + ChatFormatting.RESET);
                    }
                    event.cancel();
                    Summit.SendMessage(l_Text);
                }
            }
        }
    });
}
