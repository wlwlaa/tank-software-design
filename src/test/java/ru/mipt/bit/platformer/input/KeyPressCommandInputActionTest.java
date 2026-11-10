package ru.mipt.bit.platformer.input;

import org.junit.Test;
import ru.mipt.bit.platformer.control.ToggleHealthBarsCommand;
import ru.mipt.bit.platformer.render.HealthBarVisibility;

import static org.junit.Assert.*;

public class KeyPressCommandInputActionTest {

    @Test
    public void togglesOnlyOncePerPressAndAgainAfterRelease() {
        boolean[] pressed = {false};
        HealthBarVisibility visibility = new HealthBarVisibility();
        KeyPressCommandInputAction input = new KeyPressCommandInputAction(
                key -> key == 42 && pressed[0], 42, new ToggleHealthBarsCommand(visibility));

        input.handle();
        assertFalse(visibility.isVisible());
        pressed[0] = true;
        input.handle();
        assertTrue(visibility.isVisible());
        input.handle();
        input.handle();
        assertTrue(visibility.isVisible());
        pressed[0] = false;
        input.handle();
        assertTrue(visibility.isVisible());
        pressed[0] = true;
        input.handle();
        assertFalse(visibility.isVisible());
    }

    @Test
    public void doesNotReactToOtherKeys() {
        int[] executions = {0};
        KeyPressCommandInputAction input = new KeyPressCommandInputAction(
                key -> key == 7, 42, () -> executions[0]++);

        input.handle();
        assertEquals(0, executions[0]);
    }

    @Test
    public void commandChangesVisibilityOnlyWhenExecuted() {
        HealthBarVisibility visibility = new HealthBarVisibility();
        ToggleHealthBarsCommand command = new ToggleHealthBarsCommand(visibility);

        assertFalse(visibility.isVisible());
        command.execute();
        assertTrue(visibility.isVisible());
        command.execute();
        assertFalse(visibility.isVisible());
    }
}
