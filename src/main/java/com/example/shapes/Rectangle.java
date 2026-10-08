package com.example.shapes;

import java.awt.Color;
import java.awt.geom.Rectangle2D;

public class Rectangle extends Shape {
    protected int width, height;

    public Rectangle(int x, int y, int width, int height, Color color) {
        super(x, y, color);
        if (width <= 0 || height <= 0) {
            throw new IllegalArgumentException("Размеры должны быть положительными");
        }
        this.width = width;
        this.height = height;
    }

    @Override
    public java.awt.Shape geometry() {
        return new Rectangle2D.Double(x - width / 2.0, y - height / 2.0, width, height);
    }
}
