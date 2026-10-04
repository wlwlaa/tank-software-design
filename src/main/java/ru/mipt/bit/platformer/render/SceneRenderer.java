package ru.mipt.bit.platformer.render;

import com.badlogic.gdx.graphics.g2d.Batch;

public interface SceneRenderer {

    void render(Batch batch);

    void dispose();
}
