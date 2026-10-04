package ru.mipt.bit.platformer.input;

import ru.mipt.bit.platformer.model.MovementTarget;

import java.util.ArrayList;
import java.util.List;

public class MovementInputAction implements InputAction {

    private final MovementTarget target;
    private final KeyState keyState;
    private final List<DirectionKeyBinding> bindings;

    public MovementInputAction(MovementTarget target, KeyState keyState, List<DirectionKeyBinding> bindings) {
        this.target = target;
        this.keyState = keyState;
        this.bindings = new ArrayList<>(bindings);
    }

    @Override
    public void handle() {
        if (target.isMoving()) {
            return;
        }
        for (DirectionKeyBinding binding : bindings) {
            if (binding.isPressed(keyState)) {
                target.tryMove(binding.getDirection());
                return;
            }
        }
    }
}
