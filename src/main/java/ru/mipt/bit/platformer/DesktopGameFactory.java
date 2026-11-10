package ru.mipt.bit.platformer;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.math.Interpolation;
import ru.mipt.bit.platformer.control.ToggleHealthBarsCommand;
import ru.mipt.bit.platformer.game.GameSession;
import ru.mipt.bit.platformer.game.TankSimulation;
import ru.mipt.bit.platformer.input.DirectionKeyBinding;
import ru.mipt.bit.platformer.input.InputHandler;
import ru.mipt.bit.platformer.input.KeyPressCommandInputAction;
import ru.mipt.bit.platformer.input.KeyState;
import ru.mipt.bit.platformer.input.keyboard.GdxKeyState;
import ru.mipt.bit.platformer.level.LevelDefinition;
import ru.mipt.bit.platformer.level.LevelSource;
import ru.mipt.bit.platformer.model.Bullet;
import ru.mipt.bit.platformer.model.Direction;
import ru.mipt.bit.platformer.model.Tank;
import ru.mipt.bit.platformer.model.Tree;
import ru.mipt.bit.platformer.render.BulletRenderer;
import ru.mipt.bit.platformer.render.FieldRenderer;
import ru.mipt.bit.platformer.render.GdxGameView;
import ru.mipt.bit.platformer.render.GroundMapFactory;
import ru.mipt.bit.platformer.render.HealthBarRenderer;
import ru.mipt.bit.platformer.render.HealthBarVisibility;
import ru.mipt.bit.platformer.render.TankRenderer;
import ru.mipt.bit.platformer.render.TilePositioner;
import ru.mipt.bit.platformer.render.TreeRenderer;
import ru.mipt.bit.platformer.render.WorldSceneRenderer;
import ru.mipt.bit.platformer.util.TileMovement;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Objects;
import java.util.Random;
import java.util.function.Supplier;

import static ru.mipt.bit.platformer.util.GdxGameUtils.createSingleLayerMapRenderer;
import static ru.mipt.bit.platformer.util.GdxGameUtils.getSingleLayer;

public class DesktopGameFactory implements Supplier<GameSession> {

    private static final int AI_TANK_COUNT = 3;
    private static final float MOVEMENT_DURATION = 0.4f;

    private final LevelSource levelSource;

    public DesktopGameFactory(LevelSource levelSource) {
        this.levelSource = Objects.requireNonNull(levelSource);
    }

    @Override
    public GameSession get() {
        LevelDefinition definition;
        try {
            definition = levelSource.load();
        } catch (IOException exception) {
            throw new UncheckedIOException("Could not read level", exception);
        }
        KeyState keyState = new GdxKeyState(Gdx.input);
        HealthBarVisibility healthBars = new HealthBarVisibility();
        TankSimulation simulation = new TankSimulation(
                definition, AI_TANK_COUNT, MOVEMENT_DURATION,
                keyState,
                Arrays.asList(
                        new DirectionKeyBinding(Direction.UP, Keys.UP, Keys.W),
                        new DirectionKeyBinding(Direction.LEFT, Keys.LEFT, Keys.A),
                        new DirectionKeyBinding(Direction.DOWN, Keys.DOWN, Keys.S),
                        new DirectionKeyBinding(Direction.RIGHT, Keys.RIGHT, Keys.D)
                ),
                Keys.SPACE, new Random()
        );
        Batch batch = new SpriteBatch();
        TiledMap level = new GroundMapFactory().create(definition.getWidth(), definition.getHeight());
        TiledMapTileLayer groundLayer = getSingleLayer(level);
        TilePositioner tilePositioner = new TileMovement(groundLayer, Interpolation.smooth);
        WorldSceneRenderer scene = new WorldSceneRenderer(simulation.getWorld(), object -> {
            if (object instanceof Tank) {
                return new HealthBarRenderer(
                        new TankRenderer("images/tank_blue.png", (Tank) object, tilePositioner),
                        (Tank) object, tilePositioner, healthBars::isVisible);
            }
            if (object instanceof Tree) {
                return new TreeRenderer("images/greenTree.png", (Tree) object, tilePositioner);
            }
            if (object instanceof Bullet) {
                return new BulletRenderer((Bullet) object, tilePositioner);
            }
            throw new IllegalArgumentException("No renderer configured for " + object.getClass().getName());
        });
        GdxGameView view = new GdxGameView(
                batch,
                new FieldRenderer(level, createSingleLayerMapRenderer(level, batch)),
                Collections.singletonList(scene)
        );
        InputHandler input = new InputHandler(Arrays.asList(
                new KeyPressCommandInputAction(keyState, Keys.L, new ToggleHealthBarsCommand(healthBars)),
                simulation
        ));
        return new GameSession(input, simulation, view);
    }
}
