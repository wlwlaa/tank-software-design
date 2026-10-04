package ru.mipt.bit.platformer.input;

import ru.mipt.bit.platformer.model.Direction;

public class DirectionKeyBinding {

    private final Direction direction;
    private final int[] keys;

    public DirectionKeyBinding(Direction direction, int... keys) {
        this.direction = direction;
        this.keys = keys.clone();
    }

    public Direction getDirection() {
        return direction;
    }

    public boolean isPressed(KeyState keyState) {
        for (int key : keys) {
            if (keyState.isPressed(key)) {
                return true;
            }
        }
        return false;
    }
}
