package com.example.shapes;

import java.awt.Color;
import java.awt.geom.Ellipse2D;

public class Ellipse extends Shape {
    private static final long serialVersionUID = 1L;
    protected int a, b;

    // Создаёт эллипс с заданными полуосями.
    public Ellipse(int x, int y, int a, int b, Color color) {
        super(x, y, color);
        if (a <= 0 || b <= 0) {
            throw new IllegalArgumentException("Полуоси должны быть положительными");
        }
        this.a = a;
        this.b = b;
    }

    // Возвращает исходный контур эллипса.
    @Override
    protected java.awt.Shape baseGeometry() {
        return new Ellipse2D.Double(x - a, y - b, 2.0 * a, 2.0 * b);
    }
}
