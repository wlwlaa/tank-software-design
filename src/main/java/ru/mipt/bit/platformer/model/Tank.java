package ru.mipt.bit.platformer.model;

import com.badlogic.gdx.math.GridPoint2;
import ru.mipt.bit.platformer.game.Updatable;

import java.util.Objects;

import static com.badlogic.gdx.math.MathUtils.isEqual;

public class Tank implements MovementTarget, Updatable {

    private final float movementDuration;
    private final MovementPolicy movementPolicy;

    private final GridPoint2 coordinates;
    private final GridPoint2 destinationCoordinates;

    private Direction direction = Direction.RIGHT;
    private float movementProgress = 1f;

    public Tank(GridPoint2 initialCoordinates, float movementDuration, MovementPolicy movementPolicy) {
        if (!Float.isFinite(movementDuration) || movementDuration <= 0f) {
            throw new IllegalArgumentException("Movement duration must be finite and positive");
        }
        this.movementDuration = movementDuration;
        this.movementPolicy = Objects.requireNonNull(movementPolicy);
        coordinates = new GridPoint2(initialCoordinates);
        destinationCoordinates = new GridPoint2(initialCoordinates);
    }

    @Override
    public boolean isMoving() {
        return !isEqual(movementProgress, 1f);
    }

    @Override
    public void tryMove(Direction newDirection) {
        if (isMoving()) {
            return;
        }
        direction = newDirection;
        GridPoint2 destination = direction.move(coordinates);
        if (movementPolicy.canMoveTo(destination)) {
            destinationCoordinates.set(destination);
            movementProgress = 0f;
        }
    }

    @Override
    public void update(float deltaTime) {
        movementProgress = Math.min(1f, Math.max(0f, movementProgress + deltaTime / movementDuration));
        if (!isMoving()) {
            coordinates.set(destinationCoordinates);
        }
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
        return movementProgress;
    }
}
