package ru.mipt.bit.platformer.render;

import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static ru.mipt.bit.platformer.util.GdxGameUtils.getSingleLayer;

public class GroundMapFactoryTest {

    @Test
    public void fillsLargerGroundWithoutGapsUsingTemplateCells() {
        TiledMap map = templateMap();
        TiledMapTileLayer template = getSingleLayer(map);

        GroundMapFactory.resize(map, 5, 3);
        TiledMapTileLayer ground = getSingleLayer(map);

        assertEquals(5, ground.getWidth());
        assertEquals(3, ground.getHeight());
        assertEquals(128, ground.getTileWidth());
        assertEquals(128, ground.getTileHeight());
        assertEquals("Ground", ground.getName());
        for (int x = 0; x < 5; x++) {
            for (int y = 0; y < 3; y++) {
                assertNotNull(ground.getCell(x, y));
                assertSame(template.getCell(x % 2, y % 2), ground.getCell(x, y));
            }
        }
        assertEquals(5, map.getProperties().get("width"));
        assertEquals(3, map.getProperties().get("height"));
    }

    @Test
    public void smallerLevelHasOnlyItsOwnGroundDimensions() {
        TiledMap map = templateMap();

        GroundMapFactory.resize(map, 1, 1);
        TiledMapTileLayer ground = getSingleLayer(map);

        assertEquals(1, map.getLayers().size());
        assertEquals(1, ground.getWidth());
        assertEquals(1, ground.getHeight());
        assertNotNull(ground.getCell(0, 0));
    }

    private TiledMap templateMap() {
        TiledMap map = new TiledMap();
        TiledMapTileLayer ground = new TiledMapTileLayer(2, 2, 128, 128);
        ground.setName("Ground");
        for (int x = 0; x < 2; x++) {
            for (int y = 0; y < 2; y++) {
                ground.setCell(x, y, new TiledMapTileLayer.Cell());
            }
        }
        map.getLayers().add(ground);
        return map;
    }
}
