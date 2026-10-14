package com.example.model;

import com.example.shapes.*;
import java.awt.Color;

public enum ShapeType {
    CIRCLE("Круг"),
    ELLIPSE("Эллипс"),
    RECTANGLE("Прямоугольник"),
    ROUNDED_RECTANGLE("Скруглённый"),
    SQUARE("Квадрат");

    private final String title;

    // Сохраняет название инструмента.
    ShapeType(String title) {
        this.title = title;
    }

    // Возвращает название фигуры.
    public String getTitle() {
        return title;
    }

    // Создаёт заготовку выбранной фигуры.
    public Shape createShape() {
        return switch (this) {
            case CIRCLE -> new Circle(0, 0, 90, new Color(239, 68, 68));
            case ELLIPSE -> new Ellipse(0, 0, 140, 80, new Color(59, 130, 246));
            case RECTANGLE -> new Rectangle(0, 0, 240, 150, new Color(22, 163, 74));
            case ROUNDED_RECTANGLE -> new RoundedRectangle(0, 0, 240, 150, 40, new Color(139, 92, 246));
            case SQUARE -> new Square(0, 0, 160, new Color(234, 138, 20));
        };
    }
}
