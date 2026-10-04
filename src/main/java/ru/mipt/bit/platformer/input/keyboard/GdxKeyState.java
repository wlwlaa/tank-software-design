package ru.mipt.bit.platformer.input.keyboard;

import com.badlogic.gdx.Input;
import ru.mipt.bit.platformer.input.KeyState;

public class GdxKeyState implements KeyState {

    private final Input input;

    public GdxKeyState(Input input) {
        this.input = input;
    }

    @Override
    public boolean isPressed(int key) {
        return input.isKeyPressed(key);
    }
}
