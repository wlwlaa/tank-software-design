package ru.mipt.bit.platformer.control;

import ru.mipt.bit.platformer.model.MovementTarget;

import java.util.Optional;

public interface MovementStrategy {

    Optional<Command> nextCommand(MovementTarget target);

    default Optional<Command> nextCommandWhileMoving(MovementTarget target) {
        return Optional.empty();
    }
}
