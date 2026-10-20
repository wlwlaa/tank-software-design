package ru.mipt.bit.platformer.control;

import com.badlogic.gdx.math.GridPoint2;
import org.junit.Test;
import ru.mipt.bit.platformer.model.Direction;
import ru.mipt.bit.platformer.model.Field;
import ru.mipt.bit.platformer.model.MovementTarget;
import ru.mipt.bit.platformer.model.Tank;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

public class TankControllerTest {

    @Test
    public void passesTargetToStrategyAndExecutesOneSelectedCommand() {
        RecordingTarget target = new RecordingTarget();
        List<String> calls = new ArrayList<>();
        TankController controller = new TankController(target, requestedTarget -> {
            assertSame(target, requestedTarget);
            calls.add("select");
            return Optional.of(() -> calls.add("execute"));
        });

        controller.handle();

        assertEquals(Arrays.asList("select", "execute"), calls);
    }

    @Test
    public void doesNotSelectCommandsWhileTargetIsMoving() {
        RecordingTarget target = new RecordingTarget();
        target.moving = true;
        TankController controller = new TankController(target, requestedTarget -> {
            throw new AssertionError("A moving tank must not request another command");
        });

        controller.handle();

        assertTrue(target.movements.isEmpty());
    }

    @Test
    public void allowsStrategyToLeaveTankIdle() {
        RecordingTarget target = new RecordingTarget();
        TankController controller = new TankController(target, requestedTarget -> Optional.empty());

        controller.handle();

        assertTrue(target.movements.isEmpty());
    }

    @Test
    public void selectsAgainAfterMovementCompletes() {
        RecordingTarget target = new RecordingTarget();
        TankController controller = new TankController(target,
                requestedTarget -> Optional.of(new MoveCommand(requestedTarget, Direction.UP)));

        controller.handle();
        controller.handle();
        target.moving = false;
        controller.handle();

        assertEquals(Arrays.asList(Direction.UP, Direction.UP), target.movements);
    }

    @Test
    public void retriesWithAnotherCommandAfterBlockedMovement() {
        Field field = new Field(4, 4, Collections.singletonList(cell -> cell.x == 2));
        Tank tank = new Tank(new GridPoint2(1, 1), 0.4f, field);
        List<Direction> choices = new ArrayList<>(Arrays.asList(Direction.RIGHT, Direction.UP));
        TankController controller = new TankController(tank,
                target -> Optional.of(new MoveCommand(target, choices.remove(0))));

        controller.handle();
        assertEquals(new GridPoint2(1, 1), tank.getDestinationCoordinates());

        controller.handle();

        assertTrue(tank.isMoving());
        assertEquals(new GridPoint2(1, 2), tank.getDestinationCoordinates());
        assertTrue(choices.isEmpty());
    }

    private static class RecordingTarget implements MovementTarget {

        private boolean moving;
        private final List<Direction> movements = new ArrayList<>();

        @Override
        public boolean isMoving() {
            return moving;
        }

        @Override
        public void tryMove(Direction direction) {
            movements.add(direction);
            moving = true;
        }
    }
}
