package ru.mipt.bit.platformer;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.GridPoint2;
import com.badlogic.gdx.math.Rectangle;

import static com.badlogic.gdx.math.MathUtils.isEqual;
import static ru.mipt.bit.platformer.util.GdxGameUtils.continueProgress;
import static ru.mipt.bit.platformer.util.GdxGameUtils.createBoundingRectangle;
import static ru.mipt.bit.platformer.util.GdxGameUtils.drawTextureRegionUnscaled;

class Tank {

    private static final float MOVEMENT_SPEED = 0.4f;

    private final Texture texture;
    private final TextureRegion graphics;
    private final Rectangle rectangle;
    private final GridPoint2 coordinates;
    private final GridPoint2 destinationCoordinates;

    private Direction direction = Direction.RIGHT;
    private float movementProgress = 1f;

    Tank(String texturePath, GridPoint2 initialCoordinates) {
        texture = new Texture(texturePath);
        graphics = new TextureRegion(texture);
        rectangle = createBoundingRectangle(graphics);
        coordinates = new GridPoint2(initialCoordinates);
        destinationCoordinates = new GridPoint2(initialCoordinates);
    }

    boolean isMoving() {
        return !isEqual(movementProgress, 1f);
    }

    void tryMove(Direction newDirection, Field field) {
        direction = newDirection;
        GridPoint2 destination = direction.move(coordinates);
        if (field.canMoveTo(destination)) {
            destinationCoordinates.set(destination);
            movementProgress = 0f;
        }
    }

    void update(float deltaTime, Field field) {
        field.moveBetweenTiles(rectangle, coordinates, destinationCoordinates, movementProgress);
        movementProgress = continueProgress(movementProgress, deltaTime, MOVEMENT_SPEED);
        if (!isMoving()) {
            coordinates.set(destinationCoordinates);
        }
    }

    void render(Batch batch) {
        drawTextureRegionUnscaled(batch, graphics, rectangle, direction.getRotation());
    }

    void dispose() {
        texture.dispose();
    }
}
