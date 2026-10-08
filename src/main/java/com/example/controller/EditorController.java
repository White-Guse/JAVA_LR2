package com.example.controller;

import com.example.model.EditorModel;
import com.example.model.ShapeType;
import com.example.view.ShapeFrame;

public class EditorController {
    private final EditorModel model;
    private final ShapeFrame view;

    public EditorController(EditorModel model, ShapeFrame view) {
        this.model = model;
        this.view = view;
        view.onShapeSelected(this::select);
        view.onClear(this::clear);
        refresh();
    }

    public void select(ShapeType type) {
        model.select(type);
        refresh();
    }

    public void clear() {
        model.clear();
        refresh();
    }

    private void refresh() {
        view.display(model.getSelectedShape(), model.getStatus());
    }
}
