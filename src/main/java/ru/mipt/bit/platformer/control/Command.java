package ru.mipt.bit.platformer.control;

/**
 * Application: internal Command pattern contract, not an infrastructure port.
 * Encapsulates an action selected by a controller or an input adapter.
 */
public interface Command {

    void execute();
}
