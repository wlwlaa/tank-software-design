package ru.mipt.bit.platformer.render;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.GridPoint2;
import com.badlogic.gdx.math.Rectangle;
import org.junit.Test;
import ru.mipt.bit.platformer.control.ToggleHealthBarsCommand;
import ru.mipt.bit.platformer.model.BulletSettings;
import ru.mipt.bit.platformer.model.Direction;
import ru.mipt.bit.platformer.model.Field;
import ru.mipt.bit.platformer.model.Tank;
import ru.mipt.bit.platformer.model.TankWorld;

import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.*;

public class HealthBarRendererTest {

    private final HealthBarVisibility visibility = new HealthBarVisibility();
    private final List<Rectangle> draws = new ArrayList<>();
    private final List<String> events = new ArrayList<>();
    private float packedColor = Color.BLUE.toFloatBits();
    private final Batch batch = (Batch) Proxy.newProxyInstance(Batch.class.getClassLoader(),
            new Class<?>[]{Batch.class}, (proxy, method, arguments) -> {
                switch (method.getName()) {
                    case "getPackedColor":
                        return packedColor;
                    case "setPackedColor":
                        packedColor = (float) arguments[0];
                        break;
                    case "setColor":
                        packedColor = Color.toFloatBits((float) arguments[0], (float) arguments[1],
                                (float) arguments[2], (float) arguments[3]);
                        break;
                    case "draw":
                        events.add("bar");
                        draws.add(new Rectangle((float) arguments[1], (float) arguments[2],
                                (float) arguments[3], (float) arguments[4]));
                        break;
                    default:
                        throw new AssertionError("Unexpected batch call: " + method.getName());
                }
                return null;
            });

    @Test
    public void alwaysRendersWrappedObjectAndDrawsBarsOnlyWhenEnabled() {
        Tank tank = new Tank(new GridPoint2(0, 0), 1f, point -> true);
        HealthBarRenderer renderer = renderer(tank);

        renderer.render(batch);
        assertEquals(Collections.singletonList("tank"), events);
        assertTrue(draws.isEmpty());
        visibility.toggle();
        renderer.render(batch);
        assertEquals(java.util.Arrays.asList("tank", "tank", "bar", "bar"), events);
        assertEquals(draws.get(0).width - 2f, draws.get(1).width, 0f);
        assertEquals(Color.BLUE.toFloatBits(), packedColor, 0f);
        visibility.toggle();
        renderer.render(batch);
        assertEquals(2, draws.size());
    }

    @Test
    public void barTracksMovementAndReflectsDamageRelativeToInitialHealth() {
        TankWorld world = new TankWorld(new Field(4, 1, Collections.emptyList()),
                80, BulletSettings.DEFAULT, Tank.DEFAULT_SHOT_INTERVAL);
        Tank tank = world.addTank(new GridPoint2(0, 0), 1f);
        HealthBarRenderer renderer = renderer(tank);
        visibility.toggle();
        renderer.render(batch);
        float fullWidth = draws.get(1).width;
        float startX = draws.get(0).x;

        tank.tryMove(Direction.RIGHT);
        tank.update(0.5f);
        tank.takeDamage(20);
        renderer.render(batch);

        assertEquals(startX + 64f, draws.get(2).x, 0.001f);
        assertEquals(draws.get(0).y, draws.get(2).y, 0f);
        assertEquals(fullWidth * 0.75f, draws.get(3).width, 0.001f);
        assertEquals(80, tank.getMaxHealth());
        assertEquals(60, tank.getHealth());
    }

    @Test
    public void zeroInitialHealthAndDestroyedTankDoNotProduceInvalidWidths() {
        TankWorld world = new TankWorld(new Field(1, 1, Collections.emptyList()),
                0, BulletSettings.DEFAULT, Tank.DEFAULT_SHOT_INTERVAL);
        Tank tank = world.addTank(new GridPoint2(0, 0), 1f);
        HealthBarRenderer renderer = renderer(tank);
        visibility.toggle();

        renderer.render(batch);
        tank.takeDamage(25);
        renderer.render(batch);

        assertEquals(0f, draws.get(1).width, 0f);
        assertEquals(0f, draws.get(3).width, 0f);
    }

    @Test
    public void sharedToggleCoversEveryTankAndLaterAdditionsAndDisposesRemovedDecorators() {
        TankWorld world = new TankWorld(new Field(3, 1, Collections.emptyList()));
        Tank first = world.addTank(new GridPoint2(0, 0), 1f);
        world.addTank(new GridPoint2(1, 0), 1f);
        WorldSceneRenderer scene = new WorldSceneRenderer(world, object -> renderer((Tank) object));
        ToggleHealthBarsCommand command = new ToggleHealthBarsCommand(visibility);

        command.execute();
        scene.render(batch);
        assertEquals(4, draws.size());
        world.addTank(new GridPoint2(2, 0), 1f);
        scene.render(batch);
        assertEquals(10, draws.size());
        world.removeObject(first);
        assertEquals(1, Collections.frequency(events, "dispose tank"));
        assertEquals(1, Collections.frequency(events, "dispose pixel"));
        command.execute();
        scene.render(batch);
        assertEquals(10, draws.size());
        scene.dispose();
        scene.dispose();
        assertEquals(3, Collections.frequency(events, "dispose tank"));
        assertEquals(3, Collections.frequency(events, "dispose pixel"));
    }

    private HealthBarRenderer renderer(Tank tank) {
        SceneRenderer decorated = new SceneRenderer() {
            @Override
            public void render(Batch batch) {
                events.add("tank");
            }

            @Override
            public void dispose() {
                events.add("dispose tank");
            }
        };
        TilePositioner positioner = (bounds, from, to, progress) -> bounds.setCenter(
                64f + 128f * (from.x + (to.x - from.x) * progress),
                64f + 128f * (from.y + (to.y - from.y) * progress));
        return new HealthBarRenderer(decorated, tank, positioner, visibility::isVisible,
                new TextureRegion(), () -> events.add("dispose pixel"));
    }
}
