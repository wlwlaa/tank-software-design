package ru.mipt.bit.platformer.model;

import com.badlogic.gdx.math.GridPoint2;
import org.junit.Before;
import org.junit.Test;

import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class TankTest {

    private Field field;
    private Tank tank;

    @Before
    public void setUp() {
        field = new Field(4, 4, Collections.singletonList(new Tree(new GridPoint2(1, 2))));
        tank = new Tank(new GridPoint2(1, 1));
    }

    @Test
    public void startsAtInitialCoordinatesLookingRight() {
        assertEquals(new GridPoint2(1, 1), tank.getCoordinates());
        assertEquals(new GridPoint2(1, 1), tank.getDestinationCoordinates());
        assertEquals(Direction.RIGHT, tank.getDirection());
        assertFalse(tank.isMoving());
    }

    @Test
    public void startsMovementToFreeNeighbouringCell() {
        tank.tryMove(Direction.RIGHT, field);

        assertEquals(new GridPoint2(2, 1), tank.getDestinationCoordinates());
        assertEquals(0f, tank.getMovementProgress(), 0f);
        assertTrue(tank.isMoving());
    }

    @Test
    public void doesNotMoveIntoOccupiedCellButChangesDirection() {
        tank.tryMove(Direction.UP, field);

        assertEquals(Direction.UP, tank.getDirection());
        assertEquals(new GridPoint2(1, 1), tank.getDestinationCoordinates());
        assertFalse(tank.isMoving());
    }

    @Test
    public void updatesMovementProgressAndCoordinates() {
        tank.tryMove(Direction.RIGHT, field);

        tank.update(0.2f, field);
        assertEquals(0.5f, tank.getMovementProgress(), 0.0001f);
        assertEquals(new GridPoint2(1, 1), tank.getCoordinates());

        tank.update(0.2f, field);
        assertFalse(tank.isMoving());
        assertEquals(new GridPoint2(2, 1), tank.getCoordinates());
    }

    @Test
    public void protectsCoordinatesFromExternalChanges() {
        tank.getCoordinates().set(3, 3);
        tank.getDestinationCoordinates().set(3, 3);

        assertEquals(new GridPoint2(1, 1), tank.getCoordinates());
        assertEquals(new GridPoint2(1, 1), tank.getDestinationCoordinates());
    }
}
