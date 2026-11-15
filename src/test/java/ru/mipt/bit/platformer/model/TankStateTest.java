package ru.mipt.bit.platformer.model;

import com.badlogic.gdx.math.GridPoint2;
import org.junit.Test;
import ru.mipt.bit.platformer.control.Command;
import ru.mipt.bit.platformer.control.ShootCommand;
import ru.mipt.bit.platformer.model.state.HeavyTankState;
import ru.mipt.bit.platformer.model.state.LightTankState;
import ru.mipt.bit.platformer.model.state.MediumTankState;
import ru.mipt.bit.platformer.model.state.TankState;

import java.util.Collections;

import static org.junit.Assert.*;

public class TankStateTest {

    @Test
    public void healthBoundariesSetSpeedAndShootingRelativeToInitialHealth() {
        int[][] cases = {
                // Initial health, current health, step duration multiplier.
                {100, 100, 1}, {100, 71, 1}, {100, 70, 2},
                {100, 15, 2}, {100, 14, 3}, {100, 0, 3},
                {200, 141, 1}, {200, 140, 2}, {200, 30, 2}, {200, 29, 3},
                {101, 71, 1}, {101, 70, 2}, {101, 16, 2}, {101, 15, 3},
                {Integer.MAX_VALUE, 1_600_000_000, 1},
                {Integer.MAX_VALUE, 1_000_000_000, 2},
                {Integer.MAX_VALUE, 300_000_000, 3}
        };
        for (int[] scenario : cases) {
            TankWorld world = world(scenario[0]);
            Tank tank = world.addTank(new GridPoint2(0, 0), 3f);
            if (scenario[0] != scenario[1]) {
                tank.takeDamage(scenario[0] - scenario[1]);
            }
            int durationMultiplier = scenario[2];
            TankState expectedState = durationMultiplier == 1 ? LightTankState.INSTANCE
                    : durationMultiplier == 2 ? MediumTankState.INSTANCE : HeavyTankState.INSTANCE;
            String description = scenario[1] + "/" + scenario[0];
            assertSame(description, expectedState, tank.getState());
            tank.tryMove(Direction.RIGHT);
            tank.update(3f);
            assertEquals(description, 1f / durationMultiplier, tank.getMovementProgress(), 0.0001f);
            tank.shoot();
            assertEquals(description, durationMultiplier == 3 ? 0 : 1, world.getBullets().size());

            tank.update(3f * (durationMultiplier - 1));
            assertFalse(description, tank.isMoving());
            assertEquals(description, new GridPoint2(1, 0), tank.getCoordinates());
            assertEquals(description, scenario[0], tank.getMaxHealth());
            assertTrue(description, tank.isActive());
        }
    }

    @Test
    public void damageTransitionsPreservePositionAndReservationsDuringAStep() {
        TankWorld world = world(100);
        Tank tank = world.addTank(new GridPoint2(0, 0), 1f);
        tank.tryMove(Direction.RIGHT);
        tank.update(0.25f);
        tank.takeDamage(30);
        assertSame(MediumTankState.INSTANCE, tank.getState());
        assertEquals(0.25f, tank.getMovementProgress(), 0f);

        tank.update(0.5f);
        assertEquals(0.5f, tank.getMovementProgress(), 0f);
        tank.takeDamage(56);
        assertSame(HeavyTankState.INSTANCE, tank.getState());
        assertEquals(0.5f, tank.getMovementProgress(), 0f);
        tank.tryMove(Direction.UP);
        assertEquals(Direction.RIGHT, tank.getDirection());
        assertFalse(world.canMoveTo(new GridPoint2(0, 0)));
        assertFalse(world.canMoveTo(new GridPoint2(1, 0)));

        tank.update(0.75f);
        assertEquals(0.75f, tank.getMovementProgress(), 0.0001f);
        tank.update(0.75f);
        assertFalse(tank.isMoving());
        assertTrue(world.canMoveTo(new GridPoint2(0, 0)));
        assertEquals(new GridPoint2(1, 0), tank.getCoordinates());
    }

    @Test
    public void damageWhileStationaryDoesNotStartMovement() {
        Tank tank = world(100).addTank(new GridPoint2(0, 0), 1f);
        tank.takeDamage(90);
        assertFalse(tank.isMoving());
        assertEquals(1f, tank.getMovementProgress(), 0f);
        tank.update(1f);
        assertEquals(new GridPoint2(0, 0), tank.getCoordinates());
    }

    @Test
    public void bulletTimeSlicesDoNotDelayDamagedTanksAtTheirMovementDeadline() {
        for (float duration : new float[]{0.4f, 1f}) {
            for (float bulletDuration : new float[]{0.1f, 0.03f}) {
                for (int slowdown : new int[]{2, 3}) {
                    TankWorld world = new TankWorld(new Field(200, 2, Collections.emptyList()),
                            100, new BulletSettings(25, bulletDuration), Tank.DEFAULT_SHOT_INTERVAL);
                    Tank shooter = world.addTank(new GridPoint2(0, 0), duration);
                    Tank moving = world.addTank(new GridPoint2(0, 1), duration);
                    moving.takeDamage(slowdown == 2 ? 30 : 86);
                    moving.tryMove(Direction.RIGHT);
                    shooter.shoot();

                    world.update(duration * slowdown);

                    assertFalse("duration=" + duration + ", bullet=" + bulletDuration
                            + ", slowdown=" + slowdown + ", progress=" + moving.getMovementProgress(),
                            moving.isMoving());
                    assertEquals(new GridPoint2(1, 1), moving.getCoordinates());
                    assertTrue(world.canMoveTo(new GridPoint2(0, 1)));
                }
            }
        }
    }

    @Test
    public void aSingleHitCanSkipMediumStateAndDisableAnAlreadySelectedShot() {
        TankWorld world = world(100);
        Tank tank = world.addTank(new GridPoint2(0, 0), 1f);
        Command shot = new ShootCommand(tank);
        tank.takeDamage(86);
        shot.execute();
        tank.update(10f);
        shot.execute();
        assertSame(HeavyTankState.INSTANCE, tank.getState());
        assertTrue(world.getBullets().isEmpty());
    }

    @Test
    public void mediumStateKeepsTheOriginalReloadInterval() {
        TankWorld world = world(100);
        Tank tank = world.addTank(new GridPoint2(0, 0), 1f);
        tank.takeDamage(30);
        tank.shoot();
        world.update(0.2f);
        tank.shoot();
        assertEquals(1, world.getBullets().size());
        world.update(0.2f);
        tank.shoot();
        assertEquals(2, world.getBullets().size());
    }

    @Test
    public void zeroInitialHealthIsHeavyAndDestructionStillOccursStrictlyBelowZero() {
        TankWorld world = world(0);
        Tank tank = world.addTank(new GridPoint2(0, 0), 1f);
        assertSame(HeavyTankState.INSTANCE, tank.getState());
        assertTrue(tank.isActive());
        tank.shoot();
        assertTrue(world.getBullets().isEmpty());
        tank.tryMove(Direction.RIGHT);
        tank.takeDamage(1);
        tank.setState(LightTankState.INSTANCE);
        tank.shoot();
        tank.tryMove(Direction.UP);
        assertFalse(tank.isActive());
        assertFalse(world.contains(tank));
        assertTrue(world.canMoveTo(new GridPoint2(0, 0)));
        assertTrue(world.canMoveTo(new GridPoint2(1, 0)));
        assertTrue(world.getBullets().isEmpty());
    }

    @Test
    public void newStateCanDoubleMovementAndReloadWithoutResettingProgress() {
        TankWorld world = world(100);
        Tank tank = world.addTank(new GridPoint2(0, 0), 1f);
        tank.tryMove(Direction.RIGHT);
        tank.shoot();
        world.update(0.1f);
        TankState booster = new TankState() {
            @Override
            public double movementSpeedMultiplier() {
                return 2d;
            }

            @Override
            public double reloadSpeedMultiplier() {
                return 2d;
            }
        };
        tank.setState(booster);
        assertEquals(0.1f, tank.getMovementProgress(), 0.0001f);
        tank.shoot();
        assertEquals(1, world.getBullets().size());
        world.update(0.15f);
        tank.shoot();
        assertEquals(2, world.getBullets().size());
        assertEquals(0.4f, tank.getMovementProgress(), 0.0001f);
        world.update(0.1f);
        tank.shoot();
        assertEquals(2, world.getBullets().size());
        world.update(0.1f);
        tank.shoot();
        assertEquals(3, world.getBullets().size());
        world.update(0.1f);
        assertFalse(tank.isMoving());
        assertEquals(new GridPoint2(1, 0), tank.getCoordinates());

        tank.takeDamage(30);
        assertSame(MediumTankState.INSTANCE, tank.getState());
    }

    @Test
    public void aNewStateCanDefineItsOwnTransitionAfterDamage() {
        Tank tank = world(100).addTank(new GridPoint2(0, 0), 1f);
        TankState booster = new TankState() {
            @Override
            public TankState onHealthChanged(int health, int maxHealth) {
                return this;
            }
        };
        tank.setState(booster);
        tank.takeDamage(30);
        assertSame(booster, tank.getState());
        assertEquals(70, tank.getHealth());
    }

    @Test
    public void invalidStateCannotChangeTheCurrentBehaviour() {
        Tank tank = world(100).addTank(new GridPoint2(0, 0), 1f);
        assertThrows(NullPointerException.class, () -> tank.setState(null));
        for (double speed : new double[]{0d, -1d, Double.NaN, Double.POSITIVE_INFINITY}) {
            assertThrows(IllegalArgumentException.class, () -> tank.setState(new TankState() {
                @Override
                public double movementSpeedMultiplier() {
                    return speed;
                }
            }));
            assertThrows(IllegalArgumentException.class, () -> tank.setState(new TankState() {
                @Override
                public double reloadSpeedMultiplier() {
                    return speed;
                }
            }));
            assertSame(LightTankState.INSTANCE, tank.getState());
        }
    }

    private TankWorld world(int initialHealth) {
        return new TankWorld(new Field(20, 1, Collections.emptyList()),
                initialHealth, BulletSettings.DEFAULT, Tank.DEFAULT_SHOT_INTERVAL);
    }
}
