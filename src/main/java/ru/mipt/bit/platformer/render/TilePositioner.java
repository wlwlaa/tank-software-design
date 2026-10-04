package ru.mipt.bit.platformer.render;

import com.badlogic.gdx.math.GridPoint2;
import com.badlogic.gdx.math.Rectangle;

public interface TilePositioner {

    Rectangle moveRectangleBetweenTileCenters(Rectangle rectangle, GridPoint2 from, GridPoint2 to, float progress);
}
