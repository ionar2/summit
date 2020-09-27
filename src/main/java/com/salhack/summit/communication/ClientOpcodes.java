package com.salhack.summit.communication;

public enum ClientOpcodes
{
    CMSG_RIDE_DONKEY      (0x0001),
    CMSG_END_OF_ROAD      (0x0002),
    CMSG_CHUNKS_LOADED    (0x0003),
    CMSG_REMOUNT_DESYNC   (0x0004),
    CMSG_CLOSE_SOCKET     (0x0005),
    CMSG_PING             (0x0006),
    CMSG_DESYNCER         (0x0007),
    CMSG_OTHER_ACC        (0x0008),
    CMSG_DISMOUNTED_DONKEY(0x0009),
    CMSG_TEST_OPCODE      (0x000A),
    INVALID_OPCODE(0xBADD);
    

    int _internal;
    
    ClientOpcodes(int i)
    {
        _internal = i;
    }

    public int GetID(){return _internal;}
    public boolean IsEmpty(){return this.equals(ClientOpcodes.INVALID_OPCODE);}
    public boolean Compare(int i){return _internal == i;}
    public static ClientOpcodes GetValue(int val)
    {
        ClientOpcodes[] As = ClientOpcodes.values();
        for(int i = 0; i < As.length; i++)
        {
            if(As[i].Compare(val))
                return As[i];
        }
        return ClientOpcodes.INVALID_OPCODE;
    }            
}
