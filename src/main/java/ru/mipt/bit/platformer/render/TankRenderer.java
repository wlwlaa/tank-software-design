package ru.mipt.bit.platformer.render;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import ru.mipt.bit.platformer.model.Tank;

import static ru.mipt.bit.platformer.util.GdxGameUtils.createBoundingRectangle;
import static ru.mipt.bit.platformer.util.GdxGameUtils.drawTextureRegionUnscaled;

public class TankRenderer implements SceneRenderer {

    private final Tank tank;
    private final TilePositioner tilePositioner;
    private final Texture texture;
    private final TextureRegion graphics;
    private final Rectangle rectangle;

    public TankRenderer(String texturePath, Tank tank, TilePositioner tilePositioner) {
        this.tank = tank;
        this.tilePositioner = tilePositioner;
        texture = new Texture(texturePath);
        graphics = new TextureRegion(texture);
        rectangle = createBoundingRectangle(graphics);
    }

    @Override
    public void render(Batch batch) {
        tilePositioner.moveRectangleBetweenTileCenters(
                rectangle,
                tank.getCoordinates(),
                tank.getDestinationCoordinates(),
                tank.getMovementProgress()
        );
        drawTextureRegionUnscaled(batch, graphics, rectangle, tank.getDirection().getRotation());
    }

    @Override
    public void dispose() {
        texture.dispose();
    }
}
