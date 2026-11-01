package ru.mipt.bit.platformer.render;

import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import ru.mipt.bit.platformer.model.Bullet;

import static ru.mipt.bit.platformer.util.GdxGameUtils.createBoundingRectangle;
import static ru.mipt.bit.platformer.util.GdxGameUtils.drawTextureRegionUnscaled;

public final class BulletRenderer implements SceneRenderer {

    private final Bullet bullet;
    private final TilePositioner tilePositioner;
    private final Texture texture;
    private final TextureRegion graphics;
    private final Rectangle rectangle;

    public BulletRenderer(Bullet bullet, TilePositioner tilePositioner) {
        this.bullet = bullet;
        this.tilePositioner = tilePositioner;
        Pixmap pixmap = new Pixmap(12, 6, Pixmap.Format.RGBA8888);
        try {
            pixmap.setColor(1f, 0.85f, 0.2f, 1f);
            pixmap.fill();
            texture = new Texture(pixmap);
        } finally {
            pixmap.dispose();
        }
        graphics = new TextureRegion(texture);
        rectangle = createBoundingRectangle(graphics);
    }

    @Override
    public void render(Batch batch) {
        tilePositioner.moveRectangleBetweenTileCenters(rectangle, bullet.getCoordinates(),
                bullet.getDestinationCoordinates(), bullet.getMovementProgress());
        drawTextureRegionUnscaled(batch, graphics, rectangle, bullet.getDirection().getRotation());
    }

    @Override
    public void dispose() {
        texture.dispose();
    }
}
