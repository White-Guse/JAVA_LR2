package com.example;

import com.example.controller.EditorController;
import com.example.model.EditorModel;
import com.example.model.ExampleDrawing;
import com.example.model.ShapeType;
import com.example.shapes.Shape;
import com.example.shapes.ShapeGroup;
import com.example.view.ShapeCanvas;
import com.example.view.ShapeFrame;
import org.junit.Test;
import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.Component;
import java.awt.Container;
import java.awt.Graphics2D;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.nio.file.Files;
import java.nio.file.Path;
import static org.junit.Assert.*;

public class EditorUiTest {
    // Находит кнопку по видимой подписи без доступа к внутренним полям окна.
    private AbstractButton button(Container parent, String text) {
        for (Component child : parent.getComponents()) {
            if (child instanceof AbstractButton found && text.equals(found.getText())) return found;
            if (child instanceof Container nested) {
                AbstractButton found = button(nested, text);
                if (found != null) return found;
            }
        }
        return null;
    }

    // Передаёт холсту обычное событие левой кнопки мыши.
    private void mouse(ShapeCanvas canvas, int id, int x, int y) {
        canvas.dispatchEvent(new MouseEvent(canvas, id, System.currentTimeMillis(),
                id == MouseEvent.MOUSE_DRAGGED ? MouseEvent.BUTTON1_DOWN_MASK : 0,
                x, y, 1, false, MouseEvent.BUTTON1));
    }

    // Проверяет полный путь от кнопок и мыши до перемещения и удаления группы.
    @Test
    public void toolbarAndMouseControlTheModel() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            EditorModel model = new EditorModel();
            ShapeFrame frame = new ShapeFrame();
            try {
                new EditorController(model, frame);
                button(frame, "Прямоугольник").doClick();
                mouse(frame.getCanvas(), MouseEvent.MOUSE_PRESSED, 200, 200);
                mouse(frame.getCanvas(), MouseEvent.MOUSE_RELEASED, 200, 200);
                button(frame, "Круг").doClick();
                mouse(frame.getCanvas(), MouseEvent.MOUSE_PRESSED, 500, 200);
                mouse(frame.getCanvas(), MouseEvent.MOUSE_RELEASED, 500, 200);
                assertEquals(2, model.getShapes().size());
                button(frame, "Выделение").doClick();
                mouse(frame.getCanvas(), MouseEvent.MOUSE_PRESSED, 20, 20);
                mouse(frame.getCanvas(), MouseEvent.MOUSE_DRAGGED, 700, 400);
                mouse(frame.getCanvas(), MouseEvent.MOUSE_RELEASED, 700, 400);
                assertEquals(2, model.getSelection().size());
                button(frame, "В группу").doClick();
                assertTrue(model.getSelectedShape() instanceof ShapeGroup);
                mouse(frame.getCanvas(), MouseEvent.MOUSE_PRESSED, 200, 200);
                mouse(frame.getCanvas(), MouseEvent.MOUSE_DRAGGED, 230, 240);
                mouse(frame.getCanvas(), MouseEvent.MOUSE_RELEASED, 230, 240);
                ShapeGroup group = (ShapeGroup) model.getSelectedShape();
                assertEquals(230, group.getChildren().get(0).getBounds().getCenterX(), 0.001);
                button(frame, "Разобрать").doClick();
                assertEquals(2, model.getShapes().size());
                button(frame, "Удалить").doClick();
                assertTrue(model.getShapes().isEmpty());
            } finally { frame.dispose(); }
        });
    }

    // Проверяет единый цвет красного карандаша при рисовании через контроллер.
    @Test
    public void redPencilWorksThroughMouseEvents() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            EditorModel model = new EditorModel();
            model.setDrawingColor(java.awt.Color.RED);
            ShapeFrame frame = new ShapeFrame();
            try {
                new EditorController(model, frame);
                button(frame, "Карандаш").doClick();
                mouse(frame.getCanvas(), MouseEvent.MOUSE_PRESSED, 100, 100);
                mouse(frame.getCanvas(), MouseEvent.MOUSE_DRAGGED, 150, 130);
                mouse(frame.getCanvas(), MouseEvent.MOUSE_RELEASED, 150, 130);
                Shape stroke = model.getShapes().get(0);
                assertEquals(java.awt.Color.RED, stroke.getColor());
                assertTrue(stroke.contains(125, 115));
                assertEquals(50, stroke.getBounds().getWidth(), 0.001);
            } finally { frame.dispose(); }
        });
    }

    // Сохраняет изображение окна для проверки размещения элементов.
    @Test
    public void saveWindowPreview() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            EditorModel model = new EditorModel();
            model.loadShapes(ExampleDrawing.create());
            model.choose(model.getShapes().get(1), false);
            ShapeFrame frame = new ShapeFrame();
            try {
                new EditorController(model, frame);
                frame.setSize(1250, 850);
                frame.validate();
                BufferedImage image = new BufferedImage(frame.getContentPane().getWidth(), frame.getContentPane().getHeight(), BufferedImage.TYPE_INT_RGB);
                Graphics2D graphics = image.createGraphics();
                frame.getContentPane().printAll(graphics);
                graphics.dispose();
                Path path = Path.of("target", "editor-preview.png");
                Files.createDirectories(path.getParent());
                ImageIO.write(image, "png", path.toFile());
                assertTrue(frame.getCanvas().getWidth() > 500);
                assertTrue(frame.getProperties().getWidth() >= 200);
                assertNotEquals(image.getRGB(5, 5), image.getRGB(500, 500));
            } catch (java.io.IOException error) { throw new IllegalStateException(error); }
            finally { frame.dispose(); }
        });
    }
}
