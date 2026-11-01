package ru.mipt.bit.platformer.model;

import com.badlogic.gdx.math.GridPoint2;
import ru.mipt.bit.platformer.game.Updatable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public final class TankWorld implements MovementPolicy, Updatable {

    private final Field field;
    private final int initialHealth;
    private final BulletSettings bulletSettings;
    private final float shotInterval;
    private final List<LevelObject> objects = new ArrayList<>();
    private final List<LevelObject> objectView = Collections.unmodifiableList(objects);
    private final List<Tank> tanks = new ArrayList<>();
    private final List<Tank> tankView = Collections.unmodifiableList(tanks);
    private final List<Bullet> bullets = new ArrayList<>();
    private final List<Bullet> bulletView = Collections.unmodifiableList(bullets);
    private final List<WorldListener> listeners = new ArrayList<>();

    public TankWorld(Field field) {
        this(field, Tank.DEFAULT_HEALTH, BulletSettings.DEFAULT, Tank.DEFAULT_SHOT_INTERVAL);
    }

    public TankWorld(Field field, int initialHealth, BulletSettings bulletSettings, float shotInterval) {
        this.field = Objects.requireNonNull(field, "field");
        if (initialHealth < 0 || !Float.isFinite(shotInterval) || shotInterval <= 0f) {
            throw new IllegalArgumentException("Health must be non-negative and shot interval finite and positive");
        }
        this.initialHealth = initialHealth;
        this.bulletSettings = Objects.requireNonNull(bulletSettings, "bulletSettings");
        this.shotInterval = shotInterval;
        objects.addAll(field.getObstacles());
    }

    public Tank addTank(GridPoint2 coordinates, float movementDuration) {
        Objects.requireNonNull(coordinates, "coordinates");
        if (!canMoveTo(coordinates)) {
            throw new IllegalArgumentException("Tank start must be a free cell inside the level");
        }
        Tank tank = new Tank(coordinates, movementDuration, this, this::fire, this::removeObject,
                initialHealth, shotInterval);
        tanks.add(tank);
        addObject(tank);
        return tank;
    }

    private void fire(Tank tank) {
        if (!contains(tank)) {
            return;
        }
        Bullet bullet = new Bullet(this, tank, bulletSettings);
        bullets.add(bullet);
        addObject(bullet);
        // Resolve the muzzle cell too: a neighbouring tank/tree must not be skipped.
        resolveCollision(bullet);
    }

    private void addObject(LevelObject object) {
        objects.add(object);
        for (WorldListener listener : new ArrayList<>(listeners)) {
            listener.objectAdded(object);
        }
    }

    public void removeObject(LevelObject object) {
        if (!objects.remove(object)) {
            return;
        }
        if (object instanceof Tank) {
            tanks.remove(object);
            ((Tank) object).deactivate();
        } else if (object instanceof Bullet) {
            bullets.remove(object);
        }
        for (WorldListener listener : new ArrayList<>(listeners)) {
            listener.objectRemoved(object);
        }
    }

    /** Subscriptions receive existing objects through the same events as later additions. */
    public void addListener(WorldListener listener) {
        Objects.requireNonNull(listener, "listener");
        if (listeners.contains(listener)) {
            return;
        }
        listeners.add(listener);
        for (LevelObject object : new ArrayList<>(objects)) {
            listener.objectAdded(object);
        }
    }

    public void removeListener(WorldListener listener) {
        listeners.remove(listener);
    }

    public boolean contains(LevelObject object) {
        return objects.contains(object);
    }

    @Override
    public boolean canMoveTo(GridPoint2 coordinates) {
        if (!field.isInside(coordinates)) {
            return false;
        }
        for (LevelObject object : objects) {
            if (object instanceof Obstacle && object.occupies(coordinates)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public void update(float deltaTime) {
        if (!Float.isFinite(deltaTime) || deltaTime < 0f) {
            throw new IllegalArgumentException("Delta time must be finite and non-negative");
        }
        resolveCollisions();
        double remaining = deltaTime;
        do {
            // Advance to the next bullet cell boundary so a long frame cannot let
            // opposite bullets pass through each other or skip intermediate cells.
            double nextTick = remaining;
            for (Bullet bullet : bullets) {
                nextTick = Math.min(nextTick, bullet.timeUntilNextCell());
            }
            float tick = (float) nextTick;
            for (LevelObject object : new ArrayList<>(objects)) {
                if (contains(object)) {
                    object.update(tick);
                }
            }
            resolveCollisions();
            remaining -= tick;
        } while (remaining > 0f);
    }

    private void resolveCollisions() {
        for (Bullet bullet : new ArrayList<>(bullets)) {
            resolveCollision(bullet);
        }
    }

    void resolveCollision(Bullet bullet) {
        if (!contains(bullet)) {
            return;
        }
        GridPoint2 cell = bullet.getCoordinates();
        if (!field.isInside(cell)) {
            removeObject(bullet);
            return;
        }
        for (LevelObject object : new ArrayList<>(objects)) {
            // A moving shooter reserves its muzzle cell; do not hit its own armour.
            if (object != bullet && object != bullet.getOwner() && object.occupies(cell)) {
                bullet.collideWith(object);
                return;
            }
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

    public List<Bullet> getBullets() {
        return bulletView;
    }

    public List<LevelObject> getObjects() {
        return objectView;
    }
}
