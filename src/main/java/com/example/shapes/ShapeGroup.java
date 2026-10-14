package com.example.shapes;

import java.awt.Color;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ShapeGroup extends Shape {
    private static final long serialVersionUID = 1L;
    private final List<Shape> children = new ArrayList<>();

    // Объединяет фигуры в составной объект.
    public ShapeGroup(List<Shape> shapes) {
        super(0, 0, Color.GRAY);
        if (shapes.size() < 2) throw new IllegalArgumentException("Нужны хотя бы две фигуры");
        children.addAll(shapes);
        setName("Группа");
    }

    // Возвращает элементы группы без доступа к изменению списка.
    public List<Shape> getChildren() { return Collections.unmodifiableList(children); }

    // Даёт модели доступ к добавлению и удалению элементов группы.
    public List<Shape> mutableChildren() { return children; }

    // Строит общий контур группы из контуров её элементов.
    @Override
    protected java.awt.Shape baseGeometry() {
        Area area = new Area();
        for (Shape child : children) area.add(new Area(child.geometry()));
        return area;
    }

    // Перемещает и масштабирует все элементы, включая вложенные группы.
    @Override
    public void applyTransform(AffineTransform change) {
        for (Shape child : children) child.applyTransform(change);
    }

    // Проверяет попадание только в видимые элементы группы.
    @Override
    public boolean contains(double px, double py) {
        for (Shape child : children) if (child.isVisible() && child.contains(px, py)) return true;
        return false;
    }
}
