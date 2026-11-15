package ru.mipt.bit.platformer.input;

import ru.mipt.bit.platformer.control.Command;

import java.util.Objects;

/**
 * Infrastructure: input adapter implementing InputAction and reading the KeyState port.
 * Translates a key-press edge into an application Command.
 *
 * <p>Executes once per press, requiring a release before the next execution.
 */
public final class KeyPressCommandInputAction implements InputAction {

    private final KeyState keyState;
    private final int key;
    private final Command command;
    private boolean previouslyPressed;

    public KeyPressCommandInputAction(KeyState keyState, int key, Command command) {
        this.keyState = Objects.requireNonNull(keyState);
        this.key = key;
        this.command = Objects.requireNonNull(command);
    }

    @Override
    public void handle() {
        boolean pressed = keyState.isPressed(key);
        boolean justPressed = pressed && !previouslyPressed;
        previouslyPressed = pressed;
        if (justPressed) {
            command.execute();
        }
    }
}
