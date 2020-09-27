package com.salhack.summit.module.bot;

import com.salhack.summit.communication.ClientOpcodes;
import com.salhack.summit.communication.Packet;
import com.salhack.summit.main.Summit;
import com.salhack.summit.main.SummitStatic;
import com.salhack.summit.events.bus.EventHandler;
import com.salhack.summit.events.bus.Listener;
import com.salhack.summit.events.entity.EventEntityAdded;
import com.salhack.summit.events.player.EventPlayerStartRiding;
import com.salhack.summit.events.player.EventPlayerUpdate;
import com.salhack.summit.events.player.EventPlayerUpdateMoveState;
import com.salhack.summit.events.render.RenderEvent;
import com.salhack.summit.module.ListValue;
import com.salhack.summit.module.Module;
import com.salhack.summit.module.Value;
import com.salhack.summit.util.entity.EntityUtil;
import com.salhack.summit.util.render.RenderUtil;
import net.minecraft.client.gui.inventory.GuiScreenHorseInventory;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.passive.AbstractChestHorse;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.opengl.GL11;

import java.util.Comparator;
import java.util.Timer;
import java.util.TimerTask;

public class DupeBot extends Module
{
    public final ListValue Mode = new ListValue("Mode", "Mode to use", new String[] { "Desyncer", "OtherAcc" });
    
    public DupeBot()
    {
        super("DupeBot", new String[] {}, "9b9t dupe bot", "NONE", -1, ModuleType.BOT);
    }
    
    private boolean _started = false;
    private boolean _runToStart = false;
    private boolean _runToEnd = false;
    private boolean _inDesync = false;
    private boolean _needDismount = false;
    private boolean _tryToRemount = false;
    private boolean _sentRemountDesync = false;
    private Vec3d _startPosition = Vec3d.ZERO;
    private Vec3d _endPosition = Vec3d.ZERO;
    private Timer _timer = new Timer();
    private float _startYaw = 0.0f;
    private boolean _sentInventoryPacket = false;
    
    
    public void toggleNoSave()
    {
        
    }
    
    @Override
    public void onEnable()
    {
        super.onEnable();
        
        SendMessage("Initalizing socket for " + String.valueOf(Mode.getValue()));
        
        if (Summit.GetClient() == null)
        {
            SendMessage("Client is null, server is not reachable");
            return;
        }
        
        Packet packet = new Packet(Mode.getValue().equals("Desyncer") ? ClientOpcodes.CMSG_DESYNCER : ClientOpcodes.CMSG_OTHER_ACC);
        
        packet.WriteInt32((int)mc.player.posX);
        packet.WriteInt32((int)mc.player.posY);
        packet.WriteInt32((int)mc.player.posZ);
        
        Summit.GetClient().SendOpcodeSafe(packet);
        
        SendMessage("Sent the packet " + String.valueOf(Mode.getValue().equals("Desyncer") ? ClientOpcodes.CMSG_DESYNCER : ClientOpcodes.CMSG_OTHER_ACC));
        
        _started = false;
        _runToStart = false;
        _runToEnd = false;
        _startPosition = Vec3d.ZERO;
        _endPosition = Vec3d.ZERO;
        _startYaw = mc.player.rotationYaw;
        _needDismount = false;
        _tryToRemount = false;
        
        if (_timer != null)
        {
            _timer.cancel();
            _timer = new Timer();
        }
    }
    
    /****************************************************************
     * Packets received
     * @param input 
     ****************************************************************/

    // called once at startup
    public void OnReady(Packet packet)
    {
        _started = true;
        
        switch (Mode.getValue())
        {
            case "Desyncer":
                _endPosition = new Vec3d(packet.ReadInt32(), packet.ReadInt32(), packet.ReadInt32());
                SendMessage("_endPosition: " + _endPosition.toString());
                
                if (_endPosition.x >= -10 && _endPosition.x <= 10)
                {
                    _endPosition = Vec3d.ZERO;
                }
                
                break;
            case "OtherAcc":
                _endPosition = mc.player.getPositionVector();
                _startPosition = new Vec3d(packet.ReadInt32(), packet.ReadInt32(), packet.ReadInt32());
                Summit.GetClient().SendOpcodeSafe(new Packet(ClientOpcodes.CMSG_END_OF_ROAD));
                SendMessage("startPosition: " + _startPosition.toString());
                break;
            default:
                break;
        }
    }

    public void OnRideDonkey()
    {
        switch (Mode.getValue())
        {
            case "Desyncer":
                _sentRemountDesync = false;
                _sentInventoryPacket = false;
                
                if (SummitStatic.ENTITYDESYNC.isEnabled())
                    SummitStatic.ENTITYDESYNC.toggle();
                
                // must be after to not trigger code below. we will remount after we throw items
                _inDesync = false;
                break;
            case "OtherAcc":
                break;
            default:
                break;
        }
    }

    // run entitydesync here.
    public void OnEndOfRoad()
    {
        if (Mode.getValue().equals("Desyncer"))
        {
            _inDesync = true;
            mc.player.rotationYaw = _startYaw;
            
            // pig
            if (_endPosition.equals(Vec3d.ZERO))
            {
                Packet packet = new Packet(Mode.getValue().equals("Desyncer") ? ClientOpcodes.CMSG_DESYNCER : ClientOpcodes.CMSG_OTHER_ACC);
                
                packet.WriteInt32((int)mc.player.posX);
                packet.WriteInt32((int)mc.player.posY);
                packet.WriteInt32((int)mc.player.posZ);
                
                Summit.GetClient().SendOpcodeSafe(packet);
                return;
            }
            
            if (!SummitStatic.ENTITYDESYNC.isEnabled())
            {
                SummitStatic.ENTITYDESYNC.HClip.setValue(false);
                SummitStatic.ENTITYDESYNC.toggle();
                mc.player.setPosition(_endPosition.x, _endPosition.y - 20, _endPosition.z);
            }
        }
    }
    
    // go to start, ride donkey, on mount -> send CMSG_RIDE_DONKEY
    public void OnChunksLoad()
    {
        if (Mode.getValue().equals("OtherAcc"))
        {
            _timer.schedule(new TimerTask()
            {
                @Override
                public void run()
                {
                    _runToStart = true;
                }
            }, 5000);
        }
    }
    // // dismount here for other, main will send back CMSG_RIDE_DONKEY to restart this.
    public void OnRemountDesync()
    {
        SendMessage("RECV OnRemountDesync");
        
        if (Mode.getValue().equals("OtherAcc"))
        {
            _needDismount = true;
            SendMessage("Trying to dismount..");
        }
    }

    public void OnShutdown()
    {
        // TODO Auto-generated method stub
        
    }

    public void OnStartDupe()
    {
        // TODO Auto-generated method stub
        
    }

    public void OnStopDupe()
    {
        // TODO Auto-generated method stub
        
    }

    public void OnDismountedDonkey()
    {
        switch (Mode.getValue())
        {
            case "Desyncer":
                //_tryToRemount = true;
                break;
            case "OtherAcc":
                _runToEnd = true;
                break;
            default:
                break;
        }
    }

    /******************************************************************
     * Events
     *****************************************************************/
    @EventHandler
    private Listener<EventPlayerUpdate> OnUpdate = new Listener<>(event ->
    {
        if (!_started)
            return;
        
        if (Mode.getValue().equals("Desyncer"))
        {
            mc.player.rotationYaw = _startYaw;
        }
        
        if (mc.currentScreen instanceof GuiScreenHorseInventory && Mode.getValue().equals("Desyncer"))
        {
            GuiScreenHorseInventory inv = (GuiScreenHorseInventory)mc.currentScreen;
            
            boolean sendPacket = true;
            
            for (int i = 0; i < inv.horseInventory.getSizeInventory(); ++i)
            {
                ItemStack stack = inv.horseInventory.getStackInSlot(i);

                if (!stack.isEmpty())
                {
                    //SendMessage("A stack is not empty");
                    sendPacket = false;
                    break;
                }
            }
            
            if (sendPacket && !_sentRemountDesync)
            {
                SendMessage("Sent Remound Desync");
                _sentRemountDesync = true;
                _tryToRemount = true;
                Summit.GetClient().SendOpcodeSafe(new Packet(ClientOpcodes.CMSG_REMOUNT_DESYNC));
            }
        }
        
        if (_runToStart && _startPosition != Vec3d.ZERO)
        {
            final double rotations[] =  EntityUtil.calculateLookAt(
                    _startPosition.x,
                    _startPosition.y,
                    _startPosition.z,
                    mc.player);
            
            mc.player.rotationYaw = (float)rotations[0];
            
            if (!SummitStatic.AUTOWALK.isEnabled())
                SummitStatic.AUTOWALK.toggle();
            
            if (getDistance2D(_startPosition) < 1)
            {
                AbstractChestHorse horse = mc.world.loadedEntityList.stream()
                        .filter(e -> e instanceof AbstractChestHorse && ((AbstractChestHorse)e).hasChest())
                        .map(e -> (AbstractChestHorse)e)
                        .min(Comparator.comparing(e -> mc.player.getDistance(e)))
                        .orElse(null);
                
                if (horse != null)
                {
                    if (SummitStatic.AUTOWALK.isEnabled())
                        SummitStatic.AUTOWALK.toggle();
                    
                    _runToStart = false;
                    
                    mc.playerController.interactWithEntity(mc.player, horse, EnumHand.MAIN_HAND);
                }
            }
            
            return;
        }
        
        if (_runToEnd && _endPosition != Vec3d.ZERO)
        {
            final double rotations[] =  EntityUtil.calculateLookAt(
                    _endPosition.x + 0.5,
                    _endPosition.y - 0.5,
                    _endPosition.z + 0.5,
                    mc.player);
            
            mc.player.rotationYaw = (float)rotations[0];

            if (!SummitStatic.AUTOWALK.isEnabled())
                SummitStatic.AUTOWALK.toggle();
            
            if (getDistance2D(_endPosition) < 1)
            {
                if (SummitStatic.AUTOWALK.isEnabled())
                    SummitStatic.AUTOWALK.toggle();
                
                _runToEnd = false;
                Summit.GetClient().SendOpcodeSafe(new Packet(ClientOpcodes.CMSG_END_OF_ROAD));
            }
                
            return;
        }
        
        if (_needDismount)
        {
            if (!mc.player.isRiding())
            {
                _needDismount = false;
                Summit.GetClient().SendOpcodeSafe(new Packet(ClientOpcodes.CMSG_DISMOUNTED_DONKEY));
                _runToEnd = true;
            }
        }
        
        if (_tryToRemount)
        {
            AbstractChestHorse horse = mc.world.loadedEntityList.stream()
                    .filter(e -> e instanceof AbstractChestHorse && ((AbstractChestHorse)e).hasChest() && e != mc.player.getRidingEntity())
                    .map(e -> (AbstractChestHorse)e)
                    .min(Comparator.comparing(e -> mc.player.getDistance(e)))
                    .orElse(null);
            
            if (horse != null)
                mc.playerController.interactWithEntity(mc.player, horse, EnumHand.MAIN_HAND);
        }
        
        if (Mode.getValue().equals("Desyncer"))
        {
            mc.player.rotationYaw = _startYaw;
        }
    });
    
    @EventHandler
    private Listener<RenderEvent> OnRender = new Listener<>(event ->
    {
        if (_startPosition == Vec3d.ZERO)
            return;
        
        BlockPos pos = new BlockPos(_startPosition.x, _startPosition.y, _startPosition.z);

        final AxisAlignedBB bb = new AxisAlignedBB(pos.getX() - mc.getRenderManager().viewerPosX, pos.getY() - mc.getRenderManager().viewerPosY,
                pos.getZ() - mc.getRenderManager().viewerPosZ, pos.getX() + 1 - mc.getRenderManager().viewerPosX, pos.getY() + 2 - mc.getRenderManager().viewerPosY,
                pos.getZ() + 1 - mc.getRenderManager().viewerPosZ);

        RenderUtil.camera.setPosition(mc.getRenderViewEntity().posX, mc.getRenderViewEntity().posY, mc.getRenderViewEntity().posZ);

        if (RenderUtil.camera.isBoundingBoxInFrustum(new AxisAlignedBB(bb.minX + mc.getRenderManager().viewerPosX, bb.minY + mc.getRenderManager().viewerPosY, bb.minZ + mc.getRenderManager().viewerPosZ,
                bb.maxX + mc.getRenderManager().viewerPosX, bb.maxY + mc.getRenderManager().viewerPosY, bb.maxZ + mc.getRenderManager().viewerPosZ)))
        {
            GlStateManager.pushMatrix();
            GlStateManager.enableBlend();
            GlStateManager.disableDepth();
            GlStateManager.tryBlendFuncSeparate(770, 771, 0, 1);
            GlStateManager.disableTexture2D();
            GlStateManager.depthMask(false);
            GL11.glEnable(GL11.GL_LINE_SMOOTH);
            GL11.glHint(GL11.GL_LINE_SMOOTH_HINT, GL11.GL_NICEST);
            GL11.glLineWidth(1.5f);
            
            RenderUtil.drawFilledBox(bb, 0x29FF0010);

            GL11.glDisable(GL11.GL_LINE_SMOOTH);
            GlStateManager.depthMask(true);
            GlStateManager.enableDepth();
            GlStateManager.enableTexture2D();
            GlStateManager.disableBlend();
            GlStateManager.popMatrix();
        }
    });
    
    @EventHandler
    private Listener<EventPlayerUpdateMoveState> OnMovementInput = new Listener<>(event ->
    {
        if (_needDismount)
        {
            mc.player.movementInput.sneak = true;
        }
    });

    @EventHandler
    private Listener<EventPlayerStartRiding> OnStartRiding = new Listener<>(event ->
    {
        if (!_inDesync || Mode.getValue().equals("OtherAcc"))
        {
            _tryToRemount = false;
            Summit.GetClient().SendOpcodeSafe(new Packet(ClientOpcodes.CMSG_RIDE_DONKEY));
        }

        _timer.schedule(new TimerTask()
        {
            @Override
            public void run()
            {
                if (Mode.getValue().equals("Desyncer") && !_sentInventoryPacket && !_inDesync)
                {
                    SendMessage("Sent inventory packet");
                    _sentInventoryPacket = true;
                    mc.player.sendHorseInventory();
                }
            }
        }, 2000);
    });
    
    @EventHandler
    private Listener<EventEntityAdded> OnEntityAdded = new Listener<>(p_Event ->
    {
        if (p_Event.GetEntity() instanceof EntityPlayer && p_Event.GetEntity() != mc.player && _inDesync)
            Summit.GetClient().SendOpcodeSafe(new Packet(ClientOpcodes.CMSG_CHUNKS_LOADED));
    });
    
    /******************************************************************
     * Internal functions
     *****************************************************************/
    private double getDistance2D(Vec3d pos)
    {
        double posX = Math.abs(mc.player.posX - pos.x);
        double posZ = Math.abs(mc.player.posZ - pos.z);
        
        return posX + posZ;
    }
}
