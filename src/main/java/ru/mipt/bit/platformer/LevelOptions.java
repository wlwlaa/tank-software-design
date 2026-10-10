package ru.mipt.bit.platformer;

import ru.mipt.bit.platformer.level.FileLevelLoader;
import ru.mipt.bit.platformer.level.LevelSource;
import ru.mipt.bit.platformer.level.RandomLevelGenerator;

import java.nio.file.InvalidPathException;
import java.nio.file.Paths;
import java.util.Random;

public final class LevelOptions {

    private static final String USAGE = "Usage: [--random WIDTH HEIGHT OBSTACLE_COUNT [SEED] | --file PATH]";

    private LevelOptions() {
    }

    public static LevelSource parse(String[] args) {
        if (args == null) {
            throw invalid("Arguments must not be null.");
        }
        if (args.length == 0) {
            return new RandomLevelGenerator(10, 8, 20, new Random());
        }
        if ("--file".equals(args[0])) {
            if (args.length != 2 || args[1] == null || args[1].isEmpty()) {
                throw invalid("--file requires exactly one non-empty path.");
            }
            try {
                return new FileLevelLoader(Paths.get(args[1]));
            } catch (InvalidPathException exception) {
                throw invalid("Invalid level path: " + exception.getMessage());
            }
        }
        if ("--random".equals(args[0])) {
            if (args.length != 4 && args.length != 5) {
                throw invalid("--random requires width, height and obstacle count, followed by an optional seed.");
            }
            int width = integer(args[1], "width");
            int height = integer(args[2], "height");
            int obstacleCount = integer(args[3], "obstacle count");
            Random random = args.length == 5 ? new Random(seed(args[4])) : new Random();
            try {
                return new RandomLevelGenerator(width, height, obstacleCount, random);
            } catch (IllegalArgumentException exception) {
                throw invalid(exception.getMessage());
            }
        }
        throw invalid("Unknown level option: " + args[0]);
    }

    private static int integer(String value, String name) {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException exception) {
            throw invalid("Invalid " + name + ": " + value + ". Expected a 32-bit integer.");
        }
    }

    private static long seed(String value) {
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException exception) {
            throw invalid("Invalid seed: " + value + ". Expected a 64-bit integer.");
        }
    }

    private static IllegalArgumentException invalid(String message) {
        return new IllegalArgumentException(message + " " + USAGE);
    }
}
