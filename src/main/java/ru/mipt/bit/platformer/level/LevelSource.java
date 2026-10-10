package ru.mipt.bit.platformer.level;

import java.io.IOException;

public interface LevelSource {

    LevelDefinition load() throws IOException;
}
