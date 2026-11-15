package ru.mipt.bit.platformer.control;

import ru.mipt.bit.platformer.model.ShootingTarget;

import java.util.Objects;

/**
 * Application: shooting use-case command invoking the domain input port ShootingTarget.
 * Permission to fire is checked by the domain entity when the command executes.
 */
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
