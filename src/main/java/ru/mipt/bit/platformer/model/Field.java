package ru.mipt.bit.platformer.model;

import com.badlogic.gdx.math.GridPoint2;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Field {

    private final int width;
    private final int height;
    private final List<Tree> trees;

    public Field(int width, int height, List<Tree> trees) {
        this.width = width;
        this.height = height;
        this.trees = Collections.unmodifiableList(new ArrayList<>(trees));
    }

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
        for (Tree tree : trees) {
            if (tree.occupies(coordinates)) {
                return true;
            }
        }
        return false;
    }

    public List<Tree> getTrees() {
        return trees;
    }
}
