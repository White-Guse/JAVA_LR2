package com.example.shapes;

import java.awt.Color;

public class Circle extends Ellipse {

    public Circle(int x, int y, int radius, Color color) {
        super(x, y, radius, radius, color);
    }
}
