package ru.mipt.bit.platformer.level;

import com.badlogic.gdx.math.GridPoint2;
import org.junit.Test;
import ru.mipt.bit.platformer.model.Direction;
import ru.mipt.bit.platformer.model.Field;
import ru.mipt.bit.platformer.model.Tank;
import ru.mipt.bit.platformer.model.TankWorld;
import ru.mipt.bit.platformer.model.Tree;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

public class RandomTankGeneratorTest {

    @Test
    public void generatesRequestedTanksInDistinctFreeCellsInsideLevel() {
        for (long seed = 0; seed < 100; seed++) {
            TankWorld world = emptyWorld(8, 7);
            List<Tank> tanks = new RandomTankGenerator(new Random(seed)).generate(world, 20, 0.5f);

            assertEquals(20, tanks.size());
            assertEquals(20, new HashSet<>(coordinates(tanks)).size());
            for (Tank tank : tanks) {
                GridPoint2 cell = tank.getCoordinates();
                assertTrue(cell.x >= 0 && cell.x < 8);
                assertTrue(cell.y >= 0 && cell.y < 7);
                assertTrue(world.getTanks().contains(tank));
            }
        }
    }

    @Test
    public void sameSeedReproducesTankLayoutAndDifferentSeedsVaryIt() {
        List<Tank> first = new RandomTankGenerator(new Random(42)).generate(emptyWorld(8, 7), 20, 1f);
        List<Tank> second = new RandomTankGenerator(new Random(42)).generate(emptyWorld(8, 7), 20, 1f);
        assertEquals(coordinates(first), coordinates(second));

        Set<List<GridPoint2>> layouts = new HashSet<>();
        for (long seed = 0; seed < 20; seed++) {
            List<Tank> tanks = new RandomTankGenerator(new Random(seed)).generate(emptyWorld(8, 7), 20, 1f);
            layouts.add(coordinates(tanks));
        }
        assertTrue(layouts.size() > 1);
    }

    @Test
    public void excludesObstaclesPlayerAndPreviouslySpawnedTanks() {
        TankWorld world = new TankWorld(new Field(3, 2,
                Arrays.asList(new Tree(new GridPoint2(1, 0)), new Tree(new GridPoint2(2, 1)))));
        Tank player = world.addTank(new GridPoint2(0, 0), 1f);
        Tank previousTank = world.addTank(new GridPoint2(2, 0), 1f);

        List<Tank> tanks = new RandomTankGenerator(new Random(42)).generate(world, 100, 1f);

        assertEquals(new HashSet<>(Arrays.asList(new GridPoint2(0, 1), new GridPoint2(1, 1))),
                new HashSet<>(coordinates(tanks)));
        assertEquals(4, world.getTanks().size());
        assertSame(player, world.getTanks().get(0));
        assertSame(previousTank, world.getTanks().get(1));
    }

    @Test
    public void excludesBothCellsOfMovingTankAndCanUseReleasedCellAfterArrival() {
        TankWorld world = emptyWorld(3, 1);
        Tank movingTank = world.addTank(new GridPoint2(0, 0), 1f);
        movingTank.tryMove(Direction.RIGHT);
        movingTank.update(0.5f);
        assertTrue(movingTank.isMoving());

        RandomTankGenerator generator = new RandomTankGenerator(new Random(42));
        List<Tank> first = generator.generate(world, 3, 1f);
        assertEquals(Collections.singletonList(new GridPoint2(2, 0)), coordinates(first));

        movingTank.update(0.5f);
        List<Tank> second = generator.generate(world, 3, 1f);
        assertEquals(Collections.singletonList(new GridPoint2(0, 0)), coordinates(second));
        assertEquals(3, world.getTanks().size());
    }

    @Test
    public void supportsSingleCellAndReturnsEmptyWhenFieldIsFull() {
        TankWorld world = emptyWorld(1, 1);
        RandomTankGenerator generator = new RandomTankGenerator(new Random(42));

        assertEquals(Collections.singletonList(new GridPoint2(0, 0)),
                coordinates(generator.generate(world, Integer.MAX_VALUE, 1f)));
        assertTrue(generator.generate(world, Integer.MAX_VALUE, 1f).isEmpty());
        assertEquals(1, world.getTanks().size());
    }

    @Test
    public void zeroCountLeavesWorldAndRandomSourceUntouched() {
        TankWorld world = emptyWorld(3, 2);
        world.addTank(new GridPoint2(0, 0), 1f);
        Random random = new Random() {
            @Override
            public int nextInt(int bound) {
                throw new AssertionError("Zero count must not draw random cells");
            }
        };

        assertTrue(new RandomTankGenerator(random).generate(world, 0, 1f).isEmpty());
        assertEquals(1, world.getTanks().size());
    }

    @Test
    public void suppliedDurationControlsGeneratedTankMovement() {
        TankWorld world = emptyWorld(2, 1);
        Tank tank = new RandomTankGenerator(new Random(42)).generate(world, 1, 2f).get(0);
        Direction direction = tank.getCoordinates().x == 0 ? Direction.RIGHT : Direction.LEFT;

        tank.tryMove(direction);
        tank.update(1f);

        assertTrue(tank.isMoving());
        assertEquals(0.5f, tank.getMovementProgress(), 0f);
        tank.update(1f);
        assertFalse(tank.isMoving());
    }

    @Test
    public void samplesEachCellAtMostOnceEvenWhenRandomAlwaysSelectsSameIndex() {
        TankWorld world = new TankWorld(new Field(3, 1,
                Collections.singletonList(new Tree(new GridPoint2(0, 0)))));
        Random random = new Random() {
            private int remainingDraws = 3;

            @Override
            public int nextInt(int bound) {
                assertTrue("Generation must terminate after at most one draw per cell", remainingDraws-- > 0);
                return 0;
            }
        };

        List<Tank> tanks = new RandomTankGenerator(random).generate(world, 100, 1f);

        assertEquals(new HashSet<>(Arrays.asList(new GridPoint2(1, 0), new GridPoint2(2, 0))),
                new HashSet<>(coordinates(tanks)));
    }

    @Test
    public void largeSparseLevelDoesNotRequireOneObjectPerCell() {
        TankWorld world = emptyWorld(Integer.MAX_VALUE, 1);

        List<Tank> tanks = new RandomTankGenerator(new Random(42)).generate(world, 1, 1f);

        assertEquals(1, tanks.size());
        assertTrue(tanks.get(0).getCoordinates().x >= 0);
        assertEquals(0, tanks.get(0).getCoordinates().y);
    }

    @Test
    public void rejectsNegativeCountAndInvalidDurationEvenWhenNoTanksCanBeAdded() {
        RandomTankGenerator generator = new RandomTankGenerator(new Random(42));
        TankWorld fullWorld = emptyWorld(1, 1);
        fullWorld.addTank(new GridPoint2(0, 0), 1f);

        assertThrows(IllegalArgumentException.class, () -> generator.generate(fullWorld, -1, 1f));
        float[] invalidDurations = {0f, -1f, Float.NaN, Float.POSITIVE_INFINITY, Float.NEGATIVE_INFINITY};
        for (float duration : invalidDurations) {
            assertThrows(IllegalArgumentException.class, () -> generator.generate(fullWorld, 1, duration));
            assertThrows(IllegalArgumentException.class, () -> generator.generate(fullWorld, 0, duration));
        }
    }

    @Test
    public void rejectsCellCountOverflow() {
        RandomTankGenerator generator = new RandomTankGenerator(new Random(42));
        assertThrows(IllegalArgumentException.class, () -> generator.generate(emptyWorld(50_000, 50_000), 1, 1f));
    }

    @Test
    public void rejectsNullDependencies() {
        assertThrows(NullPointerException.class, () -> new RandomTankGenerator(null));
        assertThrows(NullPointerException.class,
                () -> new RandomTankGenerator(new Random(42)).generate(null, 1, 1f));
    }

    private TankWorld emptyWorld(int width, int height) {
        return new TankWorld(new Field(width, height, Collections.emptyList()));
    }

    private List<GridPoint2> coordinates(List<Tank> tanks) {
        List<GridPoint2> result = new ArrayList<>();
        for (Tank tank : tanks) {
            result.add(tank.getCoordinates());
        }
        return result;
    }
}
