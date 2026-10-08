package com.example.view;

import com.example.shapes.Shape;
import javax.swing.JPanel;
import java.awt.*;

public class ShapeCanvas extends JPanel {
    private Shape shape;

    public ShapeCanvas() {
        setBackground(Color.WHITE);
        setPreferredSize(new Dimension(860, 400));
    }

    public void display(Shape shape) {
        this.shape = shape;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (shape == null) return;

        Graphics2D graphics = (Graphics2D) g.create();
        try {
            graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);
            graphics.setStroke(new BasicStroke(3f));
            graphics.translate(getWidth() / 2.0, getHeight() / 2.0);
            graphics.setColor(shape.getColor());
            graphics.draw(shape.geometry());
        } finally {
            graphics.dispose();
        }
    }
}
