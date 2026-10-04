package ru.mipt.bit.platformer.render;

import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;

public class FieldRenderer {

    private final TiledMap level;
    private final OrthogonalTiledMapRenderer mapRenderer;

    public FieldRenderer(TiledMap level, OrthogonalTiledMapRenderer mapRenderer) {
        this.level = level;
        this.mapRenderer = mapRenderer;
    }

    public void renderMap() {
        mapRenderer.render();
    }

    public void dispose() {
        mapRenderer.dispose();
        level.dispose();
    }
}
