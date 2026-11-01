package ru.mipt.bit.platformer.model;

import com.badlogic.gdx.math.GridPoint2;

public final class Bullet implements LevelObject {

    private final TankWorld world;
    private final Tank owner;
    private final Direction direction;
    private final BulletSettings settings;
    private final GridPoint2 coordinates;
    private double elapsed;

    Bullet(TankWorld world, Tank owner, BulletSettings settings) {
        this.world = world;
        this.owner = owner;
        this.direction = owner.getDirection();
        this.settings = settings;
        coordinates = direction.move(owner.getCoordinates());
    }

    @Override
    public void update(float deltaTime) {
        if (!Float.isFinite(deltaTime) || deltaTime < 0f) {
            throw new IllegalArgumentException("Delta time must be finite and non-negative");
        }
        elapsed += deltaTime;
        while (world.contains(this) && elapsed >= settings.getMovementDuration()) {
            elapsed -= settings.getMovementDuration();
            coordinates.set(direction.move(coordinates));
            world.resolveCollision(this);
        }
    }

    void collideWith(LevelObject object) {
        world.removeObject(this);
        if (object instanceof Tank) {
            ((Tank) object).takeDamage(settings.getDamage());
        } else if (object instanceof Bullet) {
            world.removeObject(object);
        }
    }

    double timeUntilNextCell() {
        return settings.getMovementDuration() - elapsed;
    }

    Tank getOwner() {
        return owner;
    }

    @Override
    public boolean occupies(GridPoint2 coordinates) {
        return this.coordinates.equals(coordinates);
    }

    public GridPoint2 getCoordinates() {
        return new GridPoint2(coordinates);
    }

    public GridPoint2 getDestinationCoordinates() {
        return direction.move(coordinates);
    }

    public Direction getDirection() {
        return direction;
    }

    public float getMovementProgress() {
        return (float) (elapsed / settings.getMovementDuration());
    }

    public int getDamage() {
        return settings.getDamage();
    }
}
