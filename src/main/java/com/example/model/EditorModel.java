package com.example.model;

import com.example.shapes.Shape;
import java.util.Objects;

public class EditorModel {
    private Shape selectedShape;
    private ShapeType selectedType;

    public void select(ShapeType type) {
        ShapeType checkedType = Objects.requireNonNull(type);
        Shape newShape = checkedType.createShape();
        selectedType = checkedType;
        selectedShape = newShape;
    }

    public void clear() {
        selectedType = null;
        selectedShape = null;
    }

    public Shape getSelectedShape() {
        return selectedShape;
    }

    public String getStatus() {
        return selectedType == null ? "Выберите фигуру"
                : "Выбрана фигура: " + selectedType.getTitle();
    }
}
