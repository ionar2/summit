package com.salhack.summit.communication;

import java.net.*;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.Consumer;
import java.io.*;

public class Client
{
    // initialize socket and input output streams
    private Socket socket = null;
    private DataInputStream input = null;
    private DataOutputStream out = null;
    private ConcurrentLinkedQueue<Packet> _packetQueue = new ConcurrentLinkedQueue<>();
    private ByteBuffer prevBuffer = null;
    
    // constructor to put ip address and port
    public Client(String address, int port)
    {
        // establish a connection
        try
        {
            socket = new Socket(address, port);
            System.out.println("Connected");

            input = new DataInputStream(socket.getInputStream());
            out = new DataOutputStream(socket.getOutputStream());
        } catch (UnknownHostException u)
        {
            System.out.println(u);
        } catch (IOException i)
        {
            System.out.println(i);
        }
        
        ServerHandlers serverHandlers = new ServerHandlers(this);
        
        handlers.put(ServerOpcodes.SMSG_START_DUPE, serverHandlers.OnStartDupe);
        handlers.put(ServerOpcodes.SMSG_STOP_DUPE, serverHandlers.OnStopDupe);
        handlers.put(ServerOpcodes.SMSG_RIDE_DONKEY, serverHandlers.OnRideDonkey);
        handlers.put(ServerOpcodes.SMSG_END_OF_ROAD, serverHandlers.OnEndOfRoad);
        handlers.put(ServerOpcodes.SMSG_CHUNKS_LOADED, serverHandlers.OnChunksLoaded);
        handlers.put(ServerOpcodes.SMSG_REMOUNT_DESYNC, serverHandlers.OnRemountDesync);
        handlers.put(ServerOpcodes.SMSG_SHUTDOWN, serverHandlers.OnShutdown);
        handlers.put(ServerOpcodes.SMSG_PONG, serverHandlers.OnPong);
        handlers.put(ServerOpcodes.SMSG_READY, serverHandlers.OnReady);
        handlers.put(ServerOpcodes.SMSG_DISMOUNTED_DONKEY, serverHandlers.OnDismountedDonkey);
        handlers.put(ServerOpcodes.SMSG_TEST_OPCODE, serverHandlers.OnTestOpcode);
        handlers.put(ServerOpcodes.SMSG_INVALID_OPCODE, serverHandlers.OnInvalidOpcode);
    }
    
    private HashMap<ServerOpcodes, Consumer<Packet>> handlers = new HashMap<ServerOpcodes, Consumer<Packet>>();

    private void HandleOpcode(Packet packet) throws IOException
    {
        ServerOpcodes opcode = ServerOpcodes.GetValue(packet.GetOpcode());
        
        if (handlers.containsKey(opcode))
        {
            handlers.get(opcode).accept(packet);
            return;
        }

        System.out.println("Recvieied unhandled opcode " + Integer.toHexString(packet.GetOpcode()));
    }
    
    public void SendOpcodeUnsafe(Packet packet) throws IOException
    {
        packet.Write(out);
        
        out.flush();
    }

    public void SendOpcodeSafe(Packet packet)
    {
        _packetQueue.add(packet);
    }
    
    public void Update()
    {
        while (true)
        {
            try
            {
                if (input != null && input.available() > 0)
                {
                    // Try to read packets
                    int totalBytes = 0;
                    
                    ArrayList<byte[]> byteArray = new ArrayList<>();
                    
                    if (prevBuffer != null)
                    {
                        byteArray.add(prevBuffer.array());
                        totalBytes += prevBuffer.array().length;
                    }
                    
                    while (input.available() > 0)
                    {
                        int bytes = input.available();
                        
                        totalBytes += bytes;
                        
                        byte[] data = new byte[bytes];
                        input.readFully(data);
                        byteArray.add(data);
                    }

                    ByteBuffer buffer = ByteBuffer.allocate(totalBytes);
                    
                    for (byte[] b : byteArray)
                        buffer.put(b);
                    
                    byte[] bufferArray = buffer.array();
                    
                    prevBuffer = buffer;
                    
                    // try to get packets and their data from the buffer
                    // hopefully there is an opcode available here...
                    // verify the length is greater or equal to 8.
                    if (bufferArray.length >= 8)
                    {
                        buffer = ByteBuffer.wrap(bufferArray);
                        
                        // check opcode
                        ServerOpcodes opcode = ServerOpcodes.GetValue(buffer.getInt(0));
                        if (opcode == ServerOpcodes.SMSG_INVALID_OPCODE)
                        {
                            System.out.println("SMSG_INVALID_OPCODE!");
                        }
                        
                        int size = buffer.getInt(4);
                        
                        int currPosition = 8;
                        
                        // create a packet object
                        Packet packet = new Packet(opcode);
                        
                        if (size > 0)
                        {
                            // check if packet is ready to be processed
                            if (bufferArray.length >= 8 + size)
                            {
                                // ok, we can process this packet.
                                // slice the bytes off and init the bytes in the packet
                                packet.InitBytes(Arrays.copyOfRange(bufferArray, currPosition, currPosition+size));
                                
                                currPosition += size;
                            }
                            else // wait for next process
                            {
                                System.out.println("Not enough data to read size is " + size + " length is " + bufferArray.length);
                                continue;
                            }
                        }
                        
                        HandleOpcode(packet);
                        
                        // slice off our last read packet.
                        bufferArray = Arrays.copyOfRange(bufferArray, currPosition, bufferArray.length);

                        if (bufferArray.length == 0)
                            prevBuffer = null;
                        else
                            prevBuffer = ByteBuffer.wrap(bufferArray);
                    }
                }
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
            
            // Send packets queued
            try
            {
                while (!_packetQueue.isEmpty())
                    SendOpcodeUnsafe(_packetQueue.poll());
            } catch (Exception e)
            {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

        /*try
        {
            SendOpcodeUnsafe(new Packet(ClientOpcodes.CMSG_CLOSE_SOCKET));
        } catch (IOException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        
        // close the connection
        try
        {
            input.close();
            out.close();
            socket.close();
        } catch (IOException i)
        {
            System.out.println(i);
        }*/
    }
}
