package ru.mipt.bit.platformer.control;

import ru.mipt.bit.platformer.input.InputAction;
import ru.mipt.bit.platformer.model.MovementTarget;

import java.util.Objects;
import java.util.Optional;

public class TankController implements InputAction {

    private final MovementTarget target;
    private final MovementStrategy strategy;

    public TankController(MovementTarget target, MovementStrategy strategy) {
        this.target = Objects.requireNonNull(target);
        this.strategy = Objects.requireNonNull(strategy);
    }

    @Override
    public void handle() {
        nextCommand().ifPresent(Command::execute);
    }

    public Optional<Command> nextCommand() {
        if (!target.isActive()) {
            return Optional.empty();
        }
        return target.isMoving()
                ? strategy.nextCommandWhileMoving(target)
                : strategy.nextCommand(target);
    }
}
