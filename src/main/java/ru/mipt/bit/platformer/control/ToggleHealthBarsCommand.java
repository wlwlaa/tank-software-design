package ru.mipt.bit.platformer.control;

import ru.mipt.bit.platformer.render.HealthBarVisibility;

import java.util.Objects;

/** Command receiver is the shared health bar display setting. */
public final class ToggleHealthBarsCommand implements Command {

    private final HealthBarVisibility visibility;

    public ToggleHealthBarsCommand(HealthBarVisibility visibility) {
        this.visibility = Objects.requireNonNull(visibility);
    }

    @Override
    public void execute() {
        visibility.toggle();
    }
}
