package ru.mipt.bit.platformer.model;

import com.badlogic.gdx.math.GridPoint2;

import java.util.Objects;
import java.util.function.Consumer;

public class Tank implements MovementTarget, ShootingTarget, Obstacle {

    public static final int DEFAULT_HEALTH = 100;
    public static final float DEFAULT_SHOT_INTERVAL = 0.4f;

    private final float movementDuration;
    private final MovementPolicy movementPolicy;
    private final Consumer<Tank> shootAction;
    private final Consumer<Tank> destroyAction;
    private final float shotInterval;

    private final GridPoint2 coordinates;
    private final GridPoint2 destinationCoordinates;

    private Direction direction = Direction.RIGHT;
    private double movementTime;
    private double reloadTime;
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
        movementTime = movementDuration;
        this.movementPolicy = Objects.requireNonNull(movementPolicy);
        if (health < 0 || !Float.isFinite(shotInterval) || shotInterval <= 0f) {
            throw new IllegalArgumentException("Health must be non-negative and shot interval finite and positive");
        }
        this.shootAction = Objects.requireNonNull(shootAction);
        this.destroyAction = Objects.requireNonNull(destroyAction);
        this.health = health;
        this.shotInterval = shotInterval;
        coordinates = new GridPoint2(initialCoordinates);
        destinationCoordinates = new GridPoint2(initialCoordinates);
    }

    @Override
    public boolean isMoving() {
        return movementTime < movementDuration;
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
        reloadTime = Math.max(0d, reloadTime - deltaTime);
        movementTime = Math.min(movementDuration, movementTime + deltaTime);
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
        if (active && reloadTime <= 0f) {
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
        return isMoving() ? Math.min(Math.nextDown(1f), (float) (movementTime / movementDuration)) : 1f;
    }
}
