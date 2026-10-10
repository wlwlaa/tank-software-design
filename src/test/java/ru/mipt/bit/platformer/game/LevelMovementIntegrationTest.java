package ru.mipt.bit.platformer.game;

import com.badlogic.gdx.math.GridPoint2;
import org.junit.Test;
import ru.mipt.bit.platformer.level.FileLevelLoader;
import ru.mipt.bit.platformer.level.LevelDefinition;
import ru.mipt.bit.platformer.level.RandomLevelGenerator;
import ru.mipt.bit.platformer.model.Direction;
import ru.mipt.bit.platformer.model.Field;
import ru.mipt.bit.platformer.model.Tank;

import java.io.IOException;
import java.io.StringReader;
import java.util.Random;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

public class LevelMovementIntegrationTest {

    @Test
    public void loadedPlayerCanMoveToEmptyCellButCannotEnterTree() throws IOException {
        LevelDefinition level = FileLevelLoader.parse(new StringReader("__T\n_XT\n___"));
        Tank tank = tankOn(level);

        assertEquals(new GridPoint2(1, 1), tank.getCoordinates());
        tank.tryMove(Direction.RIGHT);
        assertFalse(tank.isMoving());
        tank.tryMove(Direction.UP);
        tank.update(0.4f);
        assertEquals(new GridPoint2(1, 2), tank.getCoordinates());
        tank.tryMove(Direction.RIGHT);
        assertFalse(tank.isMoving());
        tank.tryMove(Direction.UP);
        assertFalse(tank.isMoving());
    }

    @Test
    public void randomlyPlacedPlayerStaysInFreeStartWhenEveryOtherCellIsTree() {
        LevelDefinition level = new RandomLevelGenerator(4, 3, 11, new Random(42)).load();
        Tank tank = tankOn(level);

        for (Direction direction : Direction.values()) {
            tank.tryMove(direction);
            tank.update(0.4f);

            assertFalse(tank.isMoving());
            assertEquals(level.getPlayerStart(), tank.getCoordinates());
        }
    }

    private Tank tankOn(LevelDefinition level) {
        Field field = new Field(level.getWidth(), level.getHeight(), level.getObstacles());
        return new Tank(level.getPlayerStart(), 0.4f, field);
    }
}
