package com.example.shapes;

import java.awt.Color;
import java.io.Serializable;
import java.util.Objects;

public class ShapeStyle implements Serializable {
    private static final long serialVersionUID = 1L;
    private Color color;
    private int thickness = 3;
    private boolean filled = true;
    private boolean shadow;
    private boolean volume;
    private String text = "";
    private byte[] texture;

    // Создаёт оформление с выбранным цветом.
    public ShapeStyle(Color color) { this.color = Objects.requireNonNull(color); }

    // Создаёт независимую копию оформления.
    public ShapeStyle(ShapeStyle source) {
        color = source.color;
        thickness = source.thickness;
        filled = source.filled;
        shadow = source.shadow;
        volume = source.volume;
        text = source.text;
        texture = source.texture == null ? null : source.texture.clone();
    }

    // Возвращает цвет фигуры.
    public Color getColor() { return color; }

    // Устанавливает цвет фигуры.
    public void setColor(Color color) { this.color = Objects.requireNonNull(color); }

    // Возвращает толщину линии.
    public int getThickness() { return thickness; }

    // Устанавливает толщину линии в допустимом диапазоне.
    public void setThickness(int value) { thickness = Math.max(1, Math.min(30, value)); }

    // Проверяет наличие заливки.
    public boolean isFilled() { return filled; }

    // Включает заливку фигуры.
    public void setFilled(boolean value) { filled = value; }

    // Проверяет наличие тени.
    public boolean hasShadow() { return shadow; }

    // Включает тень.
    public void setShadow(boolean value) { shadow = value; }

    // Проверяет наличие объёмной заливки.
    public boolean hasVolume() { return volume; }

    // Включает эффект объёма.
    public void setVolume(boolean value) { volume = value; }

    // Возвращает надпись.
    public String getText() { return text; }

    // Устанавливает надпись.
    public void setText(String value) { text = Objects.requireNonNull(value); }

    // Возвращает изображение заливки в формате PNG.
    public byte[] getTexture() { return texture == null ? null : texture.clone(); }

    // Сохраняет независимую копию изображения заливки.
    public void setTexture(byte[] value) { texture = value == null ? null : value.clone(); }
}
