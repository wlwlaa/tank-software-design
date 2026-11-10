package ru.mipt.bit.platformer.render;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import ru.mipt.bit.platformer.model.Tank;

import java.util.Objects;
import java.util.function.BooleanSupplier;

/** Decorates an object's renderer with an optional, moving health bar. */
public final class HealthBarRenderer implements SceneRenderer {

    private static final float WIDTH = 64f;
    private static final float HEIGHT = 8f;
    private static final float OFFSET_Y = 54f;

    private final SceneRenderer decorated;
    private final Tank tank;
    private final TilePositioner tilePositioner;
    private final BooleanSupplier visible;
    private final TextureRegion pixel;
    private final Runnable disposePixel;
    private final Rectangle bounds = new Rectangle(0f, 0f, WIDTH, HEIGHT);

    public HealthBarRenderer(SceneRenderer decorated, Tank tank,
                             TilePositioner tilePositioner, BooleanSupplier visible) {
        this(decorated, tank, tilePositioner, visible, createPixel());
    }

    private HealthBarRenderer(SceneRenderer decorated, Tank tank, TilePositioner tilePositioner,
                              BooleanSupplier visible, Texture texture) {
        this(decorated, tank, tilePositioner, visible, new TextureRegion(texture), texture::dispose);
    }

    // Allows rendering and resource ownership to be checked without an OpenGL context.
    HealthBarRenderer(SceneRenderer decorated, Tank tank, TilePositioner tilePositioner,
                      BooleanSupplier visible, TextureRegion pixel, Runnable disposePixel) {
        this.decorated = Objects.requireNonNull(decorated);
        this.tank = Objects.requireNonNull(tank);
        this.tilePositioner = Objects.requireNonNull(tilePositioner);
        this.visible = Objects.requireNonNull(visible);
        this.pixel = Objects.requireNonNull(pixel);
        this.disposePixel = Objects.requireNonNull(disposePixel);
    }

    @Override
    public void render(Batch batch) {
        decorated.render(batch);
        if (!visible.getAsBoolean()) {
            return;
        }
        tilePositioner.moveRectangleBetweenTileCenters(bounds, tank.getCoordinates(),
                tank.getDestinationCoordinates(), tank.getMovementProgress());
        float y = bounds.y + OFFSET_Y;
        float fraction = tank.getMaxHealth() == 0 ? 0f
                : Math.max(0f, Math.min(1f, (float) tank.getHealth() / tank.getMaxHealth()));
        float previousColor = batch.getPackedColor();
        try {
            batch.setColor(0.2f, 0.2f, 0.2f, 1f);
            batch.draw(pixel, bounds.x, y, WIDTH, HEIGHT);
            batch.setColor(1f - fraction, fraction, 0f, 1f);
            batch.draw(pixel, bounds.x + 1f, y + 1f, (WIDTH - 2f) * fraction, HEIGHT - 2f);
        } finally {
            batch.setPackedColor(previousColor);
        }
    }

    @Override
    public void dispose() {
        try {
            decorated.dispose();
        } finally {
            disposePixel.run();
        }
    }

    private static Texture createPixel() {
        Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        try {
            pixmap.setColor(Color.WHITE);
            pixmap.fill();
            return new Texture(pixmap);
        } finally {
            pixmap.dispose();
        }
    }
}
