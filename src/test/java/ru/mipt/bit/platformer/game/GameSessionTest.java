package ru.mipt.bit.platformer.game;

import com.badlogic.gdx.math.GridPoint2;
import org.junit.Test;
import ru.mipt.bit.platformer.input.DirectionKeyBinding;
import ru.mipt.bit.platformer.input.MovementInputAction;
import ru.mipt.bit.platformer.model.Direction;
import ru.mipt.bit.platformer.model.Field;
import ru.mipt.bit.platformer.model.Tank;
import ru.mipt.bit.platformer.input.InputAction;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class GameSessionTest {

    @Test
    public void integratesMovementAndCollisionWithoutGraphicsContext() {
        Field field = new Field(4, 4, Collections.singletonList(coordinates -> coordinates.x == 3));
        Tank tank = new Tank(new GridPoint2(1, 1), 0.4f, field);
        InputAction input = new MovementInputAction(tank, key -> true,
                Collections.singletonList(new DirectionKeyBinding(Direction.RIGHT, 1)));
        List<GridPoint2> renderedPositions = new ArrayList<>();
        GameView view = new GameView() {
            @Override
            public void render() {
                renderedPositions.add(tank.getCoordinates());
            }

            @Override
            public void dispose() {
            }
        };
        GameSession session = new GameSession(input, tank, view);

        session.render(0.2f);
        session.render(0.2f);
        session.render(0.4f);

        assertEquals(Arrays.asList(new GridPoint2(1, 1), new GridPoint2(2, 1), new GridPoint2(2, 1)),
                renderedPositions);
    }

    @Test
    public void runsInjectedInputWorldAndViewInOrderAndDisposesView() {
        List<String> calls = new ArrayList<>();
        InputAction input = () -> calls.add("input");
        Updatable world = deltaTime -> calls.add("update " + deltaTime);
        GameView view = new GameView() {
            @Override
            public void render() {
                calls.add("render");
            }

            @Override
            public void dispose() {
                calls.add("dispose");
            }
        };
        GameSession session = new GameSession(input, world, view);

        session.render(0.25f);
        session.dispose();

        assertEquals(Arrays.asList("input", "update 0.25", "render", "dispose"), calls);
    }
}
