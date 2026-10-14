package com.example.shapes;

import java.awt.Color;
import java.awt.geom.Rectangle2D;

public class Rectangle extends Shape {
    private static final long serialVersionUID = 1L;
    protected int width, height;

    // Создаёт прямоугольник с заданными сторонами.
    public Rectangle(int x, int y, int width, int height, Color color) {
        super(x, y, color);
        if (width <= 0 || height <= 0) {
            throw new IllegalArgumentException("Размеры должны быть положительными");
        }
        this.width = width;
        this.height = height;
    }

    // Возвращает исходный контур прямоугольника.
    @Override
    protected java.awt.Shape baseGeometry() {
        return new Rectangle2D.Double(x - width / 2.0, y - height / 2.0, width, height);
    }
}
