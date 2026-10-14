package com.example.model;

import com.example.shapes.Circle;
import com.example.shapes.PencilStroke;
import com.example.shapes.Rectangle;
import com.example.shapes.RoundedRectangle;
import com.example.shapes.Shape;
import com.example.shapes.ShapeGroup;
import java.awt.Color;
import java.util.List;

public final class ExampleDrawing {
    // Запрещает создание вспомогательного объекта.
    private ExampleDrawing() { }

    // Создаёт схему мастерской для знакомства с инструментами.
    public static List<Shape> create() {
        Shape title = new RoundedRectangle(390, 95, 550, 90, 25, new Color(205, 226, 206));
        title.setName("Заголовок");
        title.getStyle().setText("ПЛАН МАСТЕРСКОЙ");
        Shape table = new RoundedRectangle(300, 290, 300, 150, 25, new Color(232, 196, 133));
        table.setName("Рабочий стол");
        table.getStyle().setVolume(true);
        table.getStyle().setText("Стол");
        Shape chair = new Circle(300, 420, 45, new Color(98, 159, 169));
        chair.setName("Стул");
        ShapeGroup workplace = new ShapeGroup(List.of(table, chair));
        workplace.setName("Рабочее место");
        workplace.getStyle().setShadow(true);
        Shape shelf = new Rectangle(610, 305, 145, 230, new Color(182, 198, 180));
        shelf.setName("Шкаф");
        shelf.getStyle().setText("Материалы");
        shelf.getStyle().setShadow(true);
        PencilStroke path = new PencilStroke(170, 525, new Color(174, 82, 56), 3);
        path.setName("Заметка карандашом");
        for (int x = 175; x <= 650; x += 5) path.addPoint(x, 525 + (int) (6 * Math.sin(x / 25.0)));
        return List.of(title, workplace, shelf, path);
    }
}
