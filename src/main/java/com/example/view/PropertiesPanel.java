package com.example.view;

import com.example.shapes.Shape;
import com.example.shapes.ShapeStyle;
import javax.swing.*;
import java.awt.*;
import java.awt.geom.Rectangle2D;

public class PropertiesPanel extends JPanel {
    private final JTextField x = new JTextField();
    private final JTextField y = new JTextField();
    private final JTextField width = new JTextField();
    private final JTextField height = new JTextField();
    private final JTextField thickness = new JTextField();
    private final JTextField text = new JTextField();
    private final JCheckBox filled = new JCheckBox("Заливка");
    private final JCheckBox shadow = new JCheckBox("Тень");
    private final JCheckBox volume = new JCheckBox("Объём");
    private final JCheckBox visible = new JCheckBox("Виден");
    private final JCheckBox focusable = new JCheckBox("Выделяется");
    private final JButton color = new JButton("Цвет");
    private final JButton texture = new JButton("Рисунок…");
    private final JButton resetTexture = new JButton("Без рисунка");
    private final JButton apply = new JButton("Применить");
    private final JLabel hint = new JLabel("Выберите объект");
    private ShapeStyle draft = new ShapeStyle(Color.BLACK);
    private String objectName = "";

    // Создаёт обычные поля ввода свойств фигуры.
    public PropertiesPanel() {
        setLayout(new GridLayout(0, 2, 4, 4));
        setBorder(BorderFactory.createEmptyBorder(6, 6, 6, 6));
        add(hint);
        add(new JLabel());
        addField("X", x);
        addField("Y", y);
        addField("Ширина", width);
        addField("Высота", height);
        addField("Толщина", thickness);
        addField("Текст", text);
        add(filled);
        add(shadow);
        add(volume);
        add(visible);
        add(focusable);
        add(color);
        add(texture);
        add(resetTexture);
        add(apply);
        add(new JLabel());
        color.addActionListener(event -> {
            Color selected = JColorChooser.showDialog(this, "Цвет", draft.getColor());
            if (selected != null) draft.setColor(selected);
        });
        resetTexture.addActionListener(event -> { draft.setTexture(null); texture.setText("Рисунок…"); });
    }

    // Добавляет подпись и текстовое поле.
    private void addField(String title, JTextField field) { add(new JLabel(title)); add(field); }

    // Заполняет поля свойствами выбранного объекта.
    public void display(Shape shape, int count) {
        boolean enabled = shape != null && count == 1;
        for (Component field : getComponents()) field.setEnabled(enabled);
        hint.setEnabled(true);
        hint.setText(enabled ? "Свойства" : "Выберите объект");
        if (!enabled) return;
        Rectangle2D bounds = shape.getBounds();
        objectName = shape.getName();
        draft = new ShapeStyle(shape.getStyle());
        x.setText(Double.toString(bounds.getX()));
        y.setText(Double.toString(bounds.getY()));
        width.setText(Double.toString(Math.max(1, bounds.getWidth())));
        height.setText(Double.toString(Math.max(1, bounds.getHeight())));
        thickness.setText(Integer.toString(draft.getThickness()));
        text.setText(draft.getText());
        filled.setSelected(draft.isFilled());
        shadow.setSelected(draft.hasShadow());
        volume.setSelected(draft.hasVolume());
        visible.setSelected(shape.isVisible());
        focusable.setSelected(shape.isFocusable());
        texture.setText(draft.getTexture() == null ? "Рисунок…" : "Заменить…");
    }

    // Подключает применение свойств к контроллеру.
    public void onApply(Runnable action) { apply.addActionListener(event -> action.run()); }

    // Подключает выбор изображения заливки.
    public void onTexture(Runnable action) { texture.addActionListener(event -> action.run()); }

    // Запоминает изображение до нажатия кнопки применения.
    public void setTexture(byte[] png) { draft.setTexture(png); filled.setSelected(true); texture.setText("Заменить…"); }

    // Собирает оформление из введённых значений.
    public ShapeStyle getStyle() {
        int lineWidth = Integer.parseInt(thickness.getText().trim());
        if (lineWidth < 1 || lineWidth > 30) throw new IllegalArgumentException("Толщина: от 1 до 30");
        draft.setThickness(lineWidth);
        draft.setFilled(filled.isSelected());
        draft.setShadow(shadow.isSelected());
        draft.setVolume(volume.isSelected());
        draft.setText(text.getText());
        return new ShapeStyle(draft);
    }

    // Возвращает исходное имя выбранного объекта.
    public String getObjectName() { return objectName; }

    // Читает координаты и размеры из текстовых полей.
    public double[] getGeometryValues() {
        return new double[]{Double.parseDouble(x.getText().trim()), Double.parseDouble(y.getText().trim()),
                Double.parseDouble(width.getText().trim()), Double.parseDouble(height.getText().trim())};
    }

    // Возвращает состояние видимости.
    public boolean getVisibleValue() { return visible.isSelected(); }

    // Возвращает разрешение выбора мышью.
    public boolean getFocusableValue() { return focusable.isSelected(); }
}
