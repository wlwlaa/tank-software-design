package ru.mipt.bit.platformer.model;

import com.badlogic.gdx.math.GridPoint2;
import ru.mipt.bit.platformer.game.Updatable;

/**
 * Domain: internal contract for collidable simulation objects, not an infrastructure port.
 * Its Updatable parent currently lives in game; see that contract for the package-boundary gap.
 *
 * <p>A collidable object whose lifetime is managed by the logical level.
 */
public interface LevelObject extends Updatable {

    boolean occupies(GridPoint2 coordinates);

    @Override
    default void update(float deltaTime) {
        // Static level objects do not change with time.
    }
}
