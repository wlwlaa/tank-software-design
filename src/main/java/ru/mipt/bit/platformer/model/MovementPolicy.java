package ru.mipt.bit.platformer.model;

import com.badlogic.gdx.math.GridPoint2;

/**
 * Domain: internal movement-policy interface consumed by Tank.
 * Implemented by Field and TankWorld; these are domain services, not external I/O adapters.
 * This abstraction separates domain responsibilities rather than crossing an infrastructure boundary.
 */
public interface MovementPolicy {

    boolean canMoveTo(GridPoint2 coordinates);
}
