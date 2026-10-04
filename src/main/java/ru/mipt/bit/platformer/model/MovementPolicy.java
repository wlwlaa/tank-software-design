package ru.mipt.bit.platformer.model;

import com.badlogic.gdx.math.GridPoint2;

public interface MovementPolicy {

    boolean canMoveTo(GridPoint2 coordinates);
}
