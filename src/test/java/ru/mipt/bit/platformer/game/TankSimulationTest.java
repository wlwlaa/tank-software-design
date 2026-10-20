package ru.mipt.bit.platformer.game;

import com.badlogic.gdx.math.GridPoint2;
import org.junit.Test;
import ru.mipt.bit.platformer.input.DirectionKeyBinding;
import ru.mipt.bit.platformer.input.KeyState;
import ru.mipt.bit.platformer.level.FileLevelLoader;
import ru.mipt.bit.platformer.level.LevelDefinition;
import ru.mipt.bit.platformer.level.RandomLevelGenerator;
import ru.mipt.bit.platformer.model.Direction;
import ru.mipt.bit.platformer.model.Field;
import ru.mipt.bit.platformer.model.Tank;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class TankSimulationTest {

    private static final float MOVEMENT_DURATION = 0.4f;

    @Test
    public void sessionKeepsPlayerOnKeyboardAndMovesAiWithoutPressedKeys() {
        MutableKeys keys = new MutableKeys();
        ScriptedRandom random = new ScriptedRandom(10, Direction.UP.ordinal(), Direction.LEFT.ordinal());
        TankSimulation simulation = simulation(emptyLevel(6, 3, 1, 1), 1, keys, random);
        Tank player = simulation.getPlayerTank();
        Tank ai = simulation.getAiTanks().get(0);
        List<List<GridPoint2>> renderedPositions = new ArrayList<>();
        boolean[] disposed = {false};
        GameSession session = new GameSession(simulation, simulation, new GameView() {
            @Override
            public void render() {
                renderedPositions.add(Arrays.asList(player.getCoordinates(), ai.getCoordinates()));
            }

            @Override
            public void dispose() {
                disposed[0] = true;
            }
        });

        session.render(MOVEMENT_DURATION);
        keys.direction = Direction.RIGHT;
        session.render(MOVEMENT_DURATION);
        session.dispose();

        assertEquals(Arrays.asList(
                Arrays.asList(new GridPoint2(1, 1), new GridPoint2(4, 2)),
                Arrays.asList(new GridPoint2(2, 1), new GridPoint2(3, 2))), renderedPositions);
        assertTrue(disposed[0]);
    }

    @Test
    public void aiKeepsItsCommandDuringMovementAndChoosesAgainAfterArrival() {
        ScriptedRandom random = new ScriptedRandom(9, Direction.RIGHT.ordinal(), Direction.UP.ordinal());
        TankSimulation simulation = simulation(emptyLevel(6, 3, 0, 0), 1, new MutableKeys(), random);
        GameSession session = new GameSession(simulation, simulation, emptyView());
        Tank ai = simulation.getAiTanks().get(0);

        session.render(0.2f);
        assertEquals(2, random.calls);
        assertEquals(new GridPoint2(3, 1), ai.getCoordinates());
        assertEquals(new GridPoint2(4, 1), ai.getDestinationCoordinates());

        session.render(0.2f);
        assertEquals(2, random.calls);
        assertEquals(new GridPoint2(4, 1), ai.getCoordinates());
        assertFalse(ai.isMoving());

        session.render(0.2f);
        assertEquals(3, random.calls);
        assertEquals(new GridPoint2(4, 1), ai.getCoordinates());
        assertEquals(new GridPoint2(4, 2), ai.getDestinationCoordinates());
        assertTrue(ai.isMoving());
    }

    @Test
    public void playerCannotEnterAiSourceOrDestinationUntilTheCellIsReleased() {
        for (int blockedX : new int[]{1, 2}) {
            MutableKeys keys = new MutableKeys();
            ScriptedRandom random = new ScriptedRandom(1, Direction.RIGHT.ordinal(), Direction.RIGHT.ordinal());
            TankSimulation simulation = simulation(emptyLevel(4, 2, blockedX, 1), 1, keys, random);
            Tank player = simulation.getPlayerTank();
            Tank ai = simulation.getAiTanks().get(0);

            simulation.handle();
            simulation.update(0.2f);
            assertTrue(ai.isMoving());
            assertEquals(new GridPoint2(1, 0), ai.getCoordinates());
            assertEquals(new GridPoint2(2, 0), ai.getDestinationCoordinates());

            keys.direction = Direction.DOWN;
            simulation.handle();
            assertFalse(player.isMoving());
            assertEquals(new GridPoint2(blockedX, 1), player.getDestinationCoordinates());

            simulation.update(0.2f);
            simulation.handle();

            assertEquals(blockedX == 1, player.isMoving());
            assertEquals(new GridPoint2(blockedX, blockedX == 1 ? 0 : 1), player.getDestinationCoordinates());
        }
    }

    @Test
    public void aiCannotEnterMovingPlayersSourceOrDestinationUntilTheCellIsReleased() {
        for (int blockedX : new int[]{1, 2}) {
            MutableKeys keys = new MutableKeys();
            keys.direction = Direction.RIGHT;
            ScriptedRandom random = new ScriptedRandom(blockedX + 4,
                    Direction.DOWN.ordinal(), Direction.DOWN.ordinal(), Direction.DOWN.ordinal());
            TankSimulation simulation = simulation(emptyLevel(4, 2, 1, 0), 1, keys, random);
            Tank player = simulation.getPlayerTank();
            Tank ai = simulation.getAiTanks().get(0);

            simulation.handle();
            assertTrue(player.isMoving());
            assertEquals(new GridPoint2(1, 0), player.getCoordinates());
            assertEquals(new GridPoint2(2, 0), player.getDestinationCoordinates());
            assertFalse(ai.isMoving());

            simulation.update(0.2f);
            simulation.handle();
            assertFalse(ai.isMoving());
            assertEquals(new GridPoint2(blockedX, 1), ai.getDestinationCoordinates());

            simulation.update(0.2f);
            keys.direction = null;
            simulation.handle();

            assertEquals(blockedX == 1, ai.isMoving());
            assertEquals(new GridPoint2(blockedX, blockedX == 1 ? 0 : 1), ai.getDestinationCoordinates());
        }
    }

    @Test
    public void simultaneousPlayerAndAiCommandsCannotReserveTheSameEmptyCell() {
        MutableKeys keys = new MutableKeys();
        keys.direction = Direction.RIGHT;
        TankSimulation simulation = simulation(emptyLevel(3, 1, 0, 0), 1, keys,
                new ScriptedRandom(2, Direction.LEFT.ordinal(), Direction.LEFT.ordinal()));
        Tank player = simulation.getPlayerTank();
        Tank ai = simulation.getAiTanks().get(0);

        simulation.handle();

        assertTrue(player.isMoving());
        assertEquals(new GridPoint2(1, 0), player.getDestinationCoordinates());
        assertFalse(ai.isMoving());
        assertEquals(new GridPoint2(2, 0), ai.getDestinationCoordinates());

        simulation.update(0.2f);
        simulation.handle();

        assertFalse(ai.isMoving());
        assertEquals(new GridPoint2(2, 0), ai.getDestinationCoordinates());
    }

    @Test
    public void bothKeyboardAndAiCommandsStopAtEveryLevelBorder() {
        GridPoint2[] playerStarts = {
                new GridPoint2(0, 1), new GridPoint2(0, 0),
                new GridPoint2(0, 0), new GridPoint2(1, 0)
        };
        GridPoint2[] aiStarts = {
                new GridPoint2(1, 1), new GridPoint2(0, 1),
                new GridPoint2(1, 0), new GridPoint2(1, 1)
        };
        for (Direction direction : Direction.values()) {
            int index = direction.ordinal();
            MutableKeys keys = new MutableKeys();
            keys.direction = direction;
            GridPoint2 playerStart = playerStarts[index];
            GridPoint2 aiStart = aiStarts[index];
            TankSimulation simulation = simulation(emptyLevel(2, 2, playerStart.x, playerStart.y), 1, keys,
                    new ScriptedRandom(aiStart.y * 2 + aiStart.x, index));

            simulation.handle();
            simulation.update(1f);

            assertFalse(simulation.getPlayerTank().isMoving());
            assertFalse(simulation.getAiTanks().get(0).isMoving());
            assertEquals(playerStart, simulation.getPlayerTank().getCoordinates());
            assertEquals(aiStart, simulation.getAiTanks().get(0).getCoordinates());
        }
    }

    @Test
    public void fullFileRandomAndSingleCellLevelsRunWithoutRoomForAi() throws IOException {
        List<LevelDefinition> levels = Arrays.asList(
                FileLevelLoader.parse(new StringReader("TTTT\nTXTT\nTTTT")),
                new RandomLevelGenerator(4, 3, 11, new Random(42)).load(),
                emptyLevel(1, 1, 0, 0));
        for (LevelDefinition level : levels) {
            MutableKeys keys = new MutableKeys();
            TankSimulation simulation = simulation(level, 8, keys, new Random(73));

            assertTrue(simulation.getAiTanks().isEmpty());
            assertEquals(1, simulation.getWorld().getTanks().size());
            for (Direction direction : Direction.values()) {
                keys.direction = direction;
                simulation.handle();
                simulation.update(MOVEMENT_DURATION);

                assertEquals(level.getPlayerStart(), simulation.getPlayerTank().getCoordinates());
                assertFalse(simulation.getPlayerTank().isMoving());
            }
        }
    }

    @Test
    public void longSeededSessionsKeepEveryReservedCellInsideFreeAndExclusive() throws IOException {
        List<LevelDefinition> levels = Arrays.asList(
                FileLevelLoader.parse(new StringReader(
                        "________\n_T__T___\n____T___\n__X_____\nT____T__\n____T___")),
                new RandomLevelGenerator(20, 15, 50, new Random(93)).load());
        for (LevelDefinition level : levels) {
            MutableKeys keys = new MutableKeys();
            TankSimulation simulation = simulation(level, 6, keys, new Random(145));
            GameSession session = new GameSession(simulation, simulation, emptyView());
            List<GridPoint2> starts = new ArrayList<>();
            for (Tank tank : simulation.getAiTanks()) {
                starts.add(tank.getCoordinates());
            }
            boolean aiMoved = false;
            assertEquals(6, simulation.getAiTanks().size());
            assertReservationsValid(level, simulation);

            for (int frame = 0; frame < 1000; frame++) {
                keys.direction = Direction.values()[(frame / 12) % Direction.values().length];
                session.render(0.07f);
                assertReservationsValid(level, simulation);
                for (int index = 0; index < starts.size(); index++) {
                    if (!starts.get(index).equals(simulation.getAiTanks().get(index).getCoordinates())) {
                        aiMoved = true;
                    }
                }
            }

            assertTrue("AI should make progress during the simulation", aiMoved);
        }
    }

    private TankSimulation simulation(LevelDefinition level, int aiCount, KeyState keys, Random random) {
        List<DirectionKeyBinding> bindings = new ArrayList<>();
        for (Direction direction : Direction.values()) {
            bindings.add(new DirectionKeyBinding(direction, direction.ordinal()));
        }
        return new TankSimulation(level, aiCount, MOVEMENT_DURATION, keys, bindings, random);
    }

    private LevelDefinition emptyLevel(int width, int height, int playerX, int playerY) {
        return new LevelDefinition(width, height, Collections.emptyList(), new GridPoint2(playerX, playerY));
    }

    private GameView emptyView() {
        return new GameView() {
            @Override
            public void render() {
            }

            @Override
            public void dispose() {
            }
        };
    }

    private void assertReservationsValid(LevelDefinition level, TankSimulation simulation) {
        Field field = new Field(level.getWidth(), level.getHeight(), level.getObstacles());
        Set<GridPoint2> occupied = new HashSet<>();
        for (Tank tank : simulation.getWorld().getTanks()) {
            Set<GridPoint2> tankCells = new HashSet<>(Arrays.asList(
                    tank.getCoordinates(), tank.getDestinationCoordinates()));
            for (GridPoint2 cell : tankCells) {
                assertTrue("Tank is outside the level or inside an obstacle at " + cell, field.canMoveTo(cell));
                assertTrue("Tanks overlap at " + cell, occupied.add(cell));
            }
        }
    }

    private static class MutableKeys implements KeyState {

        private Direction direction;

        @Override
        public boolean isPressed(int key) {
            return direction != null && key == direction.ordinal();
        }
    }

    private static class ScriptedRandom extends Random {

        private final int[] choices;
        private int calls;

        private ScriptedRandom(int... choices) {
            this.choices = choices;
        }

        @Override
        public int nextInt(int bound) {
            assertTrue("Unexpected random decision", calls < choices.length);
            int choice = choices[calls++];
            assertTrue("Scripted choice must fit requested bound", choice >= 0 && choice < bound);
            return choice;
        }
    }
}
