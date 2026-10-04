package ru.mipt.bit.platformer.model;

public interface MovementTarget {

    boolean isMoving();

    void tryMove(Direction direction);
}
