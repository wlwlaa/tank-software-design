package ru.mipt.bit.platformer.level;

import com.badlogic.gdx.math.GridPoint2;
import ru.mipt.bit.platformer.model.Field;
import ru.mipt.bit.platformer.model.Obstacle;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public final class LevelDefinition {

    private final int width;
    private final int height;
    private final List<Obstacle> obstacles;
    private final GridPoint2 playerStart;

    public LevelDefinition(int width, int height, List<? extends Obstacle> obstacles,
                           GridPoint2 playerStart) {
        if (width <= 0 || height <= 0) {
            throw new IllegalArgumentException("Level width and height must be positive");
        }
        Objects.requireNonNull(obstacles, "obstacles");
        this.obstacles = Collections.unmodifiableList(new ArrayList<>(obstacles));
        for (Obstacle obstacle : this.obstacles) {
            Objects.requireNonNull(obstacle, "obstacle");
        }
        this.playerStart = new GridPoint2(Objects.requireNonNull(playerStart, "playerStart"));
        if (!new Field(width, height, this.obstacles).canMoveTo(this.playerStart)) {
            throw new IllegalArgumentException("Player start must be a free cell inside the level");
        }
        this.width = width;
        this.height = height;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public List<Obstacle> getObstacles() {
        return obstacles;
    }

    public GridPoint2 getPlayerStart() {
        return new GridPoint2(playerStart);
    }
}
