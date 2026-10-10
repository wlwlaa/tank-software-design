package ru.mipt.bit.platformer;

import com.badlogic.gdx.math.GridPoint2;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import ru.mipt.bit.platformer.level.FileLevelLoader;
import ru.mipt.bit.platformer.level.LevelDefinition;
import ru.mipt.bit.platformer.level.LevelSource;
import ru.mipt.bit.platformer.level.RandomLevelGenerator;
import ru.mipt.bit.platformer.model.Obstacle;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

public class LevelOptionsTest {

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    @Test
    public void defaultsToRandomTenByEightLevelWithTwentyObstacles() throws IOException {
        LevelSource source = LevelOptions.parse(new String[0]);
        LevelDefinition level = source.load();

        assertTrue(source instanceof RandomLevelGenerator);
        assertEquals(10, level.getWidth());
        assertEquals(8, level.getHeight());
        assertEquals(20, level.getObstacles().size());
    }

    @Test
    public void acceptsExplicitRandomDimensionsAndObstacleCount() throws IOException {
        LevelDefinition level = LevelOptions.parse(new String[]{"--random", "4", "3", "7"}).load();

        assertEquals(4, level.getWidth());
        assertEquals(3, level.getHeight());
        assertEquals(7, level.getObstacles().size());
    }

    @Test
    public void sameSeedRecreatesPlayerStartAndObstacleCoordinates() throws IOException {
        String[] args = {"--random", "10", "8", "20", "9223372036854775807"};
        LevelDefinition first = LevelOptions.parse(args).load();
        LevelDefinition second = LevelOptions.parse(args).load();

        assertEquals(first.getPlayerStart(), second.getPlayerStart());
        for (int y = 0; y < first.getHeight(); y++) {
            for (int x = 0; x < first.getWidth(); x++) {
                GridPoint2 coordinates = new GridPoint2(x, y);
                assertEquals(occupied(first, coordinates), occupied(second, coordinates));
            }
        }
    }

    @Test
    public void fileOptionLoadsSpecifiedPath() throws IOException {
        Path file = temporaryFolder.newFile("level.txt").toPath();
        Files.write(file, "T__\n_X_\n".getBytes(StandardCharsets.UTF_8));
        LevelSource source = LevelOptions.parse(new String[]{"--file", file.toString()});
        LevelDefinition level = source.load();

        assertTrue(source instanceof FileLevelLoader);
        assertEquals(3, level.getWidth());
        assertEquals(2, level.getHeight());
        assertEquals(new GridPoint2(1, 0), level.getPlayerStart());
        assertEquals(1, level.getObstacles().size());
        assertTrue(occupied(level, new GridPoint2(0, 1)));
    }

    @Test
    public void rejectsInvalidOptionsBeforeLoadingLevelWithUsage() {
        String[][] invalidOptions = {
                {"--unknown"},
                {"--random"},
                {"--random", "10", "8"},
                {"--random", "10", "8", "20", "1", "extra"},
                {"--random", "many", "8", "20"},
                {"--random", "10", "8", "20", "seed"},
                {"--random", "0", "8", "0"},
                {"--random", "-1", "8", "0"},
                {"--random", "10", "-8", "0"},
                {"--random", "10", "8", "-1"},
                {"--random", "2", "2", "4"},
                {"--random", "2147483648", "2", "0"},
                {"--random", "2147483647", "2", "0"},
                {"--random", "2", "2", "0", "9223372036854775808"},
                {"--file"},
                {"--file", ""},
                {"--file", "level.txt", "extra"}
        };

        for (String[] args : invalidOptions) {
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                    () -> LevelOptions.parse(args));
            assertTrue(exception.getMessage().contains("Usage:"));
        }
    }

    private boolean occupied(LevelDefinition level, GridPoint2 coordinates) {
        for (Obstacle obstacle : level.getObstacles()) {
            if (obstacle.occupies(coordinates)) {
                return true;
            }
        }
        return false;
    }
}
