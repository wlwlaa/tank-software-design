package ru.mipt.bit.platformer.model;

import com.badlogic.gdx.math.GridPoint2;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.*;

public class WorldListenerTest {

    @Test
    public void subscriptionReplaysInitialObjectsAndReceivesLaterAdditionsAndRemovalsOnce() {
        Tree tree = new Tree(new GridPoint2(4, 0));
        TankWorld world = new TankWorld(new Field(5, 1, Collections.singletonList(tree)));
        Tank tank = world.addTank(new GridPoint2(0, 0), 0.4f);
        RecordingListener listener = new RecordingListener();

        world.addListener(listener);
        world.addListener(listener);
        assertEquals(Arrays.asList(tree, tank), listener.added);
        tank.shoot();
        Bullet bullet = world.getBullets().get(0);
        assertEquals(Arrays.asList(tree, tank, bullet), listener.added);

        world.update(1f);
        world.removeObject(bullet);
        assertEquals(Collections.singletonList(bullet), listener.removed);
        world.removeListener(listener);
        world.removeObject(tank);
        assertEquals(Collections.singletonList(bullet), listener.removed);
    }

    @Test
    public void subscriptionBeforeInitialTanksUsesTheSameEvents() {
        TankWorld world = new TankWorld(new Field(3, 1, Collections.emptyList()));
        RecordingListener listener = new RecordingListener();
        world.addListener(listener);

        Tank tank = world.addTank(new GridPoint2(0, 0), 0.4f);

        assertEquals(Collections.singletonList(tank), listener.added);
    }

    @Test
    public void immediateHitPublishesBulletAdditionRemovalAndTankDestruction() {
        TankWorld world = new TankWorld(new Field(3, 1, Collections.emptyList()),
                0, BulletSettings.DEFAULT, 0.4f);
        Tank shooter = world.addTank(new GridPoint2(0, 0), 0.4f);
        Tank target = world.addTank(new GridPoint2(1, 0), 0.4f);
        RecordingListener listener = new RecordingListener();
        world.addListener(listener);

        shooter.shoot();

        LevelObject bullet = listener.added.get(2);
        assertTrue(bullet instanceof Bullet);
        assertEquals(Arrays.asList(bullet, target), listener.removed);
        assertEquals(Collections.singletonList(shooter), world.getObjects());
    }

    @Test
    public void removingTreeAlsoRemovesItsCollisionAndNotifiesEveryObserver() {
        Tree tree = new Tree(new GridPoint2(1, 0));
        TankWorld world = new TankWorld(new Field(3, 1, Collections.singletonList(tree)));
        RecordingListener first = new RecordingListener();
        RecordingListener second = new RecordingListener();
        world.addListener(first);
        world.addListener(second);
        assertFalse(world.canMoveTo(tree.getCoordinates()));

        world.removeObject(tree);

        assertTrue(world.canMoveTo(tree.getCoordinates()));
        assertEquals(Collections.singletonList(tree), first.removed);
        assertEquals(first.removed, second.removed);
    }

    private static class RecordingListener implements WorldListener {
        private final List<LevelObject> added = new ArrayList<>();
        private final List<LevelObject> removed = new ArrayList<>();

        @Override
        public void objectAdded(LevelObject object) {
            added.add(object);
        }

        @Override
        public void objectRemoved(LevelObject object) {
            removed.add(object);
        }
    }
}
