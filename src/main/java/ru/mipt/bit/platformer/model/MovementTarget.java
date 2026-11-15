package ru.mipt.bit.platformer.model;

/**
 * Domain input port exposing movement and activity to application commands/controllers.
 * Implemented by Tank; the entity is part of the core, not an infrastructure adapter.
 */
public interface MovementTarget {

    default boolean isActive() {
        return true;
    }

    boolean isMoving();

    void tryMove(Direction direction);
}
