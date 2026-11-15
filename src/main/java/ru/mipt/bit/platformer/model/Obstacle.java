package ru.mipt.bit.platformer.model;

/**
 * Domain: internal capability interface marking objects that block movement.
 * Implemented by Tank and Tree; not an external-system port.
 *
 * <p>A level object that blocks tank movement. Bullets are not obstacles.
 */
public interface Obstacle extends LevelObject {
}
