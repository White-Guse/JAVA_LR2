package com.example.shapes;

import java.awt.Color;

public class Square extends Rectangle {
    private static final long serialVersionUID = 1L;

    // Создаёт квадрат с заданной стороной.
    public Square(int x, int y, int side, Color color) {
        super(x, y, side, side, color);
    }
}
