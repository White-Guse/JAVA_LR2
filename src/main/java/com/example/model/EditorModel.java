package com.example.model;

import com.example.shapes.PencilStroke;
import com.example.shapes.Shape;
import com.example.shapes.ShapeGroup;
import com.example.shapes.ShapeStyle;
import java.awt.Color;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class EditorModel {
    private final List<Shape> shapes = new ArrayList<>();
    private final List<Shape> selection = new ArrayList<>();
    private ShapeType tool;
    private boolean pencil;
    private Color drawingColor = new Color(34, 105, 115);
    private int pencilWidth = 3;
    private int nextNumber = 1;
    private boolean modified;

    // Возвращает объекты от заднего плана к переднему.
    public List<Shape> getShapes() { return Collections.unmodifiableList(shapes); }

    // Возвращает текущее выделение.
    public List<Shape> getSelection() { return Collections.unmodifiableList(selection); }

    // Возвращает первый выбранный объект.
    public Shape getSelectedShape() { return selection.isEmpty() ? null : selection.get(0); }

    // Включает добавление выбранного типа фигур.
    public void select(ShapeType type) { tool = type; pencil = false; }

    // Включает инструмент выделения.
    public void selectPointer() { tool = null; pencil = false; }

    // Включает карандаш с выбранной толщиной.
    public void selectPencil(int width) { tool = null; pencil = true; pencilWidth = width; }

    // Возвращает активный тип фигуры.
    public ShapeType getTool() { return tool; }

    // Проверяет режим рисования карандашом.
    public boolean isPencil() { return pencil; }

    // Возвращает цвет новых объектов.
    public Color getDrawingColor() { return drawingColor; }

    // Устанавливает цвет новых объектов.
    public void setDrawingColor(Color color) { drawingColor = color; }

    // Добавляет фигуру в точку щелчка мышью.
    public Shape addShape(int x, int y) {
        if (tool == null) return null;
        Shape shape = tool.createShape();
        shape.move(x, y);
        shape.setColor(drawingColor);
        shape.setName(tool.getTitle() + " " + nextNumber++);
        shapes.add(shape);
        choose(shape, false);
        modified = true;
        return shape;
    }

    // Начинает новый штрих карандаша.
    public PencilStroke beginStroke(int x, int y) {
        PencilStroke stroke = new PencilStroke(x, y, drawingColor, pencilWidth);
        stroke.setName((pencilWidth > 3 ? "Маркер " : "Карандаш ") + nextNumber++);
        shapes.add(stroke);
        choose(stroke, false);
        modified = true;
        return stroke;
    }

    // Ищет верхний видимый объект под курсором.
    public Shape findAt(int x, int y) {
        for (int i = shapes.size() - 1; i >= 0; i--) {
            Shape shape = shapes.get(i);
            if (shape.isVisible() && shape.isFocusable() && shape.contains(x, y)) return shape;
        }
        return null;
    }

    // Выбирает объект или переключает его участие в выделении.
    public void choose(Shape shape, boolean toggle) {
        if (!toggle) selection.clear();
        if (shape == null) return;
        if (toggle && selection.contains(shape)) { selection.remove(shape); return; }
        for (Shape selected : new ArrayList<>(selection)) {
            if (containsShape(selected, shape) || containsShape(shape, selected)) selection.remove(selected);
        }
        selection.add(shape);
    }

    // Проверяет вложенность объектов в составной фигуре.
    private boolean containsShape(Shape parent, Shape child) {
        if (!(parent instanceof ShapeGroup group)) return false;
        for (Shape element : group.getChildren()) {
            if (element == child || containsShape(element, child)) return true;
        }
        return false;
    }

    // Выделяет доступные объекты внутри рамки.
    public void selectArea(Rectangle2D area, boolean add) {
        if (!add) selection.clear();
        for (Shape shape : shapes) {
            if (shape.isVisible() && shape.isFocusable() && area.contains(shape.getBounds())
                    && !selection.contains(shape)) choose(shape, true);
        }
    }

    // Перемещает все выбранные объекты.
    public void moveSelection(double dx, double dy) {
        for (Shape shape : selection) shape.move(dx, dy);
        if (!selection.isEmpty()) modified = true;
    }

    // Находит список, непосредственно содержащий объект.
    private List<Shape> ownerOf(Shape target, List<Shape> list) {
        if (list.contains(target)) return list;
        for (Shape shape : list) {
            if (shape instanceof ShapeGroup group) {
                List<Shape> owner = ownerOf(target, group.mutableChildren());
                if (owner != null) return owner;
            }
        }
        return null;
    }

    // Удаляет выбранные фигуры из их группы или холста.
    public void deleteSelection() {
        for (Shape shape : selection) {
            List<Shape> owner = ownerOf(shape, shapes);
            if (owner != null) owner.remove(shape);
        }
        pruneEmptyGroups(shapes);
        if (!selection.isEmpty()) modified = true;
        selection.clear();
    }

    // Убирает пустые группы после удаления элементов.
    private void pruneEmptyGroups(List<Shape> list) {
        for (Shape shape : new ArrayList<>(list)) {
            if (shape instanceof ShapeGroup group) {
                pruneEmptyGroups(group.mutableChildren());
                if (group.getChildren().isEmpty()) list.remove(group);
            }
        }
    }

    // Объединяет выбранные объекты одного уровня в группу.
    public void groupSelection() {
        if (selection.size() < 2) throw new IllegalArgumentException("Выделите минимум два объекта");
        List<Shape> owner = ownerOf(selection.get(0), shapes);
        for (Shape shape : selection) {
            if (ownerOf(shape, shapes) != owner) throw new IllegalArgumentException("Выберите объекты одного уровня");
        }
        List<Shape> ordered = new ArrayList<>();
        for (Shape shape : owner) if (selection.contains(shape)) ordered.add(shape);
        int position = owner.indexOf(ordered.get(0));
        owner.removeAll(ordered);
        ShapeGroup group = new ShapeGroup(ordered);
        group.setName("Группа " + nextNumber++);
        owner.add(Math.min(position, owner.size()), group);
        choose(group, false);
        modified = true;
    }

    // Разбирает выбранную группу на её непосредственные элементы.
    public void ungroupSelection() {
        Shape chosen = getSelectedShape();
        if (selection.size() != 1 || !(chosen instanceof ShapeGroup group)) {
            throw new IllegalArgumentException("Выберите одну группу");
        }
        List<Shape> owner = ownerOf(group, shapes);
        int index = owner.indexOf(group);
        owner.remove(group);
        owner.addAll(index, group.getChildren());
        selection.clear();
        selection.addAll(group.getChildren());
        modified = true;
    }

    // Меняет порядок фигур внутри их непосредственного уровня.
    public void changeLayer(boolean front) {
        List<List<Shape>> owners = new ArrayList<>();
        for (Shape shape : selection) {
            List<Shape> owner = ownerOf(shape, shapes);
            if (owners.stream().noneMatch(list -> list == owner)) owners.add(owner);
        }
        for (List<Shape> owner : owners) {
            List<Shape> moved = new ArrayList<>();
            for (Shape shape : owner) if (selection.contains(shape)) moved.add(shape);
            owner.removeAll(moved);
            owner.addAll(front ? owner.size() : 0, moved);
        }
        if (!selection.isEmpty()) modified = true;
    }

    // Меняет свойства одного выбранного объекта.
    public void updateSelected(String name, double x, double y, double width, double height,
                               ShapeStyle style, boolean visible, boolean focusable) {
        if (selection.size() != 1) throw new IllegalArgumentException("Для свойств выберите один объект");
        Shape shape = getSelectedShape();
        shape.setBounds(x, y, width, height);
        shape.setName(name.isBlank() ? shape.getName() : name);
        applyStyle(shape, style);
        shape.setVisible(visible);
        shape.setFocusable(focusable);
        modified = true;
    }

    // Применяет оформление группы к элементам, сохраняя их надписи.
    private void applyStyle(Shape shape, ShapeStyle style) {
        shape.setStyle(style);
        if (shape instanceof ShapeGroup group) {
            for (Shape child : group.getChildren()) {
                ShapeStyle childStyle = new ShapeStyle(style);
                childStyle.setText(child.getStyle().getText());
                childStyle.setShadow(false);
                applyStyle(child, childStyle);
            }
        }
    }

    // Переносит оформление образца на остальные объекты.
    public void copyStyleToAll() {
        if (selection.size() != 1) throw new IllegalArgumentException("Выберите один образец оформления");
        ShapeStyle sample = new ShapeStyle(getSelectedShape().getStyle());
        for (Shape shape : shapes) applyStyle(shape, sample);
        modified = true;
    }

    // Очищает рисунок и выделение.
    public void clear() { shapes.clear(); selection.clear(); modified = true; }

    // Заменяет рисунок объектами из файла.
    public void loadShapes(List<Shape> saved) {
        shapes.clear();
        shapes.addAll(saved);
        selection.clear();
        nextNumber = countShapes(shapes) + 1;
        modified = false;
    }

    // Считает объекты вместе с вложенными группами.
    private int countShapes(List<Shape> list) {
        int count = list.size();
        for (Shape shape : list) if (shape instanceof ShapeGroup group) count += countShapes(group.getChildren());
        return count;
    }

    // Проверяет наличие несохранённых изменений.
    public boolean isModified() { return modified; }

    // Отмечает завершённое сохранение рисунка.
    public void markSaved() { modified = false; }

    // Формирует подсказку для текущего инструмента.
    public String getStatus() {
        String hint = tool != null ? "Щёлкните на листе: " + tool.getTitle()
                : pencil ? "Зажмите левую кнопку и рисуйте" : "Shift + щелчок: несколько объектов; пустое место: рамка";
        return hint + "   |   Объектов: " + countShapes(shapes) + "   |   Выделено: " + selection.size();
    }
}
