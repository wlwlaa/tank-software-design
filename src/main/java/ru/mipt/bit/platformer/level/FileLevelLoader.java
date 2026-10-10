package ru.mipt.bit.platformer.level;

import com.badlogic.gdx.math.GridPoint2;
import ru.mipt.bit.platformer.model.Obstacle;
import ru.mipt.bit.platformer.model.Tree;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class FileLevelLoader implements LevelSource {

    private final Path path;

    public FileLevelLoader(Path path) {
        this.path = Objects.requireNonNull(path, "path");
    }

    @Override
    public LevelDefinition load() throws IOException {
        try (BufferedReader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            return parse(reader);
        }
    }

    public static LevelDefinition parse(Reader reader) throws IOException {
        Objects.requireNonNull(reader, "reader");
        BufferedReader bufferedReader = reader instanceof BufferedReader
                ? (BufferedReader) reader : new BufferedReader(reader);
        List<String> rows = new ArrayList<>();
        String row;
        int width = -1;
        while ((row = bufferedReader.readLine()) != null) {
            int rowNumber = rows.size() + 1;
            if (row.isEmpty()) {
                throw invalidCell(rowNumber, 1, "Rows must not be empty");
            }
            if (width < 0) {
                width = row.length();
            } else if (row.length() != width) {
                int column = Math.min(width, row.length()) + 1;
                throw invalidCell(rowNumber, column,
                        "Expected " + width + " cells, got " + row.length());
            }
            rows.add(row);
        }
        if (rows.isEmpty()) {
            throw new IllegalArgumentException("Level file must not be empty");
        }

        List<Obstacle> obstacles = new ArrayList<>();
        GridPoint2 playerStart = null;
        int height = rows.size();
        for (int rowIndex = 0; rowIndex < height; rowIndex++) {
            String cells = rows.get(rowIndex);
            for (int column = 0; column < width; column++) {
                GridPoint2 coordinates = new GridPoint2(column, height - rowIndex - 1);
                switch (cells.charAt(column)) {
                    case 'T':
                        obstacles.add(new Tree(coordinates));
                        break;
                    case 'X':
                        if (playerStart != null) {
                            throw invalidCell(rowIndex + 1, column + 1,
                                    "Level must contain exactly one X (player start)");
                        }
                        playerStart = coordinates;
                        break;
                    case '_':
                        break;
                    default:
                        throw invalidCell(rowIndex + 1, column + 1,
                                "Unknown symbol '" + cells.charAt(column) + "'; expected T, X or _");
                }
            }
        }
        if (playerStart == null) {
            throw new IllegalArgumentException("Level must contain exactly one X (player start)");
        }
        return new LevelDefinition(width, height, obstacles, playerStart);
    }

    private static IllegalArgumentException invalidCell(int row, int column, String message) {
        return new IllegalArgumentException("Invalid level at row " + row + ", column " + column + ": " + message);
    }
}
