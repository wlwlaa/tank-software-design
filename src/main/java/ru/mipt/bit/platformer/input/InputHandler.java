package ru.mipt.bit.platformer.input;

import java.util.ArrayList;
import java.util.List;

/**
 * Infrastructure: composite input adapter implementing the application InputAction port.
 * Combines input actions without exposing device APIs to GameSession.
 */
public class InputHandler implements InputAction {

    private final List<InputAction> actions;

    public InputHandler(List<InputAction> actions) {
        this.actions = new ArrayList<>(actions);
    }

    @Override
    public void handle() {
        for (InputAction action : actions) {
            action.handle();
        }
    }
}
