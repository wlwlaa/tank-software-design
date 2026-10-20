package ru.mipt.bit.platformer.control;

import org.junit.Test;
import ru.mipt.bit.platformer.model.Direction;
import ru.mipt.bit.platformer.model.MovementTarget;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class MoveCommandTest {

    @Test
    public void defersMovementUntilExecutionAndKeepsItsTarget() {
        List<Direction> movements = new ArrayList<>();
        MovementTarget target = new MovementTarget() {
            @Override
            public boolean isMoving() {
                return false;
            }

            @Override
            public void tryMove(Direction direction) {
                movements.add(direction);
            }
        };

        Command command = new MoveCommand(target, Direction.LEFT);

        assertTrue(movements.isEmpty());
        command.execute();
        assertEquals(Collections.singletonList(Direction.LEFT), movements);
    }
}
