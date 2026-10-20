package ru.mipt.bit.platformer.control;

import ru.mipt.bit.platformer.model.MovementTarget;

import java.util.Optional;

public interface MovementStrategy {

    Optional<Command> nextCommand(MovementTarget target);
}
