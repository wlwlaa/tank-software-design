package ru.mipt.bit.platformer.input;

import java.util.ArrayList;
import java.util.List;

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
