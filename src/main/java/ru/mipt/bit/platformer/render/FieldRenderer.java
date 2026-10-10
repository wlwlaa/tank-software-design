package ru.mipt.bit.platformer.render;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Rectangle;

import static ru.mipt.bit.platformer.util.GdxGameUtils.getSingleLayer;

public class FieldRenderer {

    private final TiledMap level;
    private final OrthogonalTiledMapRenderer mapRenderer;
    private final float mapWidth;
    private final float mapHeight;
    private final Rectangle viewBounds = new Rectangle();
    private final Matrix4 projection = new Matrix4();

    public FieldRenderer(TiledMap level, OrthogonalTiledMapRenderer mapRenderer) {
        this.level = level;
        this.mapRenderer = mapRenderer;
        TiledMapTileLayer ground = getSingleLayer(level);
        mapWidth = (float) ground.getWidth() * ground.getTileWidth();
        mapHeight = (float) ground.getHeight() * ground.getTileHeight();
    }

    public void renderMap() {
        fitView(viewBounds, mapWidth, mapHeight,
                Math.max(1, Gdx.graphics.getWidth()), Math.max(1, Gdx.graphics.getHeight()));
        projection.setToOrtho2D(viewBounds.x, viewBounds.y, viewBounds.width, viewBounds.height);
        mapRenderer.setView(projection, viewBounds.x, viewBounds.y, viewBounds.width, viewBounds.height);
        mapRenderer.render();
    }

    static Rectangle fitView(Rectangle bounds, float mapWidth, float mapHeight, int screenWidth, int screenHeight) {
        float aspectRatio = (float) screenWidth / screenHeight;
        float viewWidth = Math.max(mapWidth, mapHeight * aspectRatio);
        float viewHeight = viewWidth / aspectRatio;
        return bounds.set((mapWidth - viewWidth) / 2f, (mapHeight - viewHeight) / 2f, viewWidth, viewHeight);
    }

    public void dispose() {
        mapRenderer.dispose();
        level.dispose();
    }
}
