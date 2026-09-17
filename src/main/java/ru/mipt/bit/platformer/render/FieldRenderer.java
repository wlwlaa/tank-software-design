package ru.mipt.bit.platformer.render;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.maps.MapRenderer;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.math.GridPoint2;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.Rectangle;
import ru.mipt.bit.platformer.util.TileMovement;
import ru.mipt.bit.platformer.model.Field;
import ru.mipt.bit.platformer.model.Tree;

import static ru.mipt.bit.platformer.util.GdxGameUtils.createSingleLayerMapRenderer;
import static ru.mipt.bit.platformer.util.GdxGameUtils.getSingleLayer;

public class FieldRenderer {

    private final TiledMap level;
    private final TiledMapTileLayer groundLayer;
    private final MapRenderer mapRenderer;
    private final TileMovement tileMovement;
    private final TreeRenderer treeRenderer;

    public FieldRenderer(Batch batch) {
        level = new TmxMapLoader().load("level.tmx");
        groundLayer = getSingleLayer(level);
        mapRenderer = createSingleLayerMapRenderer(level, batch);
        tileMovement = new TileMovement(groundLayer, Interpolation.smooth);
        treeRenderer = new TreeRenderer("images/greenTree.png");
    }

    public int getWidth() {
        return groundLayer.getWidth();
    }

    public int getHeight() {
        return groundLayer.getHeight();
    }

    public void moveBetweenTiles(Rectangle rectangle, GridPoint2 from, GridPoint2 to, float progress) {
        tileMovement.moveRectangleBetweenTileCenters(rectangle, from, to, progress);
    }

    public void renderMap() {
        mapRenderer.render();
    }

    public void renderObjects(Batch batch, Field field) {
        for (Tree tree : field.getTrees()) {
            treeRenderer.render(batch, tree, groundLayer);
        }
    }

    public void dispose() {
        treeRenderer.dispose();
        level.dispose();
    }
}
