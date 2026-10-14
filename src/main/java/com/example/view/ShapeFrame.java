package com.example.view;

import com.example.model.ShapeType;
import com.example.shapes.Shape;
import com.example.shapes.ShapeGroup;
import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class ShapeFrame extends JFrame {
    private final ShapeCanvas canvas = new ShapeCanvas();
    private final PropertiesPanel properties = new PropertiesPanel();
    private final JLabel status = new JLabel(" ");
    private final JTree objects = new JTree(new DefaultMutableTreeNode("Объекты"));
    private final Map<String, AbstractButton> actions = new LinkedHashMap<>();
    private final Map<ShapeType, JToggleButton> tools = new LinkedHashMap<>();
    private final ButtonGroup toolGroup = new ButtonGroup();
    private boolean updatingTree;

    // Создаёт окно редактора с листом, деревом и свойствами.
    public ShapeFrame() {
        super("Лист — редактор фигур");
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setMinimumSize(new Dimension(1050, 720));
        add(createHeader(), BorderLayout.NORTH);
        JPanel sidebar = new JPanel(new BorderLayout());
        sidebar.setPreferredSize(new Dimension(290, 650));
        objects.setRootVisible(false);
        objects.getSelectionModel().setSelectionMode(TreeSelectionModel.DISCONTIGUOUS_TREE_SELECTION);
        JScrollPane treeScroll = new JScrollPane(objects);
        treeScroll.setPreferredSize(new Dimension(280, 180));
        treeScroll.setBorder(BorderFactory.createTitledBorder("Объекты · снизу вверх"));
        sidebar.add(treeScroll, BorderLayout.NORTH);
        sidebar.add(properties, BorderLayout.CENTER);
        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, new JScrollPane(canvas), sidebar);
        split.setResizeWeight(1.0);
        split.setBorder(null);
        add(split, BorderLayout.CENTER);
        status.setBorder(BorderFactory.createEmptyBorder(9, 16, 9, 16));
        status.setOpaque(true);
        status.setBackground(new Color(223, 233, 226));
        add(status, BorderLayout.SOUTH);
        installShortcuts();
        pack();
        setLocationRelativeTo(null);
    }

    // Собирает заголовок и две строки инструментов.
    private JPanel createHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(new Color(28, 74, 72));
        JLabel title = new JLabel("  ЛИСТ / геометрическая мастерская");
        title.setForeground(Color.WHITE);
        title.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 20));
        title.setBorder(BorderFactory.createEmptyBorder(14, 8, 14, 8));
        header.add(title, BorderLayout.NORTH);
        JPanel rows = new JPanel(new GridLayout(2, 1));
        JPanel fileRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 6));
        addButton(fileRow, "new", "Новый");
        addButton(fileRow, "open", "Открыть…");
        addButton(fileRow, "save", "Сохранить…");
        addButton(fileRow, "demo", "Пример");
        addButton(fileRow, "group", "В группу");
        addButton(fileRow, "ungroup", "Разобрать");
        addButton(fileRow, "front", "Вперёд");
        addButton(fileRow, "back", "Назад");
        addButton(fileRow, "delete", "Удалить");
        addButton(fileRow, "copyStyle", "Стиль всем");
        addButton(fileRow, "grid", "Сетка");
        rows.add(fileRow);
        JPanel toolRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 6));
        addTool(toolRow, "pointer", "Выделение").setSelected(true);
        addTool(toolRow, "pencil", "Карандаш");
        addTool(toolRow, "marker", "Маркер");
        for (ShapeType type : ShapeType.values()) {
            JToggleButton button = new JToggleButton(type.getTitle());
            toolGroup.add(button);
            tools.put(type, button);
            toolRow.add(button);
        }
        addButton(toolRow, "color", "Цвет рисования…");
        rows.add(toolRow);
        header.add(rows, BorderLayout.CENTER);
        return header;
    }

    // Добавляет обычную кнопку команды.
    private void addButton(JPanel row, String key, String title) {
        JButton button = new JButton(title);
        button.setFocusPainted(false);
        actions.put(key, button);
        row.add(button);
    }

    // Добавляет переключатель режима работы мыши.
    private JToggleButton addTool(JPanel row, String key, String title) {
        JToggleButton button = new JToggleButton(title);
        toolGroup.add(button);
        actions.put(key, button);
        row.add(button);
        return button;
    }

    // Подключает Ctrl+S, Ctrl+O и клавишу Delete к кнопкам.
    private void installShortcuts() {
        bindShortcut("control S", "save");
        bindShortcut("control O", "open");
        bindShortcut("control N", "new");
        canvas.getInputMap(JComponent.WHEN_FOCUSED).put(KeyStroke.getKeyStroke("DELETE"), "delete");
        canvas.getActionMap().put("delete", new AbstractAction() {
            // Запускает удаление выделенных объектов.
            @Override
            public void actionPerformed(java.awt.event.ActionEvent event) { actions.get("delete").doClick(); }
        });
    }

    // Назначает сочетание клавиш на существующую команду.
    private void bindShortcut(String keys, String action) {
        getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(keys), action);
        getRootPane().getActionMap().put(action, new AbstractAction() {
            // Выполняет команду соответствующей кнопки.
            @Override
            public void actionPerformed(java.awt.event.ActionEvent event) { actions.get(action).doClick(); }
        });
    }

    // Подключает действие контроллера к кнопке.
    public void onAction(String key, Runnable action) { actions.get(key).addActionListener(event -> action.run()); }

    // Подключает выбор типа фигуры.
    public void onShapeSelected(Consumer<ShapeType> action) {
        tools.forEach((type, button) -> button.addActionListener(event -> action.accept(type)));
    }

    // Передаёт контроллеру выбранные элементы дерева.
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

    // Возвращает холст для подключения обработчиков мыши.
    public ShapeCanvas getCanvas() { return canvas; }

    // Возвращает панель свойств.
    public PropertiesPanel getProperties() { return properties; }

    // Отображает рисунок, выделение, свойства и состояние документа.
    public void display(List<Shape> shapes, List<Shape> selected, String message, boolean modified) {
        canvas.display(shapes, selected);
        properties.display(selected.isEmpty() ? null : selected.get(0), selected.size());
        status.setText(message);
        setTitle((modified ? "* " : "") + "Лист — редактор фигур");
        updatingTree = true;
        DefaultMutableTreeNode root = new DefaultMutableTreeNode("Объекты");
        List<TreePath> chosen = new ArrayList<>();
        for (Shape shape : shapes) addNode(root, shape, selected, chosen);
        objects.setModel(new DefaultTreeModel(root));
        for (int row = 0; row < objects.getRowCount(); row++) objects.expandRow(row);
        objects.setSelectionPaths(chosen.toArray(TreePath[]::new));
        updatingTree = false;
        actions.get("group").setEnabled(selected.size() >= 2);
        actions.get("ungroup").setEnabled(selected.size() == 1 && selected.get(0) instanceof ShapeGroup);
        for (String key : List.of("front", "back", "delete")) actions.get(key).setEnabled(!selected.isEmpty());
        actions.get("copyStyle").setEnabled(selected.size() == 1);
    }

    // Добавляет объект и вложенные группы в дерево.
    private void addNode(DefaultMutableTreeNode parent, Shape shape, List<Shape> selected, List<TreePath> paths) {
        DefaultMutableTreeNode node = new DefaultMutableTreeNode(shape);
        parent.add(node);
        if (selected.contains(shape)) paths.add(new TreePath(node.getPath()));
        if (shape instanceof ShapeGroup group) for (Shape child : group.getChildren()) addNode(node, child, selected, paths);
    }

    // Показывает пользователю понятное сообщение об ошибке.
    public void showError(String message) { JOptionPane.showMessageDialog(this, message, "Не получилось", JOptionPane.WARNING_MESSAGE); }
}
