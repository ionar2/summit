package com.salhack.summit.communication;

public enum ServerOpcodes
{
    /* Server To Client */
    SMSG_START_DUPE       (0x0001),
    SMSG_STOP_DUPE        (0x0002),
    SMSG_RIDE_DONKEY      (0x0003),
    SMSG_END_OF_ROAD      (0x0004),
    SMSG_CHUNKS_LOADED    (0x0005),
    SMSG_REMOUNT_DESYNC   (0x0006),
    SMSG_SHUTDOWN         (0x0007),
    SMSG_PONG             (0x0008),
    SMSG_READY            (0x0009),
    SMSG_DISMOUNTED_DONKEY(0x000A),
    SMSG_TEST_OPCODE      (0x000B),
    SMSG_INVALID_OPCODE   (0xBADD);
    

    int _internal;
    
    ServerOpcodes(int i)
    {
        _internal = i;
    }

    public int GetID(){return _internal;}
    public boolean IsEmpty(){return this.equals(ServerOpcodes.SMSG_INVALID_OPCODE);}
    public boolean Compare(int i){return _internal == i;}
    public static ServerOpcodes GetValue(int val)
    {
        ServerOpcodes[] As = ServerOpcodes.values();
        for(int i = 0; i < As.length; i++)
        {
            if(As[i].Compare(val))
                return As[i];
        }
        return ServerOpcodes.SMSG_INVALID_OPCODE;
    }            
}
