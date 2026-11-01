package ru.mipt.bit.platformer.game;

import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.math.GridPoint2;
import org.junit.Test;
import ru.mipt.bit.platformer.control.Command;
import ru.mipt.bit.platformer.control.KeyboardMovementStrategy;
import ru.mipt.bit.platformer.control.RandomMovementStrategy;
import ru.mipt.bit.platformer.control.ShootCommand;
import ru.mipt.bit.platformer.control.TankController;
import ru.mipt.bit.platformer.input.DirectionKeyBinding;
import ru.mipt.bit.platformer.level.LevelDefinition;
import ru.mipt.bit.platformer.model.Bullet;
import ru.mipt.bit.platformer.model.Direction;
import ru.mipt.bit.platformer.model.Field;
import ru.mipt.bit.platformer.model.LevelObject;
import ru.mipt.bit.platformer.model.Tank;
import ru.mipt.bit.platformer.model.TankWorld;
import ru.mipt.bit.platformer.model.Tree;
import ru.mipt.bit.platformer.model.WorldListener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import static org.junit.Assert.*;

public class ShootingIntegrationTest {

    @Test
    public void sessionSelectsAllCommandsThenExecutesThenUpdatesThenDraws() {
        List<String> calls = new ArrayList<>();
        Tree tree = new Tree(new GridPoint2(3, 0)) {
            @Override
            public void update(float deltaTime) {
                calls.add("update");
            }
        };
        LevelDefinition level = new LevelDefinition(4, 1, Collections.singletonList(tree), new GridPoint2(0, 0));
        Random random = new Random() {
            private int requests;

            @Override
            public int nextInt(int bound) {
                if (requests++ == 0) {
                    return 1; // AI spawn next to the player.
                }
                calls.add("select AI");
                return 4; // Shoot.
            }
        };
        TankSimulation simulation = new TankSimulation(level, 1, 0.4f, key -> {
            calls.add("select player");
            return key == Keys.SPACE;
        }, Collections.emptyList(), Keys.SPACE, random);
        Tank ai = simulation.getAiTanks().get(0);
        ai.tryMove(Direction.LEFT); // Turn towards the player; movement is blocked.
        simulation.getWorld().addListener(new WorldListener() {
            @Override
            public void objectAdded(LevelObject object) {
                if (object instanceof Bullet) {
                    calls.add("execute shot");
                }
            }

            @Override
            public void objectRemoved(LevelObject object) { }
        });
        GameView view = new GameView() {
            @Override
            public void render() {
                assertEquals(75, simulation.getPlayerTank().getHealth());
                assertEquals(75, ai.getHealth());
                calls.add("render");
            }

            @Override
            public void dispose() { }
        };

        new GameSession(simulation, simulation, view).render(0f);

        assertEquals(Arrays.asList("select player", "select AI", "execute shot", "execute shot",
                "update", "render"), calls);
    }

    @Test
    public void killedAiCannotExecuteItsAlreadySelectedShotOrRequestMoreCommands() {
        int[] decisions = {0};
        Random random = new Random() {
            @Override
            public int nextInt(int bound) {
                return decisions[0]++ == 0 ? 1 : 4;
            }
        };
        boolean[] space = {true};
        TankSimulation simulation = new TankSimulation(
                new LevelDefinition(3, 1, Collections.emptyList(), new GridPoint2(0, 0)),
                1, 0.4f, key -> space[0] && key == Keys.SPACE,
                Collections.emptyList(), Keys.SPACE, random);
        Tank ai = simulation.getAiTanks().get(0);
        ai.tryMove(Direction.LEFT);
        ai.takeDamage(100);

        simulation.handle();
        space[0] = false;
        simulation.update(1f);
        simulation.handle();

        assertEquals(2, decisions[0]);
        assertEquals(100, simulation.getPlayerTank().getHealth());
        assertFalse(simulation.getWorld().contains(ai));
        assertTrue(simulation.getWorld().getBullets().isEmpty());
    }

    @Test
    public void spaceShootsWhileMovingAndKeepsMovementInProgress() {
        TankWorld world = new TankWorld(new Field(8, 2, Collections.emptyList()));
        Tank tank = world.addTank(new GridPoint2(1, 0), 0.4f);
        tank.tryMove(Direction.RIGHT);
        world.update(0.1f);
        TankController controller = new TankController(tank, new KeyboardMovementStrategy(
                key -> true, Collections.singletonList(new DirectionKeyBinding(Direction.UP, Keys.UP)), Keys.SPACE));

        controller.handle();

        assertEquals(1, world.getBullets().size());
        assertEquals(Direction.RIGHT, world.getBullets().get(0).getDirection());
        assertEquals(new GridPoint2(2, 0), tank.getDestinationCoordinates());
        assertEquals(0.25f, tank.getMovementProgress(), 0.0001f);
        assertEquals(100, tank.getHealth());
    }

    @Test
    public void randomAiCanChooseEveryDirectionAndShootingWithoutExecutingDuringSelection() {
        for (int choice = 0; choice < 5; choice++) {
            final int selected = choice;
            TankWorld world = new TankWorld(new Field(7, 7, Collections.emptyList()));
            Tank tank = world.addTank(new GridPoint2(3, 3), 0.4f);
            RandomMovementStrategy strategy = new RandomMovementStrategy(new Random() {
                @Override
                public int nextInt(int bound) {
                    assertEquals(5, bound);
                    return selected;
                }
            });

            Command command = strategy.nextCommand(tank).get();
            assertFalse(tank.isMoving());
            assertTrue(world.getBullets().isEmpty());
            command.execute();

            if (choice == 4) {
                assertTrue(command instanceof ShootCommand);
                assertEquals(1, world.getBullets().size());
            } else {
                assertEquals(Direction.values()[choice], tank.getDirection());
                assertTrue(tank.isMoving());
            }
        }
    }
}
