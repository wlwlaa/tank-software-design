package ru.mipt.bit.platformer.control;

import ru.mipt.bit.platformer.input.InputAction;
import ru.mipt.bit.platformer.model.MovementTarget;

import java.util.Objects;

public class TankController implements InputAction {

    private final MovementTarget target;
    private final MovementStrategy strategy;

    public TankController(MovementTarget target, MovementStrategy strategy) {
        this.target = Objects.requireNonNull(target);
        this.strategy = Objects.requireNonNull(strategy);
    }

    @Override
    public void handle() {
        if (!target.isMoving()) {
            strategy.nextCommand(target).ifPresent(Command::execute);
        }
    }
}
