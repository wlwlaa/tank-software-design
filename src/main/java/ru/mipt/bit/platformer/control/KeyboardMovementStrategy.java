package ru.mipt.bit.platformer.control;

import ru.mipt.bit.platformer.input.DirectionKeyBinding;
import ru.mipt.bit.platformer.input.KeyState;
import ru.mipt.bit.platformer.model.MovementTarget;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class KeyboardMovementStrategy implements MovementStrategy {

    private final KeyState keyState;
    private final List<DirectionKeyBinding> bindings;

    public KeyboardMovementStrategy(KeyState keyState, List<DirectionKeyBinding> bindings) {
        this.keyState = Objects.requireNonNull(keyState);
        this.bindings = new ArrayList<>(bindings);
    }

    @Override
    public Optional<Command> nextCommand(MovementTarget target) {
        for (DirectionKeyBinding binding : bindings) {
            if (binding.isPressed(keyState)) {
                return Optional.of(new MoveCommand(target, binding.getDirection()));
            }
        }
        return Optional.empty();
    }
}
