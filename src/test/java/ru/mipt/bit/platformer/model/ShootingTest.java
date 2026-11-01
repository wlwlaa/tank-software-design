package ru.mipt.bit.platformer.model;

import com.badlogic.gdx.math.GridPoint2;
import org.junit.Test;
import ru.mipt.bit.platformer.control.Command;
import ru.mipt.bit.platformer.control.MoveCommand;
import ru.mipt.bit.platformer.control.ShootCommand;

import java.util.Collections;

import static org.junit.Assert.*;

public class ShootingTest {

    @Test
    public void commandDefersShotUntilExecution() {
        TankWorld world = emptyWorld(6, 3);
        Tank tank = world.addTank(new GridPoint2(1, 1), 0.4f);
        Command command = new ShootCommand(tank);

        assertTrue(world.getBullets().isEmpty());
        command.execute();

        assertEquals(1, world.getBullets().size());
        assertThrows(NullPointerException.class, () -> new ShootCommand(null));
    }

    @Test
    public void shootsInAllFourDirectionsAndDoesNotHitMovingShooter() {
        for (Direction direction : Direction.values()) {
            TankWorld world = emptyWorld(9, 9);
            Tank tank = world.addTank(new GridPoint2(4, 4), 0.4f);
            tank.tryMove(direction);
            GridPoint2 muzzle = direction.move(tank.getCoordinates());

            tank.shoot();

            Bullet bullet = world.getBullets().get(0);
            assertEquals(muzzle, bullet.getCoordinates());
            assertEquals(direction, bullet.getDirection());
            assertEquals(25, bullet.getDamage());
            bullet.getCoordinates().set(-1, -1);
            bullet.getDestinationCoordinates().set(-1, -1);
            assertEquals(muzzle, bullet.getCoordinates());
            world.update(0.05f);
            assertEquals(0.5f, bullet.getMovementProgress(), 0.0001f);
            world.update(0.05f);

            assertEquals(direction.move(muzzle), bullet.getCoordinates());
            assertEquals(Tank.DEFAULT_HEALTH, tank.getHealth());
            assertTrue(world.contains(bullet));
        }
    }

    @Test
    public void adjacentTankTakesConfiguredDamageAndDiesStrictlyBelowZero() {
        TankWorld world = new TankWorld(new Field(3, 1, Collections.emptyList()),
                80, new BulletSettings(40, 0.1f), 0.4f);
        Tank shooter = world.addTank(new GridPoint2(0, 0), 0.4f);
        Tank target = world.addTank(new GridPoint2(1, 0), 0.4f);

        shooter.shoot();
        assertEquals(40, target.getHealth());
        assertTrue(world.getBullets().isEmpty());
        world.update(0.4f);
        shooter.shoot();
        assertEquals(0, target.getHealth());
        assertTrue(world.contains(target));
        world.update(0.4f);
        shooter.shoot();

        assertEquals(-40, target.getHealth());
        assertFalse(target.isActive());
        assertFalse(world.contains(target));
        assertEquals(Collections.singletonList(shooter), world.getTanks());
        assertTrue(world.canMoveTo(new GridPoint2(1, 0)));
    }

    @Test
    public void adjacentTreeStopsBulletAtCreationAndSurvives() {
        Tree tree = new Tree(new GridPoint2(1, 0));
        TankWorld world = new TankWorld(new Field(3, 1, Collections.singletonList(tree)));
        Tank tank = world.addTank(new GridPoint2(0, 0), 0.4f);

        tank.shoot();

        assertTrue(world.getBullets().isEmpty());
        assertTrue(world.contains(tree));
        assertEquals(Tank.DEFAULT_HEALTH, tank.getHealth());
    }

    @Test
    public void outwardShotsDisappearAtEveryBorder() {
        GridPoint2[] starts = {new GridPoint2(1, 2), new GridPoint2(0, 1),
                new GridPoint2(1, 0), new GridPoint2(2, 1)};
        for (Direction direction : Direction.values()) {
            TankWorld world = emptyWorld(3, 3);
            Tank tank = world.addTank(starts[direction.ordinal()], 0.4f);
            tank.tryMove(direction);

            tank.shoot();
            world.update(1f);

            assertTrue(world.getBullets().isEmpty());
            assertEquals(1, world.getObjects().size());
        }
    }

    @Test
    public void largeTickCannotSkipTankOrDamageItTwice() {
        TankWorld world = emptyWorld(10, 1);
        Tank shooter = world.addTank(new GridPoint2(0, 0), 0.4f);
        Tank target = world.addTank(new GridPoint2(8, 0), 0.4f);
        shooter.shoot();

        world.update(5f);
        world.update(5f);

        assertEquals(75, target.getHealth());
        assertTrue(world.getBullets().isEmpty());
    }

    @Test
    public void treeStopsTravellingBulletBeforeTankBehindIt() {
        Tree tree = new Tree(new GridPoint2(3, 0));
        TankWorld world = new TankWorld(new Field(8, 1, Collections.singletonList(tree)));
        Tank shooter = world.addTank(new GridPoint2(0, 0), 0.4f);
        Tank target = world.addTank(new GridPoint2(5, 0), 0.4f);
        shooter.shoot();

        world.update(2f);

        assertEquals(Tank.DEFAULT_HEALTH, target.getHealth());
        assertTrue(world.getBullets().isEmpty());
        assertTrue(world.contains(tree));
    }

    @Test
    public void oppositeBulletsDisappearWithoutPassingThroughEachOtherInLongFrame() {
        for (int rightStart : new int[]{4, 5}) {
            TankWorld world = emptyWorld(8, 1);
            Tank left = world.addTank(new GridPoint2(0, 0), 0.4f);
            Tank right = world.addTank(new GridPoint2(rightStart, 0), 0.4f);
            right.tryMove(Direction.LEFT);
            world.update(0.4f);
            left.shoot();
            right.shoot();
            assertEquals(2, world.getBullets().size());

            world.update(5f);

            assertTrue(world.getBullets().isEmpty());
            assertEquals(Tank.DEFAULT_HEALTH, left.getHealth());
            assertEquals(Tank.DEFAULT_HEALTH, right.getHealth());
        }
    }

    @Test
    public void bulletsSharingMuzzleCellCollideImmediately() {
        TankWorld world = emptyWorld(5, 1);
        Tank left = world.addTank(new GridPoint2(0, 0), 0.4f);
        Tank right = world.addTank(new GridPoint2(3, 0), 0.4f);
        right.tryMove(Direction.LEFT);
        world.update(0.4f);
        left.shoot();
        assertEquals(1, world.getBullets().size());

        right.shoot();

        assertTrue(world.getBullets().isEmpty());
        assertEquals(Tank.DEFAULT_HEALTH, left.getHealth());
        assertEquals(Tank.DEFAULT_HEALTH, right.getHealth());
    }

    @Test
    public void tankEnteringBulletCellIsHitEvenWithZeroTick() {
        TankWorld world = emptyWorld(4, 1);
        Tank shooter = world.addTank(new GridPoint2(0, 0), 0.4f);
        Tank target = world.addTank(new GridPoint2(2, 0), 0.4f);
        shooter.shoot();
        target.tryMove(Direction.LEFT);

        world.update(0f);

        assertEquals(75, target.getHealth());
        assertTrue(world.getBullets().isEmpty());
    }

    @Test
    public void destructionDuringUpdateReleasesBothCellsAndDisablesQueuedCommands() {
        TankWorld world = new TankWorld(new Field(5, 2, Collections.emptyList()),
                0, BulletSettings.DEFAULT, 0.4f);
        Tank shooter = world.addTank(new GridPoint2(0, 0), 0.4f);
        Tank target = world.addTank(new GridPoint2(2, 1), 0.4f);
        target.tryMove(Direction.DOWN);
        Command staleShot = new ShootCommand(target);
        Command staleMove = new MoveCommand(target, Direction.RIGHT);
        shooter.shoot();

        world.update(0.1f);
        staleShot.execute();
        staleMove.execute();
        target.takeDamage(10);

        assertEquals(-25, target.getHealth());
        assertFalse(world.contains(target));
        assertTrue(world.canMoveTo(new GridPoint2(2, 1)));
        assertTrue(world.canMoveTo(new GridPoint2(2, 0)));
        assertFalse(target.occupies(new GridPoint2(2, 0)));
        assertTrue(world.getBullets().isEmpty());
    }

    @Test
    public void reloadPreventsFrameRateFromCreatingUnlimitedBullets() {
        TankWorld world = emptyWorld(20, 1);
        Tank tank = world.addTank(new GridPoint2(0, 0), 0.4f);
        tank.shoot();
        for (int frame = 0; frame < 10; frame++) {
            tank.shoot();
            world.update(0.01f);
        }
        assertEquals(1, world.getBullets().size());

        world.update(0.31f);
        tank.shoot();

        assertEquals(2, world.getBullets().size());
    }

    @Test
    public void bulletTimeSlicesDoNotDelayTankArrivalOrReloadAtExactDeadline() {
        for (float bulletDuration : new float[]{0.1f, 0.03f}) {
            TankWorld world = new TankWorld(new Field(30, 2, Collections.emptyList()),
                    100, new BulletSettings(25, bulletDuration), 0.4f);
            Tank shooter = world.addTank(new GridPoint2(0, 0), 0.4f);
            Tank moving = world.addTank(new GridPoint2(0, 1), 0.4f);
            moving.tryMove(Direction.RIGHT);
            shooter.shoot();

            world.update(0.4f);
            shooter.shoot();

            assertFalse(moving.isMoving());
            assertEquals(new GridPoint2(1, 1), moving.getCoordinates());
            assertEquals(2, world.getBullets().size());
        }
    }

    @Test
    public void removedTankCannotMoveOrShootAndRemovedBulletIsNotUpdated() {
        TankWorld world = emptyWorld(6, 1);
        Tank tank = world.addTank(new GridPoint2(0, 0), 0.4f);
        tank.shoot();
        Bullet bullet = world.getBullets().get(0);
        GridPoint2 bulletStart = bullet.getCoordinates();
        world.removeObject(bullet);
        world.removeObject(tank);

        tank.tryMove(Direction.RIGHT);
        tank.shoot();
        world.update(1f);

        assertFalse(tank.isActive());
        assertFalse(tank.isMoving());
        assertEquals(bulletStart, bullet.getCoordinates());
        assertTrue(world.getObjects().isEmpty());
    }

    @Test
    public void invalidSettingsAndTicksAreRejectedWithoutChangingState() {
        assertThrows(IllegalArgumentException.class, () -> new BulletSettings(0, 0.1f));
        for (float value : new float[]{0f, -1f, Float.NaN, Float.POSITIVE_INFINITY}) {
            assertThrows(IllegalArgumentException.class, () -> new BulletSettings(25, value));
        }
        TankWorld world = emptyWorld(5, 1);
        Tank tank = world.addTank(new GridPoint2(0, 0), 0.4f);
        tank.shoot();
        Bullet bullet = world.getBullets().get(0);
        for (float value : new float[]{-1f, Float.NaN, Float.POSITIVE_INFINITY}) {
            assertThrows(IllegalArgumentException.class, () -> world.update(value));
            assertThrows(IllegalArgumentException.class, () -> bullet.update(value));
        }
        assertEquals(new GridPoint2(1, 0), bullet.getCoordinates());
        assertThrows(IllegalArgumentException.class, () -> tank.takeDamage(-1));
        assertThrows(UnsupportedOperationException.class, () -> world.getObjects().clear());
        assertThrows(UnsupportedOperationException.class, () -> world.getBullets().clear());
    }

    private TankWorld emptyWorld(int width, int height) {
        return new TankWorld(new Field(width, height, Collections.emptyList()));
    }
}
