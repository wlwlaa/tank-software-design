package ru.mipt.bit.platformer.model.state;

/** Domain: concrete State below 15% health; one third movement speed and no shooting. */
public final class HeavyTankState implements TankState {

    public static final HeavyTankState INSTANCE = new HeavyTankState();

    private HeavyTankState() {
    }

    @Override
    public double movementSpeedMultiplier() {
        return 1d / 3d;
    }

    @Override
    public boolean canShoot() {
        return false;
    }
}
