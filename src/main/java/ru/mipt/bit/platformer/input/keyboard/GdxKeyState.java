package ru.mipt.bit.platformer.input.keyboard;

import com.badlogic.gdx.Input;
import ru.mipt.bit.platformer.input.KeyState;

/**
 * Infrastructure: driven input-device adapter implementing the application KeyState port.
 * Delegates key queries to LibGDX Input.
 */
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
