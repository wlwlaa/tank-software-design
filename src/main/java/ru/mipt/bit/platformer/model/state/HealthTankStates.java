package ru.mipt.bit.platformer.model.state;

/** Domain: health transition policy shared by the concrete States; not a port or adapter. */
public final class HealthTankStates {

    private HealthTankStates() {
    }

    /**
     * Zero initial health is treated as heavy, avoiding an undefined health ratio.
     *
     * @param health current health
     * @param maxHealth initial health
     * @return the State matching the health thresholds
     */
    public static TankState forHealth(int health, int maxHealth) {
        if (maxHealth <= 0 || (long) health * 100 < (long) maxHealth * 15) {
            return HeavyTankState.INSTANCE;
        }
        if ((long) health * 100 <= (long) maxHealth * 70) {
            return MediumTankState.INSTANCE;
        }
        return LightTankState.INSTANCE;
    }
}
