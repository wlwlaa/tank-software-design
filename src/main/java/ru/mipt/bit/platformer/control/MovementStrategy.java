package ru.mipt.bit.platformer.control;

import ru.mipt.bit.platformer.model.MovementTarget;

import java.util.Optional;

/**
 * Application: internal Strategy contract for selecting commands, not an external I/O port.
 * Device access is abstracted separately by the KeyState port.
 */
public interface MovementStrategy {

    Optional<Command> nextCommand(MovementTarget target);

    default Optional<Command> nextCommandWhileMoving(MovementTarget target) {
        return Optional.empty();
    }
}
