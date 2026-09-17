package ru.mipt.bit.platformer.input;

import java.util.ArrayList;
import java.util.List;

public class InputHandler {

    private final List<InputAction> actions;

    public InputHandler(List<InputAction> actions) {
        this.actions = new ArrayList<>(actions);
    }

    public void handleInput() {
        for (InputAction action : actions) {
            action.handle();
        }
    }
}
