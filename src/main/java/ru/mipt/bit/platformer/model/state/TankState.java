package ru.mipt.bit.platformer.model.state;

/**
 * Domain: State pattern behaviour of a tank, not an infrastructure port.
 * Implementations are immutable; speed multipliers must be finite and positive.
 * A booster can override both multipliers and the transition after damage
 * without changing the tank, its commands or its controller.
 */
public interface TankState {

    default double movementSpeedMultiplier() {
        return 1d;
    }

    default double reloadSpeedMultiplier() {
        return 1d;
    }

    default boolean canShoot() {
        return true;
    }

    /**
     * By default, damage replaces the current state with the health-based state.
     *
     * @param health current health after damage
     * @param maxHealth initial health
     * @return the State to apply after the health change
     */
    default TankState onHealthChanged(int health, int maxHealth) {
        return HealthTankStates.forHealth(health, maxHealth);
    }
}
