package ru.mipt.bit.platformer.render;

/**
 * Infrastructure: shared presentation setting, not a domain entity or an existing port adapter.
 * Architecture gap: ToggleHealthBarsCommand should reach it through an application-owned port.
 *
 * <p>Shared display setting for existing and newly added tanks.
 */
public final class HealthBarVisibility {

    private boolean visible;

    public boolean isVisible() {
        return visible;
    }

    public void toggle() {
        visible = !visible;
    }
}
