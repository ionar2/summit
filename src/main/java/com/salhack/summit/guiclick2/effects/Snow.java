package com.salhack.summit.guiclick2.effects;

import java.util.Random;

import com.salhack.summit.util.render.RenderUtil;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.util.math.MathHelper;

public class Snow
{
    private static final Random random = new Random();
    private final int x;
    private int y;
    private int fallingSpeed = random.nextInt(2) + 1;
    private float size = (float) Math.random();
    private int age = random.nextInt(100) + 1;

    public Snow(int x, int y)
    {
        this.x = x;
        this.y = y;
    }

    public void Update(ScaledResolution res)
    {
        if (y > res.getScaledHeight() + 10 || y < -10)
        {
            y = -10;

            fallingSpeed = random.nextInt(10) + 1;
            size = (float) Math.random() + 1;

            return;
        }

        age++;
        float xOffset = MathHelper.sin(age / 16.0f) * 32;
        
        float result = x + xOffset;
        
        RenderUtil.drawCircle(result, (float)y, size+1, 0xFF920707);
        RenderUtil.drawCircle(result, (float)y, size, 0xFFFFDC00);

        y += fallingSpeed;
    }
}