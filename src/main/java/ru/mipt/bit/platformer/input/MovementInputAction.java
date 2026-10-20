package ru.mipt.bit.platformer.input;

import ru.mipt.bit.platformer.control.KeyboardMovementStrategy;
import ru.mipt.bit.platformer.control.TankController;
import ru.mipt.bit.platformer.model.MovementTarget;

import java.util.List;

public class MovementInputAction implements InputAction {

    private final TankController controller;

    public MovementInputAction(MovementTarget target, KeyState keyState, List<DirectionKeyBinding> bindings) {
        controller = new TankController(target, new KeyboardMovementStrategy(keyState, bindings));
    }

    @Override
    public void handle() {
        controller.handle();
    }
}
