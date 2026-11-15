package ru.mipt.bit.platformer.game;

/**
 * Application output port for presentation and presentation-resource cleanup.
 * The infrastructure adapter is GdxGameView; the contract exposes no LibGDX types.
 */
public interface GameView {

    void render();

    void dispose();
}
