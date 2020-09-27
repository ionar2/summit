package com.salhack.summit.communication;

import java.util.function.Consumer;

import com.salhack.summit.main.SummitStatic;

public class ServerHandlers
{
    private Client _client;
    
    public ServerHandlers(Client client)
    {
        _client = client;
    }

    Consumer<Packet> OnStartDupe = packet ->
    {
        SummitStatic.DUPEBOT.OnStartDupe();
    };

    Consumer<Packet> OnStopDupe = packet ->
    {
        SummitStatic.DUPEBOT.OnStopDupe();
    };
    
    Consumer<Packet> OnRideDonkey = packet ->
    {
        SummitStatic.DUPEBOT.OnRideDonkey();
    };
    
    Consumer<Packet> OnEndOfRoad = packet ->
    {
        SummitStatic.DUPEBOT.OnEndOfRoad();
    };
    
    Consumer<Packet> OnChunksLoaded = packet ->
    {
        SummitStatic.DUPEBOT.OnChunksLoad();
    };
    
    Consumer<Packet> OnRemountDesync = packet ->
    {
        SummitStatic.DUPEBOT.OnRemountDesync();
    };
    
    Consumer<Packet> OnShutdown = packet ->
    {
        SummitStatic.DUPEBOT.OnShutdown();
    };
    
    Consumer<Packet> OnPong = packet ->
    {
        _client.SendOpcodeSafe(new Packet(ClientOpcodes.CMSG_PING));
    };
    
    Consumer<Packet> OnReady = packet ->
    {
        SummitStatic.DUPEBOT.OnReady(packet);
    };
    
    Consumer<Packet> OnDismountedDonkey = packet ->
    {
        SummitStatic.DUPEBOT.OnDismountedDonkey();
    };
    
    Consumer<Packet> OnTestOpcode = packet ->
    {
    };
    
    Consumer<Packet> OnInvalidOpcode = packet ->
    {
        System.out.println("Recieved invalid opcode...");
    };
}
