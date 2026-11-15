package ru.mipt.bit.platformer.level;

import java.io.IOException;

/**
 * Application output port for obtaining a logical level independently of its source.
 * Implemented by FileLevelLoader and RandomLevelGenerator; IOException currently exposes
 * a file-oriented failure contract even to non-file implementations.
 */
public interface LevelSource {

    LevelDefinition load() throws IOException;
}
