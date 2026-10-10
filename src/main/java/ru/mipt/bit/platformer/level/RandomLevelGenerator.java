package ru.mipt.bit.platformer.level;

import com.badlogic.gdx.math.GridPoint2;
import ru.mipt.bit.platformer.model.Obstacle;
import ru.mipt.bit.platformer.model.Tree;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;

public final class RandomLevelGenerator implements LevelSource {

    private final int width;
    private final int height;
    private final int obstacleCount;
    private final int cellCount;
    private final Random random;

    public RandomLevelGenerator(int width, int height, int obstacleCount, Random random) {
        if (width <= 0 || height <= 0) {
            throw new IllegalArgumentException("Level width and height must be positive");
        }
        long totalCells = (long) width * height;
        if (totalCells > Integer.MAX_VALUE) {
            throw new IllegalArgumentException("Level has too many cells: " + totalCells);
        }
        if (obstacleCount < 0 || obstacleCount >= totalCells) {
            throw new IllegalArgumentException("Obstacle count must be between 0 and " + (totalCells - 1));
        }
        this.width = width;
        this.height = height;
        this.obstacleCount = obstacleCount;
        this.cellCount = (int) totalCells;
        this.random = Objects.requireNonNull(random, "random");
    }

    @Override
    public LevelDefinition load() {
        // Partially shuffle cell indices without allocating an array for the whole field.
        Map<Integer, Integer> swaps = new HashMap<>();
        GridPoint2 playerStart = coordinates(drawCell(swaps, cellCount));
        List<Obstacle> obstacles = new ArrayList<>();
        for (int index = 0; index < obstacleCount; index++) {
            int cell = drawCell(swaps, cellCount - index - 1);
            obstacles.add(new Tree(coordinates(cell)));
        }
        return new LevelDefinition(width, height, obstacles, playerStart);
    }

    private int drawCell(Map<Integer, Integer> swaps, int remaining) {
        int index = random.nextInt(remaining);
        int cell = swaps.getOrDefault(index, index);
        int lastIndex = remaining - 1;
        int lastCell = swaps.getOrDefault(lastIndex, lastIndex);
        swaps.remove(lastIndex);
        if (index != lastIndex) {
            swaps.put(index, lastCell);
        }
        return cell;
    }

    private GridPoint2 coordinates(int cell) {
        return new GridPoint2(cell % width, cell / width);
    }
}
