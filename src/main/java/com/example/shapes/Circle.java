package com.example.shapes;

import java.awt.Color;

public class Circle extends Ellipse {
    private static final long serialVersionUID = 1L;

    // Создаёт круг с заданным радиусом.
    public Circle(int x, int y, int radius, Color color) {
        super(x, y, radius, radius, color);
    }
}
