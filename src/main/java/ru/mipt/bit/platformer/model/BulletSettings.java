package ru.mipt.bit.platformer.model;

public final class BulletSettings {

    public static final BulletSettings DEFAULT = new BulletSettings(25, 0.1f);

    private final int damage;
    private final float movementDuration;

    public BulletSettings(int damage, float movementDuration) {
        if (damage <= 0) {
            throw new IllegalArgumentException("Bullet damage must be positive");
        }
        if (!Float.isFinite(movementDuration) || movementDuration <= 0f) {
            throw new IllegalArgumentException("Bullet movement duration must be finite and positive");
        }
        this.damage = damage;
        this.movementDuration = movementDuration;
    }

    public int getDamage() {
        return damage;
    }

    public float getMovementDuration() {
        return movementDuration;
    }
}
