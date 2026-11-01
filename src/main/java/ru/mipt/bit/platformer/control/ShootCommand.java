package ru.mipt.bit.platformer.control;

import ru.mipt.bit.platformer.model.ShootingTarget;

import java.util.Objects;

public final class ShootCommand implements Command {

    private final ShootingTarget target;

    public ShootCommand(ShootingTarget target) {
        this.target = Objects.requireNonNull(target);
    }

    @Override
    public void execute() {
        target.shoot();
    }
}
