package com.salhack.summit.communication;

import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

public class Packet
{
    private int _opcodeVal;
    private Type _type;
    private byte[] _data;
    private int _offset;
    
    public Packet(int opcodeVal, Type type)
    {
        _opcodeVal = opcodeVal;
        _type = type;
        
        _offset = 0;
    }
    
    public Packet(ClientOpcodes opcode)
    {
        this(opcode.GetID(), Type.Client);
    }
    
    public Packet(ServerOpcodes opcode)
    {
        this(opcode.GetID(), Type.Server);
    }
    
    public int GetOpcode()
    {
        return _opcodeVal;
    }
    
    public Type GetType()
    {
        return _type;
    }
    
    public void Write(DataOutputStream outputStream) throws IOException
    {
        int byteLength = _data != null ? _data.length : 0;
        
        outputStream.writeInt(GetOpcode());
        outputStream.writeInt(byteLength); // must allocate the 4 bytes here
        
        // write the rest of the data
        if (byteLength > 0)
            outputStream.write(_data);
    }
    
    public void InitBytes(byte[] data) throws IOException
    {
        _data = data;
    }
    
    public int ReadInt32()
    {
        ByteBuffer wrapped = ByteBuffer.wrap(_data, _offset, 4);
        _offset += 4;
        return wrapped.getInt();
    }
    
    public String ReadString()
    {
        int size = ReadInt32();
        
        ByteBuffer wrapped = ByteBuffer.wrap(_data, _offset, size);
        
        byte[] byteString = new byte[size];
        wrapped.get(byteString);
        
        _offset += size;
        
        return new String(byteString, StandardCharsets.UTF_8);
    }
    
    public void WriteNewBytes(byte[] append)
    {
        byte[] oldData = _data;
        
        _data = new byte[_offset + append.length];
        
        if (oldData != null)
        {
            for (int i = 0; i < oldData.length; ++i)
                _data[i] = oldData[i];
        }
        
        for (int i = 0; i < append.length; ++i)
            _data[i + _offset] = append[i];
    }

    public int WriteInt32(int result)
    {
        ByteBuffer dbuf = ByteBuffer.allocate(4);
        dbuf.putInt(result);
        WriteNewBytes(dbuf.array());
        _offset += 4;
        return result;
    }

    public void WriteString(String string)
    {
        int len = string.length();

        ByteBuffer dbuf = ByteBuffer.allocate(len);
        dbuf.put(string.getBytes());
        
        byte[] stringByteArray = dbuf.array();
        
        // write size first
        WriteInt32(stringByteArray.length);
        WriteNewBytes(dbuf.array());
        _offset += stringByteArray.length;
    }
    
    public enum Type
    {
        Client,
        Server,
    }
}
