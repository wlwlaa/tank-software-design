package ru.mipt.bit.platformer.render;

/** Shared display setting for existing and newly added tanks. */
public final class HealthBarVisibility {

    private boolean visible;

    public boolean isVisible() {
        return visible;
    }

    public void toggle() {
        visible = !visible;
    }
}
