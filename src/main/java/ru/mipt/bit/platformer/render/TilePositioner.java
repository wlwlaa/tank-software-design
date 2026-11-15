package ru.mipt.bit.platformer.render;

import com.badlogic.gdx.math.GridPoint2;
import com.badlogic.gdx.math.Rectangle;

/**
 * Infrastructure: internal rendering strategy for tile-to-screen positioning.
 * Not a core port because its Rectangle and GridPoint2 types are LibGDX-specific.
 * Implemented by TileMovement inside the presentation adapter.
 */
public interface TilePositioner {

    Rectangle moveRectangleBetweenTileCenters(Rectangle rectangle, GridPoint2 from, GridPoint2 to, float progress);
}
