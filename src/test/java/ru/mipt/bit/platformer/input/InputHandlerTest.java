package ru.mipt.bit.platformer.input;

import org.junit.Test;

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.assertEquals;

public class InputHandlerTest {

    @Test
    public void delegatesInputHandlingToEveryRegisteredAction() {
        AtomicInteger calls = new AtomicInteger();
        InputAction first = calls::incrementAndGet;
        InputAction second = calls::incrementAndGet;
        InputHandler handler = new InputHandler(Arrays.asList(first, second));

        handler.handleInput();

        assertEquals(2, calls.get());
    }
}
