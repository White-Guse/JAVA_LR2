package com.example.shapes;

import java.awt.Color;
import java.awt.geom.RoundRectangle2D;

public class RoundedRectangle extends Rectangle {
    private final int arc; // ширина и высота дуги скругления

    public RoundedRectangle(int x, int y, int width, int height, int arc, Color color) {
        super(x, y, width, height, color);
        if (arc < 0) {
            throw new IllegalArgumentException("Скругление не может быть отрицательным");
        }
        this.arc = arc;
    }

    @Override
    public java.awt.Shape geometry() {
        return new RoundRectangle2D.Double(x - width / 2.0, y - height / 2.0,
                width, height, arc, arc);
    }
}
