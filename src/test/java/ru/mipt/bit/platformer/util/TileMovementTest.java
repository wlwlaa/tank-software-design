package ru.mipt.bit.platformer.util;

import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.math.GridPoint2;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.Rectangle;
import org.junit.Test;
import ru.mipt.bit.platformer.render.TilePositioner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

public class TileMovementTest {

    @Test
    public void positionsStaticAndMovingObjectsThroughTheSameContract() {
        TilePositioner positioner = new TileMovement(new TiledMapTileLayer(4, 4, 32, 32), Interpolation.linear);
        Rectangle rectangle = new Rectangle(0, 0, 16, 16);
        GridPoint2 from = new GridPoint2(1, 1);
        GridPoint2 to = new GridPoint2(2, 1);

        assertSame(rectangle, positioner.moveRectangleBetweenTileCenters(rectangle, from, from, 1f));
        assertEquals(40f, rectangle.x, 0.0001f);
        assertEquals(40f, rectangle.y, 0.0001f);

        positioner.moveRectangleBetweenTileCenters(rectangle, from, to, 0.5f);
        assertEquals(56f, rectangle.x, 0.0001f);
        assertEquals(40f, rectangle.y, 0.0001f);

        positioner.moveRectangleBetweenTileCenters(rectangle, from, to, 1f);
        assertEquals(72f, rectangle.x, 0.0001f);
    }
}
