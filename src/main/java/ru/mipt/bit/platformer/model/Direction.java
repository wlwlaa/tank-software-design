package ru.mipt.bit.platformer.model;

import com.badlogic.gdx.math.GridPoint2;

/**
 * Domain: direction value used by movement and shooting; not a port or adapter.
 * Architecture gap: rendering angles and GridPoint2 expose presentation/framework concerns.
 */
public enum Direction {
    UP(0, 1, 90f),
    LEFT(-1, 0, -180f),
    DOWN(0, -1, -90f),
    RIGHT(1, 0, 0f);

    private final int deltaX;
    private final int deltaY;
    private final float rotation;

    Direction(int deltaX, int deltaY, float rotation) {
        this.deltaX = deltaX;
        this.deltaY = deltaY;
        this.rotation = rotation;
    }

    public GridPoint2 move(GridPoint2 coordinates) {
        return new GridPoint2(coordinates).add(deltaX, deltaY);
    }

    public float getRotation() {
        return rotation;
    }
}
