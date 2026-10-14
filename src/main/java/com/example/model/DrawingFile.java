package com.example.model;

import com.example.shapes.Shape;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;

public final class DrawingFile {
    private static final String HEADER = "SHAPE-NOTE-1";

    // Запрещает создание вспомогательного объекта.
    private DrawingFile() { }

    // Сохраняет рисунок через временный файл без потери предыдущей версии.
    public static void save(Path path, List<Shape> shapes) throws IOException {
        Path absolute = path.toAbsolutePath();
        Path temporary = Files.createTempFile(absolute.getParent(), "drawing-", ".tmp");
        try {
            try (ObjectOutputStream output = new ObjectOutputStream(Files.newOutputStream(temporary))) {
                output.writeUTF(HEADER);
                output.writeObject(new ArrayList<>(shapes));
            }
            Files.move(temporary, absolute, StandardCopyOption.REPLACE_EXISTING);
        } finally {
            Files.deleteIfExists(temporary);
        }
    }

    // Загружает разрешённые классы рисунка с ограничением размера файла.
    public static List<Shape> load(Path path) throws IOException {
        if (Files.size(path) > 30_000_000) throw new IOException("Файл слишком большой");
        try (ObjectInputStream input = new ObjectInputStream(Files.newInputStream(path))) {
            input.setObjectInputFilter(DrawingFile::checkClass);
            if (!HEADER.equals(input.readUTF())) throw new IOException("Неизвестный формат рисунка");
            Object data = input.readObject();
            if (!(data instanceof ArrayList<?> list)) throw new IOException("Некорректный список фигур");
            List<Shape> result = new ArrayList<>();
            for (Object value : list) {
                if (!(value instanceof Shape shape)) throw new IOException("Некорректный объект рисунка");
                result.add(shape);
            }
            return result;
        } catch (ClassNotFoundException | RuntimeException error) {
            throw new IOException("Не удалось прочитать рисунок", error);
        }
    }

    // Отсекает посторонние классы и чрезмерно большие графы объектов.
    private static ObjectInputFilter.Status checkClass(ObjectInputFilter.FilterInfo info) {
        if (info.depth() > 60 || info.references() > 100000 || info.arrayLength() > 5000000) {
            return ObjectInputFilter.Status.REJECTED;
        }
        Class<?> type = info.serialClass();
        if (type == null) return ObjectInputFilter.Status.UNDECIDED;
        if (type.isArray() && (type.getComponentType().isPrimitive() || type.getComponentType() == Object.class)) {
            return ObjectInputFilter.Status.ALLOWED;
        }
        String name = type.getName();
        return name.startsWith("com.example.shapes.") || name.equals("java.util.ArrayList")
                || name.equals("java.awt.Color") || name.equals("java.awt.Point")
                || name.equals("java.awt.geom.AffineTransform")
                ? ObjectInputFilter.Status.ALLOWED : ObjectInputFilter.Status.REJECTED;
    }
}
