package ru.mipt.bit.platformer;

import com.badlogic.gdx.graphics.g2d.Batch;
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

import static ru.mipt.bit.platformer.util.GdxGameUtils.createSingleLayerMapRenderer;
import static ru.mipt.bit.platformer.util.GdxGameUtils.getSingleLayer;

class Field {

    private final TiledMap level;
    private final TiledMapTileLayer groundLayer;
    private final MapRenderer renderer;
    private final TileMovement tileMovement;
    private final List<Tree> trees;

    Field(Batch batch) {
        level = new TmxMapLoader().load("level.tmx");
        renderer = createSingleLayerMapRenderer(level, batch);
        groundLayer = getSingleLayer(level);
        tileMovement = new TileMovement(groundLayer, Interpolation.smooth);
        trees = Collections.singletonList(
                new Tree("images/greenTree.png", new GridPoint2(1, 3), groundLayer)
        );
    }

    boolean canMoveTo(GridPoint2 coordinates) {
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

    void moveBetweenTiles(Rectangle rectangle, GridPoint2 from, GridPoint2 to, float progress) {
        tileMovement.moveRectangleBetweenTileCenters(rectangle, from, to, progress);
    }

    void renderMap() {
        renderer.render();
    }

    void renderObjects(Batch batch) {
        for (Tree tree : trees) {
            tree.render(batch);
        }
    }

    void dispose() {
        for (Tree tree : trees) {
            tree.dispose();
        }
        level.dispose();
    }
}
