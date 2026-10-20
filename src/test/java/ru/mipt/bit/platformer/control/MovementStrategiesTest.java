package ru.mipt.bit.platformer.control;

import org.junit.Test;
import ru.mipt.bit.platformer.input.DirectionKeyBinding;
import ru.mipt.bit.platformer.model.Direction;
import ru.mipt.bit.platformer.model.MovementTarget;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class MovementStrategiesTest {

    @Test
    public void keyboardReturnsNoCommandWhenNoBoundKeyIsPressed() {
        KeyboardMovementStrategy strategy = new KeyboardMovementStrategy(key -> key == 99,
                Collections.singletonList(new DirectionKeyBinding(Direction.UP, 1, 2)));

        assertFalse(strategy.nextCommand(new RecordingTarget()).isPresent());
    }

    @Test
    public void keyboardSupportsEmptyBindings() {
        KeyboardMovementStrategy strategy = new KeyboardMovementStrategy(key -> true, Collections.emptyList());

        assertFalse(strategy.nextCommand(new RecordingTarget()).isPresent());
    }

    @Test
    public void keyboardDefersMovementAndUsesFirstPressedBinding() {
        KeyboardMovementStrategy strategy = new KeyboardMovementStrategy(key -> key == 2 || key == 3,
                Arrays.asList(new DirectionKeyBinding(Direction.LEFT, 1, 2),
                        new DirectionKeyBinding(Direction.RIGHT, 3)));
        RecordingTarget target = new RecordingTarget();

        Command command = strategy.nextCommand(target).get();

        assertTrue(target.movements.isEmpty());
        command.execute();
        assertEquals(Collections.singletonList(Direction.LEFT), target.movements);
    }

    @Test
    public void keyboardKeepsItsOwnBindingOrder() {
        List<DirectionKeyBinding> bindings = new ArrayList<>();
        bindings.add(new DirectionKeyBinding(Direction.DOWN, 7));
        KeyboardMovementStrategy strategy = new KeyboardMovementStrategy(key -> key == 7, bindings);
        bindings.clear();
        RecordingTarget target = new RecordingTarget();

        strategy.nextCommand(target).get().execute();

        assertEquals(Collections.singletonList(Direction.DOWN), target.movements);
    }

    @Test
    public void randomStrategyCanChooseEveryDirectionAndDefersExecution() {
        int[] nextIndex = {0};
        Random random = new Random() {
            @Override
            public int nextInt(int bound) {
                assertEquals(Direction.values().length, bound);
                return nextIndex[0]++;
            }
        };
        RandomMovementStrategy strategy = new RandomMovementStrategy(random);
        RecordingTarget target = new RecordingTarget();

        for (Direction ignored : Direction.values()) {
            int previousMovements = target.movements.size();
            Command command = strategy.nextCommand(target).get();
            assertEquals(previousMovements, target.movements.size());
            command.execute();
        }

        assertEquals(Arrays.asList(Direction.values()), target.movements);
    }

    @Test
    public void randomStrategyUsesInjectedGeneratorForEachNewCommand() {
        long seed = 1829L;
        Random expectedRandom = new Random(seed);
        RandomMovementStrategy strategy = new RandomMovementStrategy(new Random(seed));
        List<Direction> expectedMovements = new ArrayList<>();
        RecordingTarget target = new RecordingTarget();

        for (int index = 0; index < 20; index++) {
            expectedMovements.add(Direction.values()[expectedRandom.nextInt(Direction.values().length)]);
            strategy.nextCommand(target).get().execute();
        }

        assertEquals(expectedMovements, target.movements);
    }

    private static class RecordingTarget implements MovementTarget {

        private final List<Direction> movements = new ArrayList<>();

        @Override
        public boolean isMoving() {
            return false;
        }

        @Override
        public void tryMove(Direction direction) {
            movements.add(direction);
        }
    }
}
