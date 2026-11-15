package ru.mipt.bit.platformer.model;

/**
 * Domain output port publishing object-lifecycle events without referencing presentation.
 * The infrastructure adapter WorldSceneRenderer consumes these events.
 */
public interface WorldListener {

    void objectAdded(LevelObject object);

    void objectRemoved(LevelObject object);
}
