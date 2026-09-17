package ru.mipt.bit.platformer.model;

import com.badlogic.gdx.math.GridPoint2;

import static com.badlogic.gdx.math.MathUtils.isEqual;
import static ru.mipt.bit.platformer.util.GdxGameUtils.continueProgress;

public class Tank {

    private static final float MOVEMENT_SPEED = 0.4f;

    private final GridPoint2 coordinates;
    private final GridPoint2 destinationCoordinates;

    private Direction direction = Direction.RIGHT;
    private float movementProgress = 1f;

    public Tank(GridPoint2 initialCoordinates) {
        coordinates = new GridPoint2(initialCoordinates);
        destinationCoordinates = new GridPoint2(initialCoordinates);
    }

    public boolean isMoving() {
        return !isEqual(movementProgress, 1f);
    }

    public void tryMove(Direction newDirection, Field field) {
        direction = newDirection;
        GridPoint2 destination = direction.move(coordinates);
        if (field.canMoveTo(destination)) {
            destinationCoordinates.set(destination);
            movementProgress = 0f;
        }
    }

    public void update(float deltaTime, Field field) {
        movementProgress = continueProgress(movementProgress, deltaTime, MOVEMENT_SPEED);
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
