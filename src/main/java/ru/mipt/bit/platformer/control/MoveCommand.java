package ru.mipt.bit.platformer.control;

import ru.mipt.bit.platformer.model.Direction;
import ru.mipt.bit.platformer.model.MovementTarget;

import java.util.Objects;

/**
 * Application: movement use-case command invoking the domain input port MovementTarget.
 * Does not read devices or implement an infrastructure adapter.
 */
public class MoveCommand implements Command {

    private final MovementTarget target;
    private final Direction direction;

    public MoveCommand(MovementTarget target, Direction direction) {
        this.target = Objects.requireNonNull(target);
        this.direction = Objects.requireNonNull(direction);
    }

    @Override
    public void execute() {
        target.tryMove(direction);
    }
}
