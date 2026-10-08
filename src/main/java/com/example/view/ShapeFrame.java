package com.example.view;

import com.example.model.ShapeType;
import com.example.shapes.Shape;
import javax.swing.*;
import java.awt.*;
import java.util.EnumMap;
import java.util.Map;
import java.util.function.Consumer;

public class ShapeFrame extends JFrame {
    private static final Color PANEL_COLOR = new Color(238, 242, 248);
    private final ShapeCanvas canvas = new ShapeCanvas();
    private final JLabel status = new JLabel("Выберите фигуру", SwingConstants.CENTER);
    private final Map<ShapeType, JButton> shapeButtons = new EnumMap<>(ShapeType.class);
    private final JButton clearButton = new JButton("Очистить");

    public ShapeFrame() {
        super("Редактор геометрических фигур");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(860, 560));
        add(createHeader(), BorderLayout.NORTH);
        add(canvas, BorderLayout.CENTER);
        add(createFooter(), BorderLayout.SOUTH);
        pack();
        setLocationRelativeTo(null);
    }

    private JLabel createHeader() {
        JLabel title = new JLabel("Редактор фигур", SwingConstants.CENTER);
        title.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 24));
        title.setBorder(BorderFactory.createEmptyBorder(18, 0, 18, 0));
        title.setOpaque(true);
        title.setBackground(PANEL_COLOR);
        title.setForeground(new Color(35, 49, 72));
        return title;
    }

    private JPanel createFooter() {
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 12));
        buttons.setBackground(PANEL_COLOR);
        for (ShapeType type : ShapeType.values()) {
            JButton button = new JButton(type.getTitle());
            button.setFocusPainted(false);
            button.setMargin(new Insets(8, 12, 8, 12));
            shapeButtons.put(type, button);
            buttons.add(button);
        }
        buttons.add(clearButton);
        status.setForeground(new Color(80, 94, 116));
        status.setBorder(BorderFactory.createEmptyBorder(0, 0, 12, 0));

        JPanel footer = new JPanel(new BorderLayout());
        footer.setBackground(PANEL_COLOR);
        footer.add(buttons, BorderLayout.CENTER);
        footer.add(status, BorderLayout.SOUTH);
        return footer;
    }

    public void onShapeSelected(Consumer<ShapeType> action) {
        shapeButtons.forEach((type, button) ->
                button.addActionListener(event -> action.accept(type)));
    }

    public void onClear(Runnable action) {
        clearButton.addActionListener(event -> action.run());
    }

    public void display(Shape shape, String message) {
        canvas.display(shape);
        status.setText(message);
    }
}
