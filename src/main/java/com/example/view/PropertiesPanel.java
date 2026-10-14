package com.example.view;

import com.example.shapes.Shape;
import com.example.shapes.ShapeStyle;
import javax.swing.*;
import java.awt.*;
import java.awt.geom.Rectangle2D;

public class PropertiesPanel extends JPanel {
    private final JTextField name = new JTextField();
    private final JTextField text = new JTextField();
    private final JSpinner x = new JSpinner(new SpinnerNumberModel(0.0, -100000.0, 100000.0, 1.0));
    private final JSpinner y = new JSpinner(new SpinnerNumberModel(0.0, -100000.0, 100000.0, 1.0));
    private final JSpinner width = new JSpinner(new SpinnerNumberModel(100.0, 1.0, 100000.0, 1.0));
    private final JSpinner height = new JSpinner(new SpinnerNumberModel(100.0, 1.0, 100000.0, 1.0));
    private final JSpinner thickness = new JSpinner(new SpinnerNumberModel(3, 1, 30, 1));
    private final JCheckBox filled = new JCheckBox("Заливка");
    private final JCheckBox shadow = new JCheckBox("Тень");
    private final JCheckBox volume = new JCheckBox("Объём");
    private final JCheckBox visible = new JCheckBox("Виден");
    private final JCheckBox focusable = new JCheckBox("Выбирается мышью");
    private final JButton color = new JButton("Цвет фигуры");
    private final JButton texture = new JButton("Рисунок заливки…");
    private final JButton resetTexture = new JButton("Убрать рисунок");
    private final JButton apply = new JButton("Применить свойства");
    private final JLabel hint = new JLabel("Выберите один объект");
    private ShapeStyle draft = new ShapeStyle(new Color(34, 105, 115));

    // Создаёт небольшую панель параметров выбранного объекта.
    public PropertiesPanel() {
        setLayout(new GridLayout(0, 1, 0, 6));
        setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        add(hint);
        add(row("Имя", name));
        add(row("X", x));
        add(row("Y", y));
        add(row("Ширина", width));
        add(row("Высота", height));
        add(row("Линия", thickness));
        add(row("Надпись", text));
        add(color);
        add(pair(filled, shadow));
        add(pair(volume, visible));
        add(focusable);
        add(texture);
        add(resetTexture);
        add(apply);
        color.addActionListener(event -> chooseColor());
        resetTexture.addActionListener(event -> { draft.setTexture(null); texture.setText("Рисунок заливки…"); });
    }

    // Размещает подпись слева от редактора значения.
    private JPanel row(String title, JComponent field) {
        JPanel row = new JPanel(new BorderLayout(8, 0));
        JLabel label = new JLabel(title);
        label.setPreferredSize(new Dimension(70, 24));
        row.add(label, BorderLayout.WEST);
        row.add(field, BorderLayout.CENTER);
        return row;
    }

    // Размещает два переключателя в одной строке.
    private JPanel pair(JComponent first, JComponent second) {
        JPanel pair = new JPanel(new GridLayout(1, 2));
        pair.add(first);
        pair.add(second);
        return pair;
    }

    // Открывает выбор цвета для черновика свойств.
    private void chooseColor() {
        Color selected = JColorChooser.showDialog(this, "Цвет фигуры", draft.getColor());
        if (selected != null) { draft.setColor(selected); color.setForeground(selected.darker()); }
    }

    // Показывает свойства одиночного выделения.
    public void display(Shape shape, int selectedCount) {
        boolean enabled = shape != null && selectedCount == 1;
        setFieldsEnabled(this, enabled);
        hint.setEnabled(true);
        hint.setText(enabled ? "Свойства объекта" : "Выберите один объект");
        if (!enabled) return;
        Rectangle2D bounds = shape.getBounds();
        draft = new ShapeStyle(shape.getStyle());
        name.setText(shape.getName());
        text.setText(draft.getText());
        x.setValue(bounds.getX());
        y.setValue(bounds.getY());
        width.setValue(Math.max(1, bounds.getWidth()));
        height.setValue(Math.max(1, bounds.getHeight()));
        thickness.setValue(draft.getThickness());
        filled.setSelected(draft.isFilled());
        shadow.setSelected(draft.hasShadow());
        volume.setSelected(draft.hasVolume());
        visible.setSelected(shape.isVisible());
        focusable.setSelected(shape.isFocusable());
        color.setForeground(draft.getColor().darker());
        texture.setText(draft.getTexture() == null ? "Рисунок заливки…" : "Заменить рисунок…");
    }

    // Переключает доступность всех редакторов панели.
    private void setFieldsEnabled(Container parent, boolean enabled) {
        for (Component child : parent.getComponents()) {
            child.setEnabled(enabled);
            if (child instanceof Container container) setFieldsEnabled(container, enabled);
        }
    }

    // Передаёт контроллеру нажатие кнопки применения.
    public void onApply(Runnable action) { apply.addActionListener(event -> action.run()); }

    // Передаёт контроллеру запрос загрузки текстуры.
    public void onTexture(Runnable action) { texture.addActionListener(event -> action.run()); }

    // Сохраняет выбранную текстуру в черновике до применения.
    public void setTexture(byte[] png) { draft.setTexture(png); filled.setSelected(true); texture.setText("Заменить рисунок…"); }

    // Собирает оформление из элементов управления.
    public ShapeStyle getStyle() {
        draft.setThickness(((Number) thickness.getValue()).intValue());
        draft.setFilled(filled.isSelected());
        draft.setShadow(shadow.isSelected());
        draft.setVolume(volume.isSelected());
        draft.setText(text.getText());
        return new ShapeStyle(draft);
    }

    // Возвращает введённое имя объекта.
    public String getObjectName() { return name.getText().trim(); }

    // Возвращает координаты и размеры с проверкой введённых чисел.
    public double[] getGeometryValues() throws java.text.ParseException {
        JSpinner[] fields = {x, y, width, height, thickness};
        for (JSpinner field : fields) field.commitEdit();
        return new double[]{((Number) x.getValue()).doubleValue(), ((Number) y.getValue()).doubleValue(),
                ((Number) width.getValue()).doubleValue(), ((Number) height.getValue()).doubleValue()};
    }

    // Возвращает состояние видимости.
    public boolean getVisibleValue() { return visible.isSelected(); }

    // Возвращает разрешение выбора на холсте.
    public boolean getFocusableValue() { return focusable.isSelected(); }
}
