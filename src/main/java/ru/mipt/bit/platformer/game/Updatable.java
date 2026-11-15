package ru.mipt.bit.platformer.game;

/**
 * Domain: shared time-update contract for simulation objects, not an infrastructure port.
 * Architecture gap: its game package places a domain contract alongside application services;
 * moving it to a domain/shared package would remove the model-to-game dependency.
 */
public interface Updatable {

    void update(float deltaTime);
}
