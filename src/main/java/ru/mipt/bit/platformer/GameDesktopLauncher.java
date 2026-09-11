package ru.mipt.bit.platformer;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.MapRenderer;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.math.GridPoint2;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.Rectangle;
import ru.mipt.bit.platformer.util.TileMovement;

import java.util.Collections;
import java.util.List;

import static com.badlogic.gdx.graphics.GL20.GL_COLOR_BUFFER_BIT;
import static com.badlogic.gdx.math.MathUtils.isEqual;
import static ru.mipt.bit.platformer.util.GdxGameUtils.*;

public class GameDesktopLauncher implements ApplicationListener {

    private static final float MOVEMENT_SPEED = 0.4f;

    private enum Direction {
        UP(0, 1, 90f, Keys.UP, Keys.W),
        LEFT(-1, 0, -180f, Keys.LEFT, Keys.A),
        DOWN(0, -1, -90f, Keys.DOWN, Keys.S),
        RIGHT(1, 0, 0f, Keys.RIGHT, Keys.D);

        private final int deltaX;
        private final int deltaY;
        private final float rotation;
        private final int arrowKey;
        private final int letterKey;

        Direction(int deltaX, int deltaY, float rotation, int arrowKey, int letterKey) {
            this.deltaX = deltaX;
            this.deltaY = deltaY;
            this.rotation = rotation;
            this.arrowKey = arrowKey;
            this.letterKey = letterKey;
        }

        private GridPoint2 move(GridPoint2 coordinates) {
            return new GridPoint2(coordinates).add(deltaX, deltaY);
        }

        private float getRotation() {
            return rotation;
        }

        private boolean isPressed() {
            return Gdx.input.isKeyPressed(arrowKey) || Gdx.input.isKeyPressed(letterKey);
        }
    }

    private static class Tank {

        private final Texture texture;
        private final TextureRegion graphics;
        private final Rectangle rectangle;
        // Current and destination positions on the level tile grid.
        private final GridPoint2 coordinates;
        private final GridPoint2 destinationCoordinates;

        private Direction direction = Direction.RIGHT;
        private float movementProgress = 1f;

        private Tank(String texturePath, GridPoint2 initialCoordinates) {
            // Texture represents a native resource owned by the tank.
            texture = new Texture(texturePath);
            graphics = new TextureRegion(texture);
            rectangle = createBoundingRectangle(graphics);
            coordinates = new GridPoint2(initialCoordinates);
            destinationCoordinates = new GridPoint2(initialCoordinates);
        }

        private boolean isMoving() {
            return !isEqual(movementProgress, 1f);
        }

        private void tryMove(Direction newDirection, Field field) {
            direction = newDirection;
            GridPoint2 destination = direction.move(coordinates);
            if (field.canMoveTo(destination)) {
                destinationCoordinates.set(destination);
                movementProgress = 0f;
            }
        }

        private void update(float deltaTime, Field field) {
            field.moveBetweenTiles(
                    rectangle,
                    coordinates,
                    destinationCoordinates,
                    movementProgress
            );

            movementProgress = continueProgress(movementProgress, deltaTime, MOVEMENT_SPEED);
            if (!isMoving()) {
                coordinates.set(destinationCoordinates);
            }
        }

        private void render(Batch batch) {
            drawTextureRegionUnscaled(batch, graphics, rectangle, direction.getRotation());
        }

        private void dispose() {
            texture.dispose();
        }
    }

    private static class Tree {

        private final Texture texture;
        private final TextureRegion graphics;
        private final GridPoint2 coordinates;
        private final Rectangle rectangle;

        private Tree(String texturePath, GridPoint2 coordinates, TiledMapTileLayer groundLayer) {
            texture = new Texture(texturePath);
            graphics = new TextureRegion(texture);
            this.coordinates = new GridPoint2(coordinates);
            rectangle = createBoundingRectangle(graphics);
            moveRectangleAtTileCenter(groundLayer, rectangle, this.coordinates);
        }

        private boolean occupies(GridPoint2 coordinates) {
            return this.coordinates.equals(coordinates);
        }

        private void render(Batch batch) {
            drawTextureRegionUnscaled(batch, graphics, rectangle, 0f);
        }

        private void dispose() {
            texture.dispose();
        }
    }

    private static class Field {

        private final TiledMap level;
        private final TiledMapTileLayer groundLayer;
        private final MapRenderer renderer;
        private final TileMovement tileMovement;
        private final List<Tree> trees;

        private Field(Batch batch) {
            level = new TmxMapLoader().load("level.tmx");
            renderer = createSingleLayerMapRenderer(level, batch);
            groundLayer = getSingleLayer(level);
            tileMovement = new TileMovement(groundLayer, Interpolation.smooth);
            trees = Collections.singletonList(
                    new Tree("images/greenTree.png", new GridPoint2(1, 3), groundLayer)
            );
        }

        private boolean canMoveTo(GridPoint2 coordinates) {
            return isInside(coordinates) && !isOccupied(coordinates);
        }

        private boolean isInside(GridPoint2 coordinates) {
            return coordinates.x >= 0
                    && coordinates.x < groundLayer.getWidth()
                    && coordinates.y >= 0
                    && coordinates.y < groundLayer.getHeight();
        }

        private boolean isOccupied(GridPoint2 coordinates) {
            for (Tree tree : trees) {
                if (tree.occupies(coordinates)) {
                    return true;
                }
            }
            return false;
        }

        private void moveBetweenTiles(
                Rectangle rectangle,
                GridPoint2 from,
                GridPoint2 to,
                float progress
        ) {
            tileMovement.moveRectangleBetweenTileCenters(rectangle, from, to, progress);
        }

        private void renderMap() {
            renderer.render();
        }

        private void renderObjects(Batch batch) {
            for (Tree tree : trees) {
                tree.render(batch);
            }
        }

        private void dispose() {
            for (Tree tree : trees) {
                tree.dispose();
            }
            level.dispose();
        }
    }

    private Batch batch;
    private Field field;
    private Tank tank;

    @Override
    public void create() {
        batch = new SpriteBatch();
        field = new Field(batch);
        tank = new Tank("images/tank_blue.png", new GridPoint2(1, 1));
    }

    @Override
    public void render() {
        // clear the screen
        Gdx.gl.glClearColor(0f, 0f, 0.2f, 1f);
        Gdx.gl.glClear(GL_COLOR_BUFFER_BIT);

        // get time passed since the last render
        float deltaTime = Gdx.graphics.getDeltaTime();

        if (!tank.isMoving()) {
            handleMovementInput();
        }

        tank.update(deltaTime, field);

        // render each tile of the level
        field.renderMap();

        // start recording all drawing commands
        batch.begin();

        tank.render(batch);
        field.renderObjects(batch);

        // submit all drawing requests
        batch.end();
    }

    private void handleMovementInput() {
        for (Direction direction : Direction.values()) {
            if (!direction.isPressed()) {
                continue;
            }

            tank.tryMove(direction, field);
            return;
        }
    }

    @Override
    public void resize(int width, int height) {
        // do not react to window resizing
    }

    @Override
    public void pause() {
        // game doesn't get paused
    }

    @Override
    public void resume() {
        // game doesn't get paused
    }

    @Override
    public void dispose() {
        // dispose of all the native resources (classes which implement com.badlogic.gdx.utils.Disposable)
        tank.dispose();
        field.dispose();
        batch.dispose();
    }

    public static void main(String[] args) {
        Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
        // level width: 10 tiles x 128px, height: 8 tiles x 128px
        config.setWindowedMode(1280, 1024);
        new Lwjgl3Application(new GameDesktopLauncher(), config);
    }
}
