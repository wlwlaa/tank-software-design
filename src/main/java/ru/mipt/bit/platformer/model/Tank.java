package ru.mipt.bit.platformer.model;

import com.badlogic.gdx.math.GridPoint2;
import ru.mipt.bit.platformer.model.state.HealthTankStates;
import ru.mipt.bit.platformer.model.state.TankState;

import java.util.Objects;
import java.util.function.Consumer;

/**
 * Domain entity and State pattern context implementing MovementTarget and ShootingTarget input ports.
 * Owns health, movement, reload and collision rules; delegates state-dependent behaviour to TankState.
 * It is a core implementation of domain contracts, not an infrastructure adapter.
 * Architecture gap: coordinates still expose the framework type GridPoint2.
 */
public class Tank implements MovementTarget, ShootingTarget, Obstacle {

    public static final int DEFAULT_HEALTH = 100;
    public static final float DEFAULT_SHOT_INTERVAL = 0.4f;

    private final float movementDuration;
    private final MovementPolicy movementPolicy;
    private final Consumer<Tank> shootAction;
    private final Consumer<Tank> destroyAction;
    private final float shotInterval;
    private final int maxHealth;

    private final GridPoint2 coordinates;
    private final GridPoint2 destinationCoordinates;

    private Direction direction = Direction.RIGHT;
    private double stateMovementDuration;
    private double movementTime;
    private double reloadTime;
    private TankState state;
    private int health;
    private boolean active = true;

    public Tank(GridPoint2 initialCoordinates, float movementDuration, MovementPolicy movementPolicy) {
        this(initialCoordinates, movementDuration, movementPolicy, tank -> { }, tank -> { },
                DEFAULT_HEALTH, DEFAULT_SHOT_INTERVAL);
    }

    Tank(GridPoint2 initialCoordinates, float movementDuration, MovementPolicy movementPolicy,
         Consumer<Tank> shootAction, Consumer<Tank> destroyAction, int health, float shotInterval) {
        if (!Float.isFinite(movementDuration) || movementDuration <= 0f) {
            throw new IllegalArgumentException("Movement duration must be finite and positive");
        }
        this.movementDuration = movementDuration;
        this.movementPolicy = Objects.requireNonNull(movementPolicy);
        if (health < 0 || !Float.isFinite(shotInterval) || shotInterval <= 0f) {
            throw new IllegalArgumentException("Health must be non-negative and shot interval finite and positive");
        }
        this.shootAction = Objects.requireNonNull(shootAction);
        this.destroyAction = Objects.requireNonNull(destroyAction);
        this.health = health;
        this.maxHealth = health;
        state = HealthTankStates.forHealth(health, maxHealth);
        stateMovementDuration = movementDuration / state.movementSpeedMultiplier();
        movementTime = stateMovementDuration;
        this.shotInterval = shotInterval;
        coordinates = new GridPoint2(initialCoordinates);
        destinationCoordinates = new GridPoint2(initialCoordinates);
    }

    @Override
    public boolean isMoving() {
        return movementTime < stateMovementDuration;
    }

    @Override
    public void tryMove(Direction newDirection) {
        if (!active || isMoving()) {
            return;
        }
        direction = newDirection;
        GridPoint2 destination = direction.move(coordinates);
        if (movementPolicy.canMoveTo(destination)) {
            destinationCoordinates.set(destination);
            movementTime = 0d;
        }
    }

    @Override
    public void update(float deltaTime) {
        if (!Float.isFinite(deltaTime) || deltaTime < 0f) {
            throw new IllegalArgumentException("Delta time must be finite and non-negative");
        }
        if (!active) {
            return;
        }
        // Reload progress is measured in base-speed seconds.
        reloadTime = Math.max(0d, reloadTime - deltaTime * state.reloadSpeedMultiplier());
        // Sum elapsed time before dividing by the state's duration. Multiplying
        // every small tick by 1/3 can otherwise postpone arrival by one frame.
        movementTime = Math.min(stateMovementDuration, movementTime + deltaTime);
        if (!isMoving()) {
            coordinates.set(destinationCoordinates);
        }
    }

    @Override
    public boolean occupies(GridPoint2 coordinates) {
        // Keep the source reserved until the entire step is complete.
        return active && (this.coordinates.equals(coordinates) || destinationCoordinates.equals(coordinates));
    }

    @Override
    public void shoot() {
        if (active && state.canShoot() && reloadTime <= 0f) {
            reloadTime = shotInterval;
            shootAction.accept(this);
        }
    }

    public void takeDamage(int damage) {
        if (damage <= 0) {
            throw new IllegalArgumentException("Damage must be positive");
        }
        if (!active) {
            return;
        }
        health -= damage;
        setState(state.onHealthChanged(health, maxHealth));
        // The assignment defines destruction strictly below zero; zero is still alive.
        if (health < 0) {
            deactivate();
            destroyAction.accept(this);
        }
    }

    void deactivate() {
        active = false;
    }

    @Override
    public boolean isActive() {
        return active;
    }

    public int getHealth() {
        return health;
    }

    public int getMaxHealth() {
        return maxHealth;
    }

    public TankState getState() {
        return state;
    }

    /**
     * Changes behaviour immediately without resetting movement or reload progress.
     * Allows field effects to supply new States; an inactive tank stays inactive.
     *
     * @param state immutable behaviour with finite, positive speed multipliers
     */
    public void setState(TankState state) {
        Objects.requireNonNull(state, "state");
        double movementSpeed = state.movementSpeedMultiplier();
        double reloadSpeed = state.reloadSpeedMultiplier();
        if (!Double.isFinite(movementSpeed) || movementSpeed <= 0d
                || !Double.isFinite(reloadSpeed) || reloadSpeed <= 0d) {
            throw new IllegalArgumentException("State speed multipliers must be finite and positive");
        }
        double nextMovementDuration = movementDuration / movementSpeed;
        if (!Double.isFinite(nextMovementDuration) || nextMovementDuration <= 0d) {
            throw new IllegalArgumentException("State movement duration must be finite and positive");
        }
        movementTime = isMoving() ? movementTime / stateMovementDuration * nextMovementDuration
                : nextMovementDuration;
        stateMovementDuration = nextMovementDuration;
        this.state = state;
    }

    public GridPoint2 getCoordinates() {
        return new GridPoint2(coordinates);
    }

    public GridPoint2 getDestinationCoordinates() {
        return new GridPoint2(destinationCoordinates);
    }

    public Direction getDirection() {
        return direction;
    }

    public float getMovementProgress() {
        return isMoving() ? Math.min(Math.nextDown(1f), (float) (movementTime / stateMovementDuration)) : 1f;
    }
}
