package com.salhack.summit.gui.hud.items;

import java.text.DecimalFormat;

import com.mojang.realmsclient.gui.ChatFormatting;

import com.salhack.summit.gui.hud.components.OptionalListHudComponent;
import com.salhack.summit.main.SummitStatic;
import net.minecraft.util.math.MathHelper;

public class SpeedComponent extends OptionalListHudComponent
{
    final DecimalFormat Formatter = new DecimalFormat("#.#");
    
    public SpeedComponent()
    {
        super("Speed", 2, 80, 50, 9);
        setEnabled(true);
    }
    
    private float[] _speeds = new float[30];
    private double _prevTickX;
    private double _prevTickZ;

    private float getAverage()
    {
        float average = 0f;
        
        for (float s : _speeds)
            average += s;
        
        average /= _speeds.length;
        
        return average;
    }
    
    private String generateName()
    {
        String l_Formatter = Formatter.format(getAverage());
        
        if (!l_Formatter.contains("."))
            l_Formatter += ".0";
        
        return new StringBuilder("Speed ").append(ChatFormatting.WHITE).append(l_Formatter).append("km/h").toString();
    }
    
    @Override
    public void onUpdate()
    {
        final double deltaX = mc.player.posX - _prevTickX;
        final double deltaZ = mc.player.posZ - _prevTickZ;
        
        float l_Distance = MathHelper.sqrt(deltaX * deltaX + deltaZ * deltaZ);
        
        float hour = 3600f * SummitStatic.TIMER.getCurrentSpeed();
        
        double l_KMH = Math.floor(( l_Distance/1000.0f ) / ( 0.05f / hour ));
        
        for (int i = 0; i < _speeds.length - 1; ++i)
        {
            _speeds[i] = _speeds[i+1];
        }
        
        _speeds[_speeds.length - 1] = (float) l_KMH;
        
        _prevTickX = mc.player.posX;
        _prevTickZ = mc.player.posZ;
        
        cornerItem.setName(generateName());
    }
}
