package ru.mipt.bit.platformer;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.math.GridPoint2;

enum Direction {
    UP(0, 1, 90f, Keys.UP, Keys.W),
    LEFT(-1, 0, -180f, Keys.LEFT, Keys.A),
    DOWN(0, -1, -90f, Keys.DOWN, Keys.S),
    RIGHT(1, 0, 0f, Keys.RIGHT, Keys.D);

    private final int deltaX;
    private final int deltaY;
    private final float rotation;
    private final int arrowKey;
    private final int letterKey;

    Direction(int deltaX, int deltaY, float rotation, int arrowKey, int letterKey) {
        this.deltaX = deltaX;
        this.deltaY = deltaY;
        this.rotation = rotation;
        this.arrowKey = arrowKey;
        this.letterKey = letterKey;
    }

    GridPoint2 move(GridPoint2 coordinates) {
        return new GridPoint2(coordinates).add(deltaX, deltaY);
    }

    float getRotation() {
        return rotation;
    }

    boolean isPressed() {
        return Gdx.input.isKeyPressed(arrowKey) || Gdx.input.isKeyPressed(letterKey);
    }
}
