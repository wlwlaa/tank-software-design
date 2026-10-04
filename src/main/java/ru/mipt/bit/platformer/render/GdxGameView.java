package ru.mipt.bit.platformer.render;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.Batch;
import ru.mipt.bit.platformer.game.GameView;

import java.util.ArrayList;
import java.util.List;

import static com.badlogic.gdx.graphics.GL20.GL_COLOR_BUFFER_BIT;

public class GdxGameView implements GameView {

    private final Batch batch;
    private final FieldRenderer fieldRenderer;
    private final List<SceneRenderer> sceneRenderers;

    public GdxGameView(Batch batch, FieldRenderer fieldRenderer, List<SceneRenderer> sceneRenderers) {
        this.batch = batch;
        this.fieldRenderer = fieldRenderer;
        this.sceneRenderers = new ArrayList<>(sceneRenderers);
    }

    @Override
    public void render() {
        Gdx.gl.glClearColor(0f, 0f, 0.2f, 1f);
        Gdx.gl.glClear(GL_COLOR_BUFFER_BIT);
        fieldRenderer.renderMap();
        batch.begin();
        try {
            for (SceneRenderer renderer : sceneRenderers) {
                renderer.render(batch);
            }
        } finally {
            batch.end();
        }
    }

    @Override
    public void dispose() {
        for (SceneRenderer renderer : sceneRenderers) {
            renderer.dispose();
        }
        fieldRenderer.dispose();
        batch.dispose();
    }
}
