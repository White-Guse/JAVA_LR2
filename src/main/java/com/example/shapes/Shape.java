package com.example.shapes;

import java.awt.Color;
import java.util.Objects;
import java.io.Serializable;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;


public abstract class Shape implements Serializable {
    private static final long serialVersionUID = 1L;
    protected int x, y;
    private ShapeStyle style;
    private String name;
    private boolean visible = true;
    private boolean focusable = true;
    private final AffineTransform transform = new AffineTransform();

    // Создаёт общие свойства фигуры.
    protected Shape(int x, int y, Color color) {
        this.x = x;
        this.y = y;
        style = new ShapeStyle(Objects.requireNonNull(color));
        name = getClass().getSimpleName();
    }

    // Возвращает исходный контур фигуры.
    protected abstract java.awt.Shape baseGeometry();

    // Возвращает контур с учётом перемещения и масштаба.
    public java.awt.Shape geometry() { return transform.createTransformedShape(baseGeometry()); }

    // Находит прямоугольник, занимаемый фигурой.
    public Rectangle2D getBounds() { return geometry().getBounds2D(); }

    // Переносит фигуру на указанное смещение.
    public void move(double dx, double dy) { applyTransform(AffineTransform.getTranslateInstance(dx, dy)); }

    // Применяет преобразование к текущему контуру.
    public void applyTransform(AffineTransform change) { transform.preConcatenate(change); }

    // Меняет положение и размеры фигуры.
    public void setBounds(double left, double top, double width, double height) {
        if (!Double.isFinite(left) || !Double.isFinite(top) || !Double.isFinite(width)
                || !Double.isFinite(height) || width < 1 || height < 1) {
            throw new IllegalArgumentException("Размеры должны быть положительными");
        }
        Rectangle2D old = getBounds();
        if (this instanceof Circle || this instanceof Square) height = width;
        AffineTransform change = new AffineTransform();
        change.translate(left, top);
        change.scale(width / Math.max(1, old.getWidth()), height / Math.max(1, old.getHeight()));
        change.translate(-old.getX(), -old.getY());
        applyTransform(change);
    }

    // Проверяет попадание мыши в фигуру или рядом с контуром.
    public boolean contains(double px, double py) {
        return geometry().contains(px, py) || geometry().intersects(px - 4, py - 4, 8, 8);
    }

    // Возвращает цвет контура.
    public Color getColor() {
        return style.getColor();
    }

    // Меняет цвет контура.
    public void setColor(Color color) {
        style.setColor(color);
    }

    // Возвращает название объекта в списке.
    public String getName() { return name; }

    // Задаёт название объекта.
    public void setName(String name) { this.name = Objects.requireNonNull(name); }

    // Возвращает оформление фигуры.
    public ShapeStyle getStyle() { return style; }

    // Копирует оформление без изменения геометрии.
    public void setStyle(ShapeStyle style) { this.style = new ShapeStyle(style); }

    // Проверяет видимость фигуры.
    public boolean isVisible() { return visible; }

    // Меняет видимость фигуры.
    public void setVisible(boolean visible) { this.visible = visible; }

    // Проверяет возможность выбора фигуры мышью.
    public boolean isFocusable() { return focusable; }

    // Разрешает или запрещает выбор на холсте.
    public void setFocusable(boolean focusable) { this.focusable = focusable; }

    // Показывает имя и состояние объекта в дереве.
    @Override
    public String toString() {
        return name + (visible ? "" : " [скрыт]") + (focusable ? "" : " [закреплён]");
    }
}
