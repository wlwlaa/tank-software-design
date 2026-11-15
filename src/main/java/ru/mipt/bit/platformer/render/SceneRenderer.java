package ru.mipt.bit.platformer.render;

import com.badlogic.gdx.graphics.g2d.Batch;

/**
 * Infrastructure: internal composition/decorator interface for presentation components.
 * Not a hexagonal application port: its Batch parameter deliberately belongs to LibGDX.
 * GameView is the framework-free core presentation port.
 */
public interface SceneRenderer {

    void render(Batch batch);

    void dispose();
}
