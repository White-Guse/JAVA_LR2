package com.example.view;

import com.example.shapes.PencilStroke;
import com.example.shapes.Shape;
import com.example.shapes.ShapeGroup;
import com.example.shapes.ShapeStyle;
import javax.imageio.ImageIO;
import javax.swing.JPanel;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.List;
import java.util.WeakHashMap;

public class ShapeCanvas extends JPanel {
    private List<Shape> shapes = List.of();
    private List<Shape> selection = List.of();
    private Rectangle2D selectionArea;
    private boolean grid = true;
    private final WeakHashMap<ShapeStyle, BufferedImage> textures = new WeakHashMap<>();

    // Создаёт рабочий лист с фиксированной системой координат.
    public ShapeCanvas() {
        setBackground(new Color(253, 252, 247));
        setPreferredSize(new Dimension(900, 650));
        setFocusable(true);
    }

    // Обновляет рисунок и список выделенных объектов.
    public void display(List<Shape> shapes, List<Shape> selection) {
        this.shapes = shapes;
        this.selection = selection;
        repaint();
    }

    // Показывает рамку выделения мышью.
    public void setSelectionArea(Rectangle2D area) { selectionArea = area; repaint(); }

    // Переключает отображение сетки.
    public void toggleGrid() { grid = !grid; repaint(); }

    // Рисует лист, фигуры и рамки выделения.
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D graphics = (Graphics2D) g.create();
        graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        if (grid) drawGrid(graphics);
        for (Shape shape : shapes) drawShape(graphics, shape);
        graphics.setColor(new Color(20, 114, 128));
        graphics.setStroke(new BasicStroke(1.5f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10, new float[]{5, 4}, 0));
        for (Shape shape : selection) {
            if (!isDisplayed(shape, shapes)) continue;
            Rectangle2D bounds = shape.getBounds();
            graphics.draw(new Rectangle2D.Double(bounds.getX() - 5, bounds.getY() - 5,
                    Math.max(1, bounds.getWidth()) + 10, Math.max(1, bounds.getHeight()) + 10));
        }
        if (selectionArea != null) {
            graphics.setColor(new Color(20, 114, 128, 25));
            graphics.fill(selectionArea);
            graphics.setColor(new Color(20, 114, 128));
            graphics.draw(selectionArea);
        }
        if (shapes.isEmpty()) {
            graphics.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 16));
            graphics.setColor(new Color(120, 130, 128));
            graphics.drawString("Выберите фигуру сверху и щёлкните на листе", 70, 90);
            graphics.drawString("Или возьмите карандаш и нарисуйте от руки", 70, 118);
        }
        graphics.dispose();
    }

    // Рисует сетку тетради и отметки координат.
    private void drawGrid(Graphics2D g) {
        g.setStroke(new BasicStroke(1));
        g.setColor(new Color(231, 234, 225));
        for (int x = 0; x < getWidth(); x += 25) g.drawLine(x, 0, x, getHeight());
        for (int y = 0; y < getHeight(); y += 25) g.drawLine(0, y, getWidth(), y);
        g.setColor(new Color(205, 220, 212));
        g.drawLine(50, 0, 50, getHeight());
        g.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 10));
        g.setColor(new Color(130, 145, 137));
        for (int x = 100; x < getWidth(); x += 100) g.drawString(Integer.toString(x), x + 3, 14);
        for (int y = 100; y < getHeight(); y += 100) g.drawString(Integer.toString(y), 5, y - 3);
    }

    // Рисует обычный объект или рекурсивно рисует группу.
    private void drawShape(Graphics2D g, Shape shape) {
        if (!shape.isVisible()) return;
        ShapeStyle style = shape.getStyle();
        java.awt.Shape contour = shape.geometry();
        if (style.hasShadow()) {
            g.setColor(new Color(30, 40, 40, 45));
            java.awt.Shape shadow = AffineTransform.getTranslateInstance(7, 7)
                    .createTransformedShape(shape instanceof ShapeGroup ? visibleContour(shape) : contour);
            if (shape instanceof PencilStroke) {
                g.setStroke(new BasicStroke(style.getThickness(), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g.draw(shadow);
            } else g.fill(shadow);
        }
        if (shape instanceof ShapeGroup group) {
            for (Shape child : group.getChildren()) drawShape(g, child);
        } else {
            Rectangle2D bounds = shape.getBounds();
            Paint fill = style.getColor();
            BufferedImage image = textureOf(style);
            if (image != null) fill = new TexturePaint(image, new Rectangle2D.Double(bounds.getX(), bounds.getY(), 70, 70));
            else if (style.hasVolume()) fill = new GradientPaint((float) bounds.getX(), (float) bounds.getY(),
                    style.getColor().brighter(), (float) bounds.getMaxX(), (float) bounds.getMaxY(), style.getColor().darker());
            if (style.isFilled() && !(shape instanceof PencilStroke)) {
                g.setPaint(fill);
                g.fill(contour);
            }
            g.setColor(shape instanceof PencilStroke ? style.getColor() : style.getColor().darker());
            g.setStroke(new BasicStroke(style.getThickness(), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g.draw(contour);
        }
        drawText(g, shape);
    }

    // Собирает контуры только видимых элементов для тени группы.
    private java.awt.Shape visibleContour(Shape shape) {
        java.awt.geom.Area area = new java.awt.geom.Area();
        if (!shape.isVisible()) return area;
        if (shape instanceof ShapeGroup group) {
            for (Shape child : group.getChildren()) area.add(new java.awt.geom.Area(visibleContour(child)));
        } else {
            java.awt.Shape contour = shape.geometry();
            if (shape instanceof PencilStroke) contour = new BasicStroke(shape.getStyle().getThickness()).createStrokedShape(contour);
            area.add(new java.awt.geom.Area(contour));
        }
        return area;
    }

    // Проверяет видимость выбранного элемента с учётом его родителей.
    private boolean isDisplayed(Shape target, List<Shape> list) {
        for (Shape shape : list) {
            if (!shape.isVisible()) continue;
            if (shape == target) return true;
            if (shape instanceof ShapeGroup group && isDisplayed(target, group.getChildren())) return true;
        }
        return false;
    }

    // Подготавливает изображение заливки один раз для каждого оформления.
    private BufferedImage textureOf(ShapeStyle style) {
        if (textures.containsKey(style)) return textures.get(style);
        byte[] bytes = style.getTexture();
        BufferedImage image = null;
        if (bytes != null) {
            try { image = ImageIO.read(new ByteArrayInputStream(bytes)); }
            catch (IOException ignored) { image = null; }
        }
        textures.put(style, image);
        return image;
    }

    // Подбирает размер надписи под размеры выбранной фигуры.
    private void drawText(Graphics2D g, Shape shape) {
        String text = shape.getStyle().getText();
        if (text.isBlank()) return;
        Rectangle2D bounds = shape.getBounds();
        int size = Math.max(1, (int) Math.min(bounds.getHeight() * 0.23, bounds.getWidth() / Math.max(1, text.length()) * 1.4));
        Font font = new Font(Font.SANS_SERIF, Font.BOLD, size);
        FontMetrics metrics = g.getFontMetrics(font);
        while (size > 1 && metrics.stringWidth(text) > bounds.getWidth() - 12) {
            font = font.deriveFont((float) --size);
            metrics = g.getFontMetrics(font);
        }
        Graphics2D label = (Graphics2D) g.create();
        label.clip(bounds);
        label.setFont(font);
        label.setColor(new Color(25, 38, 40));
        label.drawString(text, (float) (bounds.getCenterX() - metrics.stringWidth(text) / 2.0),
                (float) (bounds.getCenterY() + (metrics.getAscent() - metrics.getDescent()) / 2.0));
        label.dispose();
    }
}
