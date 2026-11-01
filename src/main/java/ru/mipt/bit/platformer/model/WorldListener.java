package ru.mipt.bit.platformer.model;

public interface WorldListener {

    void objectAdded(LevelObject object);

    void objectRemoved(LevelObject object);
}
