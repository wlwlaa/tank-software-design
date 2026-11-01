package ru.mipt.bit.platformer.control;

import ru.mipt.bit.platformer.input.DirectionKeyBinding;
import ru.mipt.bit.platformer.input.KeyState;
import ru.mipt.bit.platformer.model.MovementTarget;
import ru.mipt.bit.platformer.model.ShootingTarget;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class KeyboardMovementStrategy implements MovementStrategy {

    private final KeyState keyState;
    private final List<DirectionKeyBinding> bindings;
    private final Integer shootKey;

    public KeyboardMovementStrategy(KeyState keyState, List<DirectionKeyBinding> bindings) {
        this(keyState, bindings, null);
    }

    public KeyboardMovementStrategy(KeyState keyState, List<DirectionKeyBinding> bindings, Integer shootKey) {
        this.keyState = Objects.requireNonNull(keyState);
        this.bindings = new ArrayList<>(bindings);
        this.shootKey = shootKey;
    }

    @Override
    public Optional<Command> nextCommand(MovementTarget target) {
        Optional<Command> shot = nextCommandWhileMoving(target);
        if (shot.isPresent()) {
            return shot;
        }
        for (DirectionKeyBinding binding : bindings) {
            if (binding.isPressed(keyState)) {
                return Optional.of(new MoveCommand(target, binding.getDirection()));
            }
        }
        return Optional.empty();
    }

    @Override
    public Optional<Command> nextCommandWhileMoving(MovementTarget target) {
        if (shootKey != null && keyState.isPressed(shootKey) && target instanceof ShootingTarget) {
            return Optional.of(new ShootCommand((ShootingTarget) target));
        }
        return Optional.empty();
    }
}
