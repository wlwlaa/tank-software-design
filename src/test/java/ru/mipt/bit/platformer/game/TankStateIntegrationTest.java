package ru.mipt.bit.platformer.game;

import com.badlogic.gdx.math.GridPoint2;
import org.junit.Test;
import ru.mipt.bit.platformer.input.DirectionKeyBinding;
import ru.mipt.bit.platformer.level.LevelDefinition;
import ru.mipt.bit.platformer.model.Direction;
import ru.mipt.bit.platformer.model.Tank;
import ru.mipt.bit.platformer.model.state.HeavyTankState;
import ru.mipt.bit.platformer.model.state.MediumTankState;

import java.util.Collections;
import java.util.Random;

import static org.junit.Assert.*;

public class TankStateIntegrationTest {

    @Test
    public void hitBeforeAiCommandExecutionDisablesItsSelectedShotInTheSameFrame() {
        int shootKey = 1;
        TankSimulation simulation = new TankSimulation(
                new LevelDefinition(3, 1, Collections.emptyList(), new GridPoint2(0, 0)),
                1, 1f, key -> key == shootKey, Collections.emptyList(), shootKey, spawningThenShooting());
        Tank ai = simulation.getAiTanks().get(0);
        ai.tryMove(Direction.LEFT); // Face the player, who blocks this cell.
        ai.takeDamage(74);
        assertSame(MediumTankState.INSTANCE, ai.getState());

        simulation.handle();

        assertSame(HeavyTankState.INSTANCE, ai.getState());
        assertEquals(1, ai.getHealth());
        assertTrue(ai.isActive());
        assertTrue(simulation.getWorld().contains(ai));
        assertEquals(100, simulation.getPlayerTank().getHealth());
        assertTrue(simulation.getWorld().getBullets().isEmpty());
    }

    @Test
    public void aiHitChangesThePlayersMovementSpeedBeforeTheWorldUpdate() {
        TankSimulation simulation = new TankSimulation(
                new LevelDefinition(3, 2, Collections.emptyList(), new GridPoint2(0, 0)),
                1, 1f, key -> true,
                Collections.singletonList(new DirectionKeyBinding(Direction.UP, 1)), spawningThenShooting());
        Tank player = simulation.getPlayerTank();
        player.takeDamage(74);
        simulation.getAiTanks().get(0).tryMove(Direction.LEFT);

        simulation.handle();
        simulation.update(1f);

        assertEquals(1, player.getHealth());
        assertSame(HeavyTankState.INSTANCE, player.getState());
        assertEquals(1f / 3f, player.getMovementProgress(), 0.0001f);
        assertEquals(new GridPoint2(0, 1), player.getDestinationCoordinates());
        assertTrue(player.occupies(new GridPoint2(0, 0)));
        assertTrue(player.occupies(new GridPoint2(0, 1)));
        simulation.update(2f);
        assertFalse(player.isMoving());
        assertEquals(new GridPoint2(0, 1), player.getCoordinates());
    }

    private Random spawningThenShooting() {
        return new Random() {
            private boolean spawning = true;

            @Override
            public int nextInt(int bound) {
                if (spawning) {
                    spawning = false;
                    return 1;
                }
                return 4;
            }
        };
    }
}
