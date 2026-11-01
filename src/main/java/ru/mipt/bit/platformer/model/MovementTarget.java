package ru.mipt.bit.platformer.model;

public interface MovementTarget {

    default boolean isActive() {
        return true;
    }

    boolean isMoving();

    void tryMove(Direction direction);
}
