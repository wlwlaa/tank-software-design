package ru.mipt.bit.platformer.render;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.GridPoint2;
import org.junit.Test;
import ru.mipt.bit.platformer.model.Bullet;
import ru.mipt.bit.platformer.model.Field;
import ru.mipt.bit.platformer.model.LevelObject;
import ru.mipt.bit.platformer.model.Tank;
import ru.mipt.bit.platformer.model.TankWorld;
import ru.mipt.bit.platformer.model.Tree;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.Assert.*;

public class WorldSceneRendererTest {

    @Test
    public void observesInitialAndDynamicObjectsAndDisposesRemovedGraphicsExactlyOnce() {
        Tree tree = new Tree(new GridPoint2(4, 0));
        TankWorld world = new TankWorld(new Field(5, 1, Collections.singletonList(tree)));
        Tank tank = world.addTank(new GridPoint2(0, 0), 0.4f);
        Map<LevelObject, RecordingRenderer> graphics = new LinkedHashMap<>();
        WorldSceneRenderer scene = new WorldSceneRenderer(world, object -> {
            RecordingRenderer renderer = new RecordingRenderer();
            graphics.put(object, renderer);
            return renderer;
        });

        assertEquals(2, graphics.size());
        tank.shoot();
        Bullet bullet = world.getBullets().get(0);
        assertEquals(3, graphics.size());
        scene.render(null);
        assertEquals(1, graphics.get(bullet).renders);

        world.update(1f);
        scene.render(null);
        assertEquals(1, graphics.get(bullet).renders);
        assertEquals(1, graphics.get(bullet).disposals);
        assertEquals(2, graphics.get(tree).renders);
        world.removeObject(tank);
        scene.render(null);
        assertEquals(2, graphics.get(tank).renders);
        assertEquals(1, graphics.get(tank).disposals);

        scene.dispose();
        scene.dispose();
        world.addTank(new GridPoint2(0, 0), 0.4f);
        assertEquals(3, graphics.size());
        for (RecordingRenderer renderer : graphics.values()) {
            assertEquals(1, renderer.disposals);
        }
    }

    @Test
    public void failedInitialRendererCreationDisposesCreatedGraphicsAndUnsubscribes() {
        TankWorld world = new TankWorld(new Field(3, 1,
                Collections.singletonList(new Tree(new GridPoint2(2, 0)))));
        Tank tank = world.addTank(new GridPoint2(0, 0), 0.4f);
        RecordingRenderer treeRenderer = new RecordingRenderer();
        int[] creations = {0};

        assertThrows(IllegalArgumentException.class, () -> new WorldSceneRenderer(world, object -> {
            creations[0]++;
            if (object instanceof Tank) {
                throw new IllegalArgumentException("Unsupported tank graphics");
            }
            return treeRenderer;
        }));
        assertEquals(1, treeRenderer.disposals);

        tank.shoot();
        assertEquals(2, creations[0]);
    }

    private static class RecordingRenderer implements SceneRenderer {
        private int renders;
        private int disposals;

        @Override
        public void render(Batch batch) {
            renders++;
        }

        @Override
        public void dispose() {
            disposals++;
        }
    }
}
