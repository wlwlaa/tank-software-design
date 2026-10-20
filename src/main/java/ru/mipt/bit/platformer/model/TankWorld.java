package ru.mipt.bit.platformer.model;

import com.badlogic.gdx.math.GridPoint2;
import ru.mipt.bit.platformer.game.Updatable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public final class TankWorld implements MovementPolicy, Updatable {

    private final Field field;
    private final List<Tank> tanks = new ArrayList<>();
    private final List<Tank> tankView = Collections.unmodifiableList(tanks);

    public TankWorld(Field field) {
        this.field = Objects.requireNonNull(field, "field");
    }

    public Tank addTank(GridPoint2 coordinates, float movementDuration) {
        Objects.requireNonNull(coordinates, "coordinates");
        if (!canMoveTo(coordinates)) {
            throw new IllegalArgumentException("Tank start must be a free cell inside the level");
        }
        Tank tank = new Tank(coordinates, movementDuration, this);
        tanks.add(tank);
        return tank;
    }

    @Override
    public boolean canMoveTo(GridPoint2 coordinates) {
        if (!field.canMoveTo(coordinates)) {
            return false;
        }
        for (Tank tank : tanks) {
            if (tank.occupies(coordinates)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public void update(float deltaTime) {
        for (Tank tank : tanks) {
            tank.update(deltaTime);
        }
    }

    public int getWidth() {
        return field.getWidth();
    }

    public int getHeight() {
        return field.getHeight();
    }

    public List<Tank> getTanks() {
        return tankView;
    }
}
