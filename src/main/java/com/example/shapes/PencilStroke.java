package com.example.shapes;

import java.awt.Color;
import java.awt.Point;
import java.awt.geom.Path2D;
import java.util.ArrayList;
import java.util.List;

public class PencilStroke extends Shape {
    private static final long serialVersionUID = 1L;
    private final List<Point> points = new ArrayList<>();

    // Начинает линию карандаша с одной точки.
    public PencilStroke(int x, int y, Color color, int thickness) {
        super(x, y, color);
        points.add(new Point(x, y));
        getStyle().setThickness(thickness);
        getStyle().setFilled(false);
        setName("Карандаш");
    }

    // Добавляет очередную точку движения мыши.
    public void addPoint(int x, int y) {
        Point point = new Point(x, y);
        if (!points.get(points.size() - 1).equals(point)) points.add(point);
    }

    // Соединяет сохранённые точки в линию.
    @Override
    protected java.awt.Shape baseGeometry() {
        Path2D path = new Path2D.Double();
        Point first = points.get(0);
        path.moveTo(first.x, first.y);
        if (points.size() == 1) path.lineTo(first.x + 0.1, first.y + 0.1);
        for (int i = 1; i < points.size(); i++) path.lineTo(points.get(i).x, points.get(i).y);
        return path;
    }
}
