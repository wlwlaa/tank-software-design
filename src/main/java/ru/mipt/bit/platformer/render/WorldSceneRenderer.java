package ru.mipt.bit.platformer.render;

import com.badlogic.gdx.graphics.g2d.Batch;
import ru.mipt.bit.platformer.model.LevelObject;
import ru.mipt.bit.platformer.model.TankWorld;
import ru.mipt.bit.platformer.model.WorldListener;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

/** Keeps graphics in sync with the level, including the initial subscription snapshot. */
public final class WorldSceneRenderer implements SceneRenderer, WorldListener {

    private final TankWorld world;
    private final Function<LevelObject, SceneRenderer> rendererFactory;
    private final Map<LevelObject, SceneRenderer> renderers = new LinkedHashMap<>();

    public WorldSceneRenderer(TankWorld world, Function<LevelObject, SceneRenderer> rendererFactory) {
        this.world = Objects.requireNonNull(world);
        this.rendererFactory = Objects.requireNonNull(rendererFactory);
        try {
            world.addListener(this);
        } catch (RuntimeException exception) {
            dispose();
            throw exception;
        }
    }

    @Override
    public void objectAdded(LevelObject object) {
        renderers.computeIfAbsent(object, rendererFactory);
    }

    @Override
    public void objectRemoved(LevelObject object) {
        SceneRenderer renderer = renderers.remove(object);
        if (renderer != null) {
            renderer.dispose();
        }
    }

    @Override
    public void render(Batch batch) {
        for (SceneRenderer renderer : renderers.values()) {
            renderer.render(batch);
        }
    }

    @Override
    public void dispose() {
        world.removeListener(this);
        for (SceneRenderer renderer : renderers.values()) {
            renderer.dispose();
        }
        renderers.clear();
    }
}
