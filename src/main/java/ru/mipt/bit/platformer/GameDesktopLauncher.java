package ru.mipt.bit.platformer;

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import ru.mipt.bit.platformer.game.TankGame;
import ru.mipt.bit.platformer.level.LevelDefinition;

import java.io.IOException;

public final class GameDesktopLauncher {

    private static final int WINDOW_WIDTH = 1280;
    private static final int WINDOW_HEIGHT = 1024;

    private GameDesktopLauncher() {
    }

    public static void main(String[] args) throws IOException {
        LevelDefinition level = LevelOptions.parse(args).load();
        Lwjgl3ApplicationConfiguration configuration = new Lwjgl3ApplicationConfiguration();
        configuration.setWindowedMode(WINDOW_WIDTH, WINDOW_HEIGHT);
        new Lwjgl3Application(new TankGame(new DesktopGameFactory(() -> level)), configuration);
    }
}
