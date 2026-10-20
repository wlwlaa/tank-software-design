package ru.mipt.bit.platformer.level;

import com.badlogic.gdx.math.GridPoint2;
import ru.mipt.bit.platformer.model.Tank;
import ru.mipt.bit.platformer.model.TankWorld;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;

public final class RandomTankGenerator {

    private final Random random;

    public RandomTankGenerator(Random random) {
        this.random = Objects.requireNonNull(random, "random");
    }

    public List<Tank> generate(TankWorld world, int maxTankCount, float movementDuration) {
        Objects.requireNonNull(world, "world");
        if (maxTankCount < 0) {
            throw new IllegalArgumentException("Tank count must not be negative");
        }
        if (!Float.isFinite(movementDuration) || movementDuration <= 0f) {
            throw new IllegalArgumentException("Movement duration must be finite and positive");
        }
        int width = world.getWidth();
        int height = world.getHeight();
        long cellCount = (long) width * height;
        if (width <= 0 || height <= 0 || cellCount > Integer.MAX_VALUE) {
            throw new IllegalArgumentException("Level must have between 1 and " + Integer.MAX_VALUE + " cells");
        }

        // Sample without replacement; even an occupied field needs at most one visit per cell.
        // Store only shuffled indices that differ from their original position.
        Map<Integer, Integer> swaps = new HashMap<>();
        List<Tank> generatedTanks = new ArrayList<>();
        for (int remaining = (int) cellCount; remaining > 0 && generatedTanks.size() < maxTankCount; remaining--) {
            int cell = drawCell(swaps, remaining);
            GridPoint2 coordinates = new GridPoint2(cell % width, cell / width);
            if (world.canMoveTo(coordinates)) {
                generatedTanks.add(world.addTank(coordinates, movementDuration));
            }
        }
        return Collections.unmodifiableList(generatedTanks);
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
}
