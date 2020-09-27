package com.salhack.summit.util;

public class BouncyValue {
    private float last = 0.0f;
    private float current = 0.0f;

    private float springLength = 0.0f;

    private final float bounceAmount;
    private final float bounceStrength;

    public BouncyValue(float bounceAmount, float bounceStrength) {
        this.bounceAmount = bounceAmount;
        this.bounceStrength = bounceStrength;
    }

    public void subtract(float f) {
        last -= f;
        current -= f;
    }

    public void subtract(double d) {
        subtract((float) d);
    }

    public void reset() {
        last = 0.0f;
        current = 0.0f;
        springLength = 0.0f;
    }

    public void update() {
        last = current;
        springLength *= bounceAmount;
        springLength -= current * bounceStrength;
        current += springLength;
    }

    public float get(float f) {
        return last + (current - last) * f;
    }
}
