package com.example.controller;

import com.example.model.DrawingFile;
import com.example.model.EditorModel;
import com.example.model.ExampleDrawing;
import com.example.model.ShapeType;
import com.example.shapes.PencilStroke;
import com.example.shapes.Shape;
import com.example.view.PropertiesPanel;
import com.example.view.ShapeFrame;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Iterator;

public class EditorController extends MouseAdapter {
    private final EditorModel model;
    private final ShapeFrame view;
    private Point lastPoint;
    private Point areaStart;
    private PencilStroke stroke;
    private Path currentFile;
    private boolean addSelection;

    // Связывает модель с кнопками, деревом и действиями мыши.
    public EditorController(EditorModel model, ShapeFrame view) {
        this.model = model;
        this.view = view;
        view.onShapeSelected(type -> { model.select(type); refresh(); });
        view.onAction("pointer", () -> { model.selectPointer(); refresh(); });
        view.onAction("pencil", () -> { model.selectPencil(3); refresh(); });
        view.onAction("marker", () -> { model.selectPencil(10); refresh(); });
        view.onAction("group", () -> perform(model::groupSelection));
        view.onAction("ungroup", () -> perform(model::ungroupSelection));
        view.onAction("front", () -> perform(() -> model.changeLayer(true)));
        view.onAction("back", () -> perform(() -> model.changeLayer(false)));
        view.onAction("delete", () -> perform(model::deleteSelection));
        view.onAction("copyStyle", () -> perform(model::copyStyleToAll));
        view.onAction("grid", view.getCanvas()::toggleGrid);
        view.onAction("color", this::chooseDrawingColor);
        view.onAction("save", this::save);
        view.onAction("open", this::open);
        view.onAction("new", this::newDrawing);
        view.onAction("demo", this::showExample);
        view.onTreeSelected(selected -> {
            model.choose(null, false);
            for (Shape shape : selected) model.choose(shape, true);
            refresh();
        });
        view.getProperties().onApply(this::applyProperties);
        view.getProperties().onTexture(this::loadTexture);
        view.getCanvas().addMouseListener(this);
        view.getCanvas().addMouseMotionListener(this);
        view.addWindowListener(new WindowAdapter() {
            // Проверяет несохранённый рисунок перед закрытием.
            @Override
            public void windowClosing(WindowEvent event) { if (canDiscard()) view.dispose(); }
        });
        refresh();
    }

    // Выполняет изменение модели и показывает ошибку ввода.
    private void perform(Runnable action) {
        try { action.run(); refresh(); }
        catch (IllegalArgumentException error) { view.showError(error.getMessage()); }
    }

    // Начинает рисование, выделение рамкой или перемещение.
    @Override
    public void mousePressed(MouseEvent event) {
        if (!SwingUtilities.isLeftMouseButton(event)) return;
        view.getCanvas().requestFocusInWindow();
        lastPoint = event.getPoint();
        addSelection = event.isShiftDown();
        if (model.isPencil()) stroke = model.beginStroke(event.getX(), event.getY());
        else if (model.getTool() != null) model.addShape(event.getX(), event.getY());
        else {
            Shape hit = model.findAt(event.getX(), event.getY());
            Shape selected = model.getSelectedShape();
            if (!addSelection && selected != null && selected.isVisible() && selected.isFocusable()
                    && selected.contains(event.getX(), event.getY())) {
                hit = selected;
            }
            if (hit == null) {
                if (!addSelection) model.choose(null, false);
                areaStart = event.getPoint();
            } else if (addSelection || !model.getSelection().contains(hit)) model.choose(hit, addSelection);
        }
        refresh();
    }

    // Продолжает штрих, перемещение выделения или изменение рамки.
    @Override
    public void mouseDragged(MouseEvent event) {
        if (lastPoint == null) return;
        if (stroke != null) stroke.addPoint(event.getX(), event.getY());
        else if (areaStart != null) view.getCanvas().setSelectionArea(areaBetween(areaStart, event.getPoint()));
        else model.moveSelection(event.getX() - lastPoint.x, event.getY() - lastPoint.y);
        lastPoint = event.getPoint();
        view.getCanvas().repaint();
    }

    // Завершает жест мыши и обновляет свойства выделения.
    @Override
    public void mouseReleased(MouseEvent event) {
        if (!SwingUtilities.isLeftMouseButton(event) || lastPoint == null) return;
        if (areaStart != null) model.selectArea(areaBetween(areaStart, event.getPoint()), addSelection);
        areaStart = null;
        lastPoint = null;
        stroke = null;
        view.getCanvas().setSelectionArea(null);
        refresh();
    }

    // Создаёт рамку независимо от направления движения мыши.
    private Rectangle2D areaBetween(Point first, Point second) {
        return new Rectangle2D.Double(Math.min(first.x, second.x), Math.min(first.y, second.y),
                Math.abs(first.x - second.x), Math.abs(first.y - second.y));
    }

    // Выбирает цвет для следующих фигур и штрихов.
    private void chooseDrawingColor() {
        Color color = JColorChooser.showDialog(view, "Цвет рисования", model.getDrawingColor());
        if (color != null) model.setDrawingColor(color);
    }

    // Передаёт значения панели свойств в модель.
    private void applyProperties() {
        PropertiesPanel panel = view.getProperties();
        try {
            double[] bounds = panel.getGeometryValues();
            model.updateSelected(panel.getObjectName(), bounds[0], bounds[1], bounds[2], bounds[3],
                    panel.getStyle(), panel.getVisibleValue(), panel.getFocusableValue());
            refresh();
        } catch (IllegalArgumentException | java.text.ParseException error) {
            view.showError("Проверьте значения свойств: " + error.getMessage());
        }
    }

    // Загружает изображение заливки и уменьшает его для хранения в рисунке.
    private void loadTexture() {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileFilter(new FileNameExtensionFilter("Изображения PNG и JPEG", "png", "jpg", "jpeg"));
        if (chooser.showOpenDialog(view) != JFileChooser.APPROVE_OPTION) return;
        try (ImageInputStream input = ImageIO.createImageInputStream(chooser.getSelectedFile())) {
            Iterator<ImageReader> readers = ImageIO.getImageReaders(input);
            if (!readers.hasNext()) throw new IOException("Нужен файл изображения");
            ImageReader reader = readers.next();
            BufferedImage image;
            try {
                reader.setInput(input);
                if (reader.getWidth(0) > 4096 || reader.getHeight(0) > 4096) throw new IOException("Максимальный размер: 4096 × 4096");
                image = reader.read(0);
            } finally { reader.dispose(); }
            double scale = Math.min(1, 256.0 / Math.max(image.getWidth(), image.getHeight()));
            BufferedImage tile = new BufferedImage(Math.max(1, (int) (image.getWidth() * scale)),
                    Math.max(1, (int) (image.getHeight() * scale)), BufferedImage.TYPE_INT_ARGB);
            Graphics2D graphics = tile.createGraphics();
            graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            graphics.drawImage(image, 0, 0, tile.getWidth(), tile.getHeight(), null);
            graphics.dispose();
            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            ImageIO.write(tile, "png", bytes);
            view.getProperties().setTexture(bytes.toByteArray());
        } catch (IOException | IllegalArgumentException error) { view.showError("Не удалось загрузить заливку: " + error.getMessage()); }
    }

    // Сохраняет редактируемый рисунок в выбранный файл.
    private boolean save() {
        JFileChooser chooser = drawingChooser();
        if (currentFile != null) chooser.setSelectedFile(currentFile.toFile());
        if (chooser.showSaveDialog(view) != JFileChooser.APPROVE_OPTION) return false;
        Path file = chooser.getSelectedFile().toPath();
        if (!file.getFileName().toString().toLowerCase().endsWith(".sheet")) file = Path.of(file + ".sheet");
        if (java.nio.file.Files.exists(file) && JOptionPane.showConfirmDialog(view, "Заменить существующий файл?",
                "Сохранение", JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) return false;
        try {
            DrawingFile.save(file, model.getShapes());
            currentFile = file;
            model.markSaved();
            refresh();
            return true;
        } catch (IOException error) { view.showError("Не удалось сохранить: " + error.getMessage()); return false; }
    }

    // Открывает рисунок только после успешного чтения всего файла.
    private void open() {
        if (!canDiscard()) return;
        JFileChooser chooser = drawingChooser();
        if (chooser.showOpenDialog(view) != JFileChooser.APPROVE_OPTION) return;
        try {
            Path file = chooser.getSelectedFile().toPath();
            model.loadShapes(DrawingFile.load(file));
            currentFile = file;
            refresh();
        } catch (IOException error) { view.showError("Не удалось открыть: " + error.getMessage()); }
    }

    // Создаёт диалог выбора файла редактируемого рисунка.
    private JFileChooser drawingChooser() {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileFilter(new FileNameExtensionFilter("Рисунок редактора (*.sheet)", "sheet"));
        return chooser;
    }

    // Запрашивает сохранение перед заменой или закрытием рисунка.
    private boolean canDiscard() {
        if (!model.isModified()) return true;
        int answer = JOptionPane.showConfirmDialog(view, "Сохранить изменения рисунка?", "Несохранённый рисунок",
                JOptionPane.YES_NO_CANCEL_OPTION);
        return answer == JOptionPane.NO_OPTION || answer == JOptionPane.YES_OPTION && save();
    }

    // Начинает новый пустой рисунок.
    private void newDrawing() {
        if (!canDiscard()) return;
        model.clear();
        model.markSaved();
        currentFile = null;
        refresh();
    }

    // Загружает небольшой пример с группой и надписями.
    private void showExample() {
        if (!canDiscard()) return;
        model.loadShapes(ExampleDrawing.create());
        model.moveSelection(0, 0);
        currentFile = null;
        refresh();
    }

    // Передаёт представлению актуальное состояние модели.
    private void refresh() {
        view.display(model.getShapes(), model.getSelection(), model.getStatus(), model.isModified());
    }
}
