package ru.mipt.bit.platformer.model;

import com.badlogic.gdx.math.GridPoint2;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Field implements MovementPolicy {

    private final int width;
    private final int height;
    private final List<Obstacle> obstacles;

    public Field(int width, int height, List<? extends Obstacle> obstacles) {
        if (width <= 0 || height <= 0) {
            throw new IllegalArgumentException("Field width and height must be positive");
        }
        this.width = width;
        this.height = height;
        this.obstacles = Collections.unmodifiableList(new ArrayList<>(obstacles));
    }

    @Override
    public boolean canMoveTo(GridPoint2 coordinates) {
        return isInside(coordinates) && !isOccupied(coordinates);
    }

    private boolean isInside(GridPoint2 coordinates) {
        return coordinates.x >= 0
                && coordinates.x < width
                && coordinates.y >= 0
                && coordinates.y < height;
    }

    private boolean isOccupied(GridPoint2 coordinates) {
        for (Obstacle obstacle : obstacles) {
            if (obstacle.occupies(coordinates)) {
                return true;
            }
        }
        return false;
    }

    public List<Obstacle> getObstacles() {
        return obstacles;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }
}
