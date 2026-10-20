package ru.mipt.bit.platformer.model;

import com.badlogic.gdx.math.GridPoint2;
import org.junit.Test;

import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

public class TankWorldTest {

    @Test
    public void stationaryTankOccupiesOnlyItsOwnCell() {
        TankWorld world = emptyWorld(4, 3);
        Tank tank = world.addTank(new GridPoint2(1, 1), 0.4f);

        assertTrue(tank.occupies(new GridPoint2(1, 1)));
        assertFalse(world.canMoveTo(new GridPoint2(1, 1)));
        assertTrue(world.canMoveTo(new GridPoint2(2, 1)));
    }

    @Test
    public void reservesBothCellsImmediatelyAndReleasesSourceOnlyOnArrival() {
        TankWorld world = emptyWorld(4, 3);
        Tank tank = world.addTank(new GridPoint2(1, 1), 0.4f);

        tank.tryMove(Direction.RIGHT);
        assertBothCellsOccupied(world, tank);
        world.update(0.2f);
        assertBothCellsOccupied(world, tank);

        world.update(0.2f);
        assertTrue(world.canMoveTo(new GridPoint2(1, 1)));
        assertFalse(tank.occupies(new GridPoint2(1, 1)));
        assertFalse(world.canMoveTo(new GridPoint2(2, 1)));
        assertTrue(tank.occupies(new GridPoint2(2, 1)));
    }

    @Test
    public void keepsSourceReservedEvenJustBeforeMovementFinishes() {
        TankWorld world = emptyWorld(2, 1);
        Tank tank = world.addTank(new GridPoint2(0, 0), 1f);
        tank.tryMove(Direction.RIGHT);

        world.update(Math.nextDown(1f));

        assertTrue(tank.isMoving());
        assertFalse(world.canMoveTo(new GridPoint2(0, 0)));
        assertFalse(world.canMoveTo(new GridPoint2(1, 0)));

        world.update(1f - tank.getMovementProgress());

        assertFalse(tank.isMoving());
        assertTrue(world.canMoveTo(new GridPoint2(0, 0)));
        assertEquals(new GridPoint2(1, 0), tank.getCoordinates());
    }

    @Test
    public void otherTanksCannotEnterEitherCellDuringMovement() {
        TankWorld world = emptyWorld(4, 3);
        Tank moving = world.addTank(new GridPoint2(1, 1), 0.4f);
        Tank follower = world.addTank(new GridPoint2(0, 1), 0.4f);
        Tank contender = world.addTank(new GridPoint2(3, 1), 0.4f);
        moving.tryMove(Direction.RIGHT);

        follower.tryMove(Direction.RIGHT);
        contender.tryMove(Direction.LEFT);
        assertFalse(follower.isMoving());
        assertFalse(contender.isMoving());

        world.update(0.4f);
        follower.tryMove(Direction.RIGHT);
        contender.tryMove(Direction.LEFT);
        assertTrue(follower.isMoving());
        assertFalse(contender.isMoving());
    }

    @Test
    public void firstTankReservesContestedDestinationBeforeSecondCommand() {
        TankWorld world = emptyWorld(3, 1);
        Tank first = world.addTank(new GridPoint2(0, 0), 0.4f);
        Tank second = world.addTank(new GridPoint2(2, 0), 0.4f);

        first.tryMove(Direction.RIGHT);
        second.tryMove(Direction.LEFT);

        assertTrue(first.isMoving());
        assertFalse(second.isMoving());
        world.update(0.4f);
        assertEquals(new GridPoint2(1, 0), first.getCoordinates());
        assertEquals(new GridPoint2(2, 0), second.getCoordinates());
    }

    @Test
    public void adjacentTanksCannotSwapCells() {
        TankWorld world = emptyWorld(2, 1);
        Tank first = world.addTank(new GridPoint2(0, 0), 0.4f);
        Tank second = world.addTank(new GridPoint2(1, 0), 0.4f);

        first.tryMove(Direction.RIGHT);
        second.tryMove(Direction.LEFT);

        assertFalse(first.isMoving());
        assertFalse(second.isMoving());
    }

    @Test
    public void updatesAllTanksWithTheirOwnMovementDurations() {
        TankWorld world = emptyWorld(4, 2);
        Tank first = world.addTank(new GridPoint2(0, 0), 0.4f);
        Tank second = world.addTank(new GridPoint2(0, 1), 0.8f);
        first.tryMove(Direction.RIGHT);
        second.tryMove(Direction.RIGHT);

        world.update(0.4f);

        assertEquals(new GridPoint2(1, 0), first.getCoordinates());
        assertFalse(first.isMoving());
        assertEquals(0.5f, second.getMovementProgress(), 0.0001f);
        assertTrue(second.isMoving());
    }

    @Test
    public void everyTankStopsAtAllFourBoundaries() {
        GridPoint2[] starts = {new GridPoint2(0, 0), new GridPoint2(2, 0),
                new GridPoint2(1, 0), new GridPoint2(1, 1)};
        Direction[] directions = {Direction.LEFT, Direction.RIGHT, Direction.DOWN, Direction.UP};
        for (int index = 0; index < starts.length; index++) {
            TankWorld world = emptyWorld(3, 2);
            Tank tank = world.addTank(starts[index], 0.4f);

            tank.tryMove(directions[index]);
            world.update(1f);

            assertFalse(tank.isMoving());
            assertEquals(starts[index], tank.getCoordinates());
            assertEquals(starts[index], tank.getDestinationCoordinates());
            assertEquals(directions[index], tank.getDirection());
        }
    }

    @Test
    public void obstaclesStillBlockEveryTank() {
        TankWorld world = new TankWorld(new Field(3, 2,
                Collections.singletonList(new Tree(new GridPoint2(1, 0)))));
        Tank tank = world.addTank(new GridPoint2(0, 0), 0.4f);

        tank.tryMove(Direction.RIGHT);

        assertFalse(tank.isMoving());
        assertFalse(world.canMoveTo(new GridPoint2(1, 0)));
    }

    @Test
    public void rejectsInvalidSpawnsWithoutChangingRoster() {
        TankWorld world = new TankWorld(new Field(3, 2,
                Collections.singletonList(new Tree(new GridPoint2(1, 0)))));
        world.addTank(new GridPoint2(0, 0), 0.4f);

        GridPoint2[] invalidStarts = {new GridPoint2(0, 0), new GridPoint2(1, 0),
                new GridPoint2(-1, 0), new GridPoint2(3, 0), new GridPoint2(0, 2)};
        for (GridPoint2 start : invalidStarts) {
            assertThrows(IllegalArgumentException.class, () -> world.addTank(start, 0.4f));
        }
        assertThrows(IllegalArgumentException.class, () -> world.addTank(new GridPoint2(2, 0), 0f));
        assertThrows(NullPointerException.class, () -> world.addTank(null, 0.4f));
        assertEquals(1, world.getTanks().size());
    }

    @Test
    public void cannotSpawnIntoSourceOrDestinationOfMovingTank() {
        TankWorld world = emptyWorld(3, 1);
        Tank tank = world.addTank(new GridPoint2(0, 0), 0.4f);
        tank.tryMove(Direction.RIGHT);

        assertThrows(IllegalArgumentException.class, () -> world.addTank(new GridPoint2(0, 0), 0.4f));
        assertThrows(IllegalArgumentException.class, () -> world.addTank(new GridPoint2(1, 0), 0.4f));
    }

    @Test
    public void rosterIsReadOnlyAndStartCoordinatesAreCopied() {
        TankWorld world = emptyWorld(3, 2);
        List<Tank> roster = world.getTanks();
        GridPoint2 start = new GridPoint2(0, 0);
        Tank tank = world.addTank(start, 0.4f);
        start.set(2, 1);

        assertEquals(3, world.getWidth());
        assertEquals(2, world.getHeight());
        assertEquals(1, roster.size());
        assertEquals(new GridPoint2(0, 0), tank.getCoordinates());
        assertThrows(UnsupportedOperationException.class, roster::clear);
    }

    @Test
    public void requiresField() {
        assertThrows(NullPointerException.class, () -> new TankWorld(null));
    }

    private TankWorld emptyWorld(int width, int height) {
        return new TankWorld(new Field(width, height, Collections.emptyList()));
    }

    private void assertBothCellsOccupied(TankWorld world, Tank tank) {
        for (GridPoint2 cell : new GridPoint2[]{new GridPoint2(1, 1), new GridPoint2(2, 1)}) {
            assertTrue(tank.occupies(cell));
            assertFalse(world.canMoveTo(cell));
        }
    }
}
