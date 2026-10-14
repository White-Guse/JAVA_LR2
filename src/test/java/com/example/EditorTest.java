package com.example;

import com.example.model.DrawingFile;
import com.example.model.EditorModel;
import com.example.model.ExampleDrawing;
import com.example.model.ShapeType;
import com.example.shapes.*;
import com.example.view.ShapeCanvas;
import org.junit.Test;
import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import static org.junit.Assert.*;

public class EditorTest {
    // Создаёт две фигуры и выделяет их вместе.
    private EditorModel twoShapes() {
        EditorModel model = new EditorModel();
        model.select(ShapeType.RECTANGLE);
        Shape first = model.addShape(150, 150);
        model.select(ShapeType.CIRCLE);
        Shape second = model.addShape(450, 150);
        model.choose(first, false);
        model.choose(second, true);
        return model;
    }

    // Проверяет добавление нескольких объектов и порядок наложения.
    @Test
    public void layersFollowDrawingOrder() {
        EditorModel model = twoShapes();
        Shape first = model.getShapes().get(0);
        model.choose(first, false);
        model.changeLayer(true);
        assertSame(first, model.getShapes().get(1));
        model.changeLayer(false);
        assertSame(first, model.getShapes().get(0));
    }

    // Проверяет отмену выбора повторным Shift-щелчком.
    @Test
    public void selectionCanBeToggled() {
        EditorModel model = twoShapes();
        Shape first = model.getShapes().get(0);
        model.choose(first, true);
        assertEquals(1, model.getSelection().size());
        assertFalse(model.getSelection().contains(first));
    }

    // Проверяет изменение всех выбранных координат без изменения формы.
    @Test
    public void multipleShapesMoveTogether() {
        EditorModel model = twoShapes();
        Rectangle2D bounds = model.getShapes().get(0).getBounds();
        model.moveSelection(25, -10);
        assertEquals(bounds.getX() + 25, model.getShapes().get(0).getBounds().getX(), 0.001);
        assertEquals(475, model.getShapes().get(1).getBounds().getCenterX(), 0.001);
    }

    // Проверяет создание вложенной группы и её масштабирование.
    @Test
    public void nestedCompositeResizesAllChildren() {
        EditorModel model = twoShapes();
        model.groupSelection();
        ShapeGroup inner = (ShapeGroup) model.getSelectedShape();
        model.select(ShapeType.SQUARE);
        Shape other = model.addShape(650, 350);
        model.choose(inner, false);
        model.choose(other, true);
        model.groupSelection();
        ShapeGroup outer = (ShapeGroup) model.getSelectedShape();
        double originalWidth = inner.getChildren().get(0).getBounds().getWidth();
        Rectangle2D bounds = outer.getBounds();
        outer.setBounds(50, 70, bounds.getWidth() * 2, bounds.getHeight() * 2);
        assertEquals(originalWidth * 2, inner.getChildren().get(0).getBounds().getWidth(), 0.001);
        assertEquals(50, outer.getBounds().getX(), 0.001);
        assertEquals(70, outer.getBounds().getY(), 0.001);
    }

    // Проверяет выбор элемента группы без двойного перемещения.
    @Test
    public void parentAndChildCannotBeSelectedTogether() {
        EditorModel model = twoShapes();
        model.groupSelection();
        ShapeGroup group = (ShapeGroup) model.getSelectedShape();
        Shape child = group.getChildren().get(0);
        model.choose(child, true);
        assertEquals(List.of(child), model.getSelection());
    }

    // Проверяет изменение формы отдельного элемента составной фигуры.
    @Test
    public void groupChildCanBeEditedAndDeleted() {
        EditorModel model = twoShapes();
        model.groupSelection();
        ShapeGroup group = (ShapeGroup) model.getSelectedShape();
        Shape child = group.getChildren().get(0);
        model.choose(child, false);
        model.updateSelected("Элемент", 10, 20, 80, 60, child.getStyle(), true, true);
        assertEquals(80, child.getBounds().getWidth(), 0.001);
        model.deleteSelection();
        assertEquals(1, group.getChildren().size());
        model.choose(group, false);
        model.ungroupSelection();
        assertEquals(1, model.getShapes().size());
        assertFalse(model.getShapes().get(0) instanceof ShapeGroup);
    }

    // Проверяет пропуск скрытых и закреплённых объектов при выборе.
    @Test
    public void hiddenAndLockedShapesCannotBePicked() {
        EditorModel model = twoShapes();
        Shape first = model.getShapes().get(0);
        first.setVisible(false);
        assertNull(model.findAt(150, 150));
        first.setVisible(true);
        first.setFocusable(false);
        assertNull(model.findAt(150, 150));
        first.setFocusable(true);
        assertSame(first, model.findAt(150, 150));
    }

    // Проверяет рисование одиночной точки и горизонтальной линии.
    @Test
    public void pencilKeepsColorAndCanBePicked() {
        EditorModel model = new EditorModel();
        model.setDrawingColor(Color.RED);
        model.selectPencil(3);
        PencilStroke stroke = model.beginStroke(100, 100);
        assertTrue(stroke.contains(100, 100));
        stroke.addPoint(200, 100);
        assertSame(stroke, model.findAt(150, 100));
        ShapeCanvas canvas = new ShapeCanvas();
        canvas.setSize(300, 200);
        canvas.toggleGrid();
        canvas.display(model.getShapes(), List.of());
        BufferedImage image = new BufferedImage(300, 200, BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = image.createGraphics();
        canvas.paint(graphics);
        graphics.dispose();
        assertEquals(Color.RED.getRGB(), image.getRGB(150, 100));
    }

    // Проверяет выделение нескольких фигур рамкой.
    @Test
    public void areaSelectionIncludesOnlyContainedObjects() {
        EditorModel model = twoShapes();
        model.selectArea(new Rectangle2D.Double(0, 0, 600, 300), false);
        assertEquals(2, model.getSelection().size());
        model.selectArea(new Rectangle2D.Double(0, 0, 100, 100), false);
        assertTrue(model.getSelection().isEmpty());
    }

    // Проверяет независимость оформления после переноса свойств.
    @Test
    public void copiedStylesDoNotShareMutableState() {
        EditorModel model = twoShapes();
        Shape first = model.getShapes().get(0);
        first.getStyle().setColor(Color.ORANGE);
        first.getStyle().setShadow(true);
        first.getStyle().setText("Образец");
        model.choose(first, false);
        model.copyStyleToAll();
        Shape second = model.getShapes().get(1);
        assertTrue(second.getStyle().hasShadow());
        assertEquals("Образец", second.getStyle().getText());
        second.setColor(Color.BLUE);
        assertEquals(Color.ORANGE, first.getColor());
    }

    // Проверяет сохранение групп, надписей, текстуры, видимости и координат.
    @Test
    public void documentRoundTripPreservesEditableProperties() throws Exception {
        EditorModel model = twoShapes();
        model.groupSelection();
        ShapeGroup group = (ShapeGroup) model.getSelectedShape();
        Shape child = group.getChildren().get(0);
        child.getStyle().setText("Стол");
        child.getStyle().setVolume(true);
        child.setFocusable(false);
        BufferedImage image = new BufferedImage(4, 4, BufferedImage.TYPE_INT_RGB);
        ByteArrayOutputStream png = new ByteArrayOutputStream();
        ImageIO.write(image, "png", png);
        child.getStyle().setTexture(png.toByteArray());
        group.setVisible(false);
        group.move(20, 30);
        Path file = Files.createTempFile("editor-test-", ".sheet");
        try {
            DrawingFile.save(file, model.getShapes());
            ShapeGroup restored = (ShapeGroup) DrawingFile.load(file).get(0);
            Shape element = restored.getChildren().get(0);
            assertFalse(restored.isVisible());
            assertFalse(element.isFocusable());
            assertEquals("Стол", element.getStyle().getText());
            assertTrue(element.getStyle().hasVolume());
            assertArrayEquals(png.toByteArray(), element.getStyle().getTexture());
            assertEquals(child.getBounds(), element.getBounds());
            element.move(10, 0);
            assertNotEquals(child.getBounds(), element.getBounds());
        } finally { Files.deleteIfExists(file); }
    }

    // Проверяет сохранение точек карандаша и всех исходных типов фигур.
    @Test
    public void everyShapeAndPencilSurviveSaving() throws Exception {
        EditorModel model = new EditorModel();
        for (ShapeType type : ShapeType.values()) { model.select(type); model.addShape(250, 250); }
        PencilStroke stroke = model.beginStroke(50, 50);
        stroke.addPoint(200, 100);
        Path file = Files.createTempFile("editor-all-", ".sheet");
        try {
            DrawingFile.save(file, model.getShapes());
            List<Shape> loaded = DrawingFile.load(file);
            assertEquals(6, loaded.size());
            assertTrue(loaded.get(5).contains(150, 83));
            for (int i = 0; i < loaded.size(); i++) assertEquals(model.getShapes().get(i).getClass(), loaded.get(i).getClass());
        } finally { Files.deleteIfExists(file); }
    }

    // Проверяет отказ от открытия повреждённого файла.
    @Test
    public void invalidFileIsRejected() throws Exception {
        Path file = Files.createTempFile("editor-invalid-", ".sheet");
        try {
            Files.writeString(file, "Это не рисунок");
            assertThrows(IOException.class, () -> DrawingFile.load(file));
        } finally { Files.deleteIfExists(file); }
    }

    // Проверяет сохранение пропорций круга и квадрата.
    @Test
    public void circleAndSquareKeepEqualSides() {
        Shape circle = ShapeType.CIRCLE.createShape();
        Shape square = ShapeType.SQUARE.createShape();
        for (Shape shape : List.of(circle, square)) {
            shape.setBounds(10, 20, 70, 120);
            assertEquals(70, shape.getBounds().getWidth(), 0.001);
            assertEquals(70, shape.getBounds().getHeight(), 0.001);
        }
    }

    // Проверяет отображение примера без зависимости от открытого окна.
    @Test
    public void exampleCanBeRendered() {
        ShapeCanvas canvas = new ShapeCanvas();
        canvas.setSize(900, 650);
        canvas.display(ExampleDrawing.create(), List.of());
        BufferedImage image = new BufferedImage(900, 650, BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = image.createGraphics();
        canvas.paint(graphics);
        graphics.dispose();
        assertNotEquals(image.getRGB(400, 600), image.getRGB(610, 300));
    }
}
