package ru.mipt.bit.platformer.model.state;

/** Domain: concrete State for health strictly above 70%; normal movement and shooting. */
public final class LightTankState implements TankState {

    public static final LightTankState INSTANCE = new LightTankState();

    private LightTankState() {
    }
}
