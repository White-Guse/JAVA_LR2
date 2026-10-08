package com.example.shapes;

import java.awt.Color;
import java.util.Objects;


public abstract class Shape {
    protected int x, y;
    private Color color;

    protected Shape(int x, int y, Color color) {
        this.x = x;
        this.y = y;
        this.color = Objects.requireNonNull(color);
    }

    public abstract java.awt.Shape geometry();

    public Color getColor() {
        return color;
    }

    public void setColor(Color color) {
        this.color = Objects.requireNonNull(color);
    }
}
