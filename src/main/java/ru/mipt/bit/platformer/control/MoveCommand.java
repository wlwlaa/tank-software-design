package ru.mipt.bit.platformer.control;

import ru.mipt.bit.platformer.model.Direction;
import ru.mipt.bit.platformer.model.MovementTarget;

import java.util.Objects;

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
