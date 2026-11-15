package ru.mipt.bit.platformer.model.state;

/** Domain: concrete State for 15-70% health inclusive; half movement speed. */
public final class MediumTankState implements TankState {

    public static final MediumTankState INSTANCE = new MediumTankState();

    private MediumTankState() {
    }

    @Override
    public double movementSpeedMultiplier() {
        return 0.5d;
    }
}
