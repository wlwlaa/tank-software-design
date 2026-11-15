package ru.mipt.bit.platformer.input;

/**
 * Application output port for querying input-device state without accessing LibGDX.
 * The infrastructure adapter is GdxKeyState; tests provide in-memory substitutes.
 */
public interface KeyState {

    boolean isPressed(int key);
}
