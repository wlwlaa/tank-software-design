package ru.mipt.bit.platformer.model;

/**
 * Domain input port exposing shooting to the application ShootCommand.
 * Implemented by Tank, which owns state-dependent firing permission and reload rules.
 */
public interface ShootingTarget {

    void shoot();
}
