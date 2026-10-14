package com.example.view;

import com.example.model.ShapeType;
import com.example.shapes.Shape;
import com.example.shapes.ShapeGroup;
import javax.swing.*;
import javax.swing.tree.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class ShapeFrame extends JFrame {
    private final ShapeCanvas canvas = new ShapeCanvas();
    private final PropertiesPanel properties = new PropertiesPanel();
    private final JLabel status = new JLabel(" ");
    private final JTree objects = new JTree(new DefaultMutableTreeNode("Объекты"));
    private final JComboBox<String> tools = new JComboBox<>();
    private final JButton color = new JButton("Цвет");
    private final Map<String, JMenuItem> actions = new HashMap<>();
    private boolean updatingTree;

    // Создаёт обычное окно с холстом и двумя вкладками справа.
    public ShapeFrame() {
        super("Графический редактор");
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setMinimumSize(new Dimension(900, 600));
        setJMenuBar(createMenu());
        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT));
        tools.addItem("Выделение");
        tools.addItem("Карандаш");
        tools.addItem("Маркер");
        for (ShapeType type : ShapeType.values()) tools.addItem(type.getTitle());
        toolbar.add(new JLabel("Инструмент:"));
        toolbar.add(tools);
        toolbar.add(color);
        add(toolbar, BorderLayout.NORTH);
        objects.setRootVisible(false);
        objects.getSelectionModel().setSelectionMode(TreeSelectionModel.DISCONTIGUOUS_TREE_SELECTION);
        JTabbedPane tabs = new JTabbedPane();
        tabs.setPreferredSize(new Dimension(260, 460));
        tabs.addTab("Объекты", new JScrollPane(objects));
        tabs.addTab("Свойства", properties);
        add(tabs, BorderLayout.EAST);
        add(new JScrollPane(canvas), BorderLayout.CENTER);
        status.setBorder(BorderFactory.createEmptyBorder(4, 6, 4, 6));
        add(status, BorderLayout.SOUTH);
        pack();
        setLocationRelativeTo(null);
    }

    // Создаёт меню файлов и операций над фигурами.
    private JMenuBar createMenu() {
        JMenuBar menu = new JMenuBar();
        JMenu file = new JMenu("Файл");
        addCommand(file, "new", "Новый", "control N");
        addCommand(file, "open", "Открыть", "control O");
        addCommand(file, "save", "Сохранить", "control S");
        JMenu edit = new JMenu("Правка");
        addCommand(edit, "delete", "Удалить", null);
        addCommand(edit, "group", "В группу", null);
        addCommand(edit, "ungroup", "Разобрать", null);
        edit.addSeparator();
        addCommand(edit, "front", "Вперёд", null);
        addCommand(edit, "back", "Назад", null);
        addCommand(edit, "copyStyle", "Стиль всем", null);
        menu.add(file);
        menu.add(edit);
        return menu;
    }

    // Добавляет пункт меню и его сочетание клавиш.
    private void addCommand(JMenu menu, String key, String title, String shortcut) {
        JMenuItem item = new JMenuItem(title);
        if (shortcut != null) item.setAccelerator(KeyStroke.getKeyStroke(shortcut));
        actions.put(key, item);
        menu.add(item);
    }

    // Подключает меню, цвет или переключение инструмента к контроллеру.
    public void onAction(String key, Runnable action) {
        if (key.equals("color")) color.addActionListener(event -> action.run());
        else if (key.equals("pointer") || key.equals("pencil") || key.equals("marker")) {
            int index = key.equals("pointer") ? 0 : key.equals("pencil") ? 1 : 2;
            tools.addActionListener(event -> { if (tools.getSelectedIndex() == index) action.run(); });
        } else actions.get(key).addActionListener(event -> action.run());
    }

    // Передаёт контроллеру выбранный тип фигуры.
    public void onShapeSelected(Consumer<ShapeType> action) {
        tools.addActionListener(event -> {
            int index = tools.getSelectedIndex() - 3;
            if (index >= 0) action.accept(ShapeType.values()[index]);
        });
    }

    // Передаёт выбранные объекты из дерева в модель.
    public void onTreeSelected(Consumer<List<Shape>> action) {
        objects.addTreeSelectionListener(event -> {
            if (updatingTree) return;
            List<Shape> selected = new ArrayList<>();
            TreePath[] paths = objects.getSelectionPaths();
            if (paths != null) for (TreePath path : paths) {
                Object value = ((DefaultMutableTreeNode) path.getLastPathComponent()).getUserObject();
                if (value instanceof Shape shape) selected.add(shape);
            }
            action.accept(selected);
        });
    }

    // Возвращает холст для обработки мыши.
    public ShapeCanvas getCanvas() { return canvas; }

    // Возвращает поля свойств.
    public PropertiesPanel getProperties() { return properties; }

    // Обновляет холст, свойства и дерево объектов.
    public void display(List<Shape> shapes, List<Shape> selected, String message, boolean modified) {
        canvas.display(shapes, selected);
        properties.display(selected.isEmpty() ? null : selected.get(0), selected.size());
        status.setText(message);
        setTitle((modified ? "* " : "") + "Графический редактор");
        updatingTree = true;
        DefaultMutableTreeNode root = new DefaultMutableTreeNode("Объекты");
        List<TreePath> paths = new ArrayList<>();
        for (Shape shape : shapes) addNode(root, shape, selected, paths);
        objects.setModel(new DefaultTreeModel(root));
        for (int row = 0; row < objects.getRowCount(); row++) objects.expandRow(row);
        objects.setSelectionPaths(paths.toArray(new TreePath[0]));
        updatingTree = false;
        actions.get("group").setEnabled(selected.size() >= 2);
        actions.get("ungroup").setEnabled(selected.size() == 1 && selected.get(0) instanceof ShapeGroup);
        for (String key : List.of("front", "back", "delete")) actions.get(key).setEnabled(!selected.isEmpty());
        actions.get("copyStyle").setEnabled(selected.size() == 1);
    }

    // Добавляет фигуру и её элементы в дерево.
    private void addNode(DefaultMutableTreeNode parent, Shape shape, List<Shape> selected, List<TreePath> paths) {
        DefaultMutableTreeNode node = new DefaultMutableTreeNode(shape);
        parent.add(node);
        if (selected.contains(shape)) paths.add(new TreePath(node.getPath()));
        if (shape instanceof ShapeGroup group) for (Shape child : group.getChildren()) addNode(node, child, selected, paths);
    }

    // Показывает ошибку ввода или чтения файла.
    public void showError(String message) { JOptionPane.showMessageDialog(this, message, "Ошибка", JOptionPane.WARNING_MESSAGE); }
}
