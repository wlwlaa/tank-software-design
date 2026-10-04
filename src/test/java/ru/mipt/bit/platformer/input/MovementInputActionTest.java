package ru.mipt.bit.platformer.input;

import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.math.GridPoint2;
import org.junit.Test;
import ru.mipt.bit.platformer.model.Direction;
import ru.mipt.bit.platformer.model.Field;
import ru.mipt.bit.platformer.model.MovementTarget;
import ru.mipt.bit.platformer.model.Tank;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class MovementInputActionTest {

    @Test
    public void supportsArrowAndLetterBindings() {
        int[] keys = {Keys.UP, Keys.W, Keys.LEFT, Keys.A, Keys.DOWN, Keys.S, Keys.RIGHT, Keys.D};
        Direction[] directions = {
                Direction.UP, Direction.UP,
                Direction.LEFT, Direction.LEFT,
                Direction.DOWN, Direction.DOWN,
                Direction.RIGHT, Direction.RIGHT
        };
        for (int index = 0; index < keys.length; index++) {
            Tank tank = createTank();
            action(tank, pressedKeys(keys[index])).handle();

            assertEquals(directions[index], tank.getDirection());
            assertEquals(directions[index].move(new GridPoint2(2, 2)), tank.getDestinationCoordinates());
        }
    }

    @Test
    public void ignoresDirectionChangesWhileTankIsMoving() {
        Tank tank = createTank();
        tank.tryMove(Direction.UP);

        action(tank, pressedKeys(Keys.LEFT)).handle();

        assertEquals(Direction.UP, tank.getDirection());
        assertEquals(new GridPoint2(2, 3), tank.getDestinationCoordinates());
    }

    @Test
    public void doesNothingWhenNoMovementKeyIsPressed() {
        Tank tank = createTank();

        action(tank, pressedKeys()).handle();

        assertEquals(new GridPoint2(2, 2), tank.getDestinationCoordinates());
    }

    @Test
    public void usesFirstBindingWhenSeveralKeysArePressed() {
        Tank tank = createTank();

        action(tank, pressedKeys(Keys.UP, Keys.RIGHT)).handle();

        assertEquals(Direction.UP, tank.getDirection());
    }

    @Test
    public void supportsCustomBindingsAndOtherMovementTargets() {
        List<Direction> movements = new ArrayList<>();
        MovementTarget target = new MovementTarget() {
            @Override
            public boolean isMoving() {
                return false;
            }

            @Override
            public void tryMove(Direction direction) {
                movements.add(direction);
            }
        };
        List<DirectionKeyBinding> bindings = new ArrayList<>();
        int[] keys = {Keys.SPACE};
        bindings.add(new DirectionKeyBinding(Direction.LEFT, keys));
        MovementInputAction action = new MovementInputAction(target, pressedKeys(Keys.SPACE), bindings);
        bindings.clear();
        keys[0] = Keys.UP;

        action.handle();

        assertEquals(Collections.singletonList(Direction.LEFT), movements);
    }

    private Tank createTank() {
        return new Tank(new GridPoint2(2, 2), 0.4f, new Field(5, 5, Collections.emptyList()));
    }

    private MovementInputAction action(Tank tank, KeyState keyState) {
        return new MovementInputAction(tank, keyState, Arrays.asList(
                new DirectionKeyBinding(Direction.UP, Keys.UP, Keys.W),
                new DirectionKeyBinding(Direction.LEFT, Keys.LEFT, Keys.A),
                new DirectionKeyBinding(Direction.DOWN, Keys.DOWN, Keys.S),
                new DirectionKeyBinding(Direction.RIGHT, Keys.RIGHT, Keys.D)
        ));
    }

    private KeyState pressedKeys(int... keys) {
        return requestedKey -> {
            for (int key : keys) {
                if (key == requestedKey) {
                    return true;
                }
            }
            return false;
        };
    }
}
