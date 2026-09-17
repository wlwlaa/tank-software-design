package ru.mipt.bit.platformer.input;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;

import java.util.Arrays;
import java.util.List;
import ru.mipt.bit.platformer.model.Direction;
import ru.mipt.bit.platformer.model.Field;
import ru.mipt.bit.platformer.model.Tank;

public class MovementInputAction implements InputAction {

    private final Tank tank;
    private final Field field;
    private final List<DirectionKeyBinding> bindings = Arrays.asList(
            new DirectionKeyBinding(Direction.UP, Keys.UP, Keys.W),
            new DirectionKeyBinding(Direction.LEFT, Keys.LEFT, Keys.A),
            new DirectionKeyBinding(Direction.DOWN, Keys.DOWN, Keys.S),
            new DirectionKeyBinding(Direction.RIGHT, Keys.RIGHT, Keys.D)
    );

    public MovementInputAction(Tank tank, Field field) {
        this.tank = tank;
        this.field = field;
    }

    @Override
    public void handle() {
        if (tank.isMoving()) {
            return;
        }

        for (DirectionKeyBinding binding : bindings) {
            if (binding.isPressed()) {
                tank.tryMove(binding.direction, field);
                return;
            }
        }
    }

    private static class DirectionKeyBinding {

        private final Direction direction;
        private final int[] keys;

        private DirectionKeyBinding(Direction direction, int... keys) {
            this.direction = direction;
            this.keys = keys;
        }

        private boolean isPressed() {
            for (int key : keys) {
                if (Gdx.input.isKeyPressed(key)) {
                    return true;
                }
            }
            return false;
        }
    }
}
