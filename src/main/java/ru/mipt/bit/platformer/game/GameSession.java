package ru.mipt.bit.platformer.game;

import ru.mipt.bit.platformer.input.InputAction;

/**
 * Application: frame and shutdown use cases invoked by the driving TankGame adapter.
 * Coordinates control processing, domain updates and the outgoing GameView port.
 */
public class GameSession {

    private final InputAction input;
    private final Updatable world;
    private final GameView view;

    public GameSession(InputAction input, Updatable world, GameView view) {
        this.input = input;
        this.world = world;
        this.view = view;
    }

    public void render(float deltaTime) {
        input.handle();
        world.update(deltaTime);
        view.render();
    }

    public void dispose() {
        view.dispose();
    }
}
