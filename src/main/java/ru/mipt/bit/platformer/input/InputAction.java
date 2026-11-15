package ru.mipt.bit.platformer.input;

/**
 * Application output port for the control-processing phase called by GameSession.
 * Implemented by input adapters and in-process application services.
 */
public interface InputAction {

    void handle();
}
