package com.example.app;

import com.example.controller.EditorController;
import com.example.model.EditorModel;
import com.example.view.ShapeFrame;
import javax.swing.SwingUtilities;

public final class Application {
    // Запрещает создание класса запуска.
    private Application() { }

    // Запускает редактор в потоке интерфейса Swing.
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            EditorModel model = new EditorModel();
            ShapeFrame view = new ShapeFrame();
            new EditorController(model, view);
            view.setVisible(true);
        });
    }
}
