package ru.mipt.bit.platformer.input;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.math.GridPoint2;
import org.junit.After;
import org.junit.Test;
import ru.mipt.bit.platformer.model.Direction;
import ru.mipt.bit.platformer.model.Field;
import ru.mipt.bit.platformer.model.Tank;

import java.lang.reflect.Proxy;
import java.util.Collections;

import static org.junit.Assert.assertEquals;

public class MovementInputActionTest {

    private final Input originalInput = Gdx.input;

    @After
    public void restoreInput() {
        Gdx.input = originalInput;
    }

    @Test
    public void mapsArrowAndLetterKeysToDirections() {
        int[] keys = {Keys.UP, Keys.W, Keys.LEFT, Keys.A, Keys.DOWN, Keys.S, Keys.RIGHT, Keys.D};
        Direction[] directions = {
                Direction.UP, Direction.UP,
                Direction.LEFT, Direction.LEFT,
                Direction.DOWN, Direction.DOWN,
                Direction.RIGHT, Direction.RIGHT
        };

        for (int index = 0; index < keys.length; index++) {
            Tank tank = createTank();
            Gdx.input = inputWithPressedKey(keys[index]);

            new MovementInputAction(tank, emptyField()).handle();

            assertEquals(directions[index], tank.getDirection());
            assertEquals(directions[index].move(new GridPoint2(2, 2)), tank.getDestinationCoordinates());
        }
    }

    @Test
    public void ignoresDirectionChangesWhileTankIsMoving() {
        Tank tank = createTank();
        Field field = emptyField();
        tank.tryMove(Direction.UP, field);
        Gdx.input = inputWithPressedKey(Keys.LEFT);

        new MovementInputAction(tank, field).handle();

        assertEquals(Direction.UP, tank.getDirection());
        assertEquals(new GridPoint2(2, 3), tank.getDestinationCoordinates());
    }

    @Test
    public void doesNothingWhenNoMovementKeyIsPressed() {
        Tank tank = createTank();
        Gdx.input = inputWithPressedKey(-1);

        new MovementInputAction(tank, emptyField()).handle();

        assertEquals(new GridPoint2(2, 2), tank.getDestinationCoordinates());
    }

    @Test
    public void usesFirstDirectionWhenSeveralKeysArePressed() {
        Tank tank = createTank();
        Gdx.input = inputWithPressedKeys(Keys.UP, Keys.RIGHT);

        new MovementInputAction(tank, emptyField()).handle();

        assertEquals(Direction.UP, tank.getDirection());
    }

    private Tank createTank() {
        return new Tank(new GridPoint2(2, 2));
    }

    private Field emptyField() {
        return new Field(5, 5, Collections.emptyList());
    }

    private Input inputWithPressedKey(int pressedKey) {
        return inputWithPressedKeys(pressedKey);
    }

    private Input inputWithPressedKeys(int... pressedKeys) {
        return (Input) Proxy.newProxyInstance(
                Input.class.getClassLoader(),
                new Class<?>[]{Input.class},
                (proxy, method, arguments) -> {
                    if (method.getName().equals("isKeyPressed")) {
                        int requestedKey = (Integer) arguments[0];
                        for (int pressedKey : pressedKeys) {
                            if (requestedKey == pressedKey) {
                                return true;
                            }
                        }
                        return false;
                    }
                    return defaultValue(method.getReturnType());
                }
        );
    }

    private Object defaultValue(Class<?> type) {
        if (type == boolean.class) {
            return false;
        }
        if (type == int.class) {
            return 0;
        }
        if (type == long.class) {
            return 0L;
        }
        if (type == float.class) {
            return 0f;
        }
        return null;
    }
}
