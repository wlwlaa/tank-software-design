package ru.mipt.bit.platformer;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.math.GridPoint2;
import com.badlogic.gdx.math.Interpolation;
import ru.mipt.bit.platformer.game.GameSession;
import ru.mipt.bit.platformer.input.DirectionKeyBinding;
import ru.mipt.bit.platformer.input.InputHandler;
import ru.mipt.bit.platformer.input.MovementInputAction;
import ru.mipt.bit.platformer.input.keyboard.GdxKeyState;
import ru.mipt.bit.platformer.model.Direction;
import ru.mipt.bit.platformer.model.Field;
import ru.mipt.bit.platformer.model.Tank;
import ru.mipt.bit.platformer.model.Tree;
import ru.mipt.bit.platformer.render.FieldRenderer;
import ru.mipt.bit.platformer.render.GdxGameView;
import ru.mipt.bit.platformer.render.TankRenderer;
import ru.mipt.bit.platformer.render.TilePositioner;
import ru.mipt.bit.platformer.render.TreeRenderer;
import ru.mipt.bit.platformer.util.TileMovement;

import java.util.Arrays;
import java.util.Collections;
import java.util.function.Supplier;

import static ru.mipt.bit.platformer.util.GdxGameUtils.createSingleLayerMapRenderer;
import static ru.mipt.bit.platformer.util.GdxGameUtils.getSingleLayer;

public class DesktopGameFactory implements Supplier<GameSession> {

    @Override
    public GameSession get() {
        Batch batch = new SpriteBatch();
        TiledMap level = new TmxMapLoader().load("level.tmx");
        TiledMapTileLayer groundLayer = getSingleLayer(level);
        TilePositioner tilePositioner = new TileMovement(groundLayer, Interpolation.smooth);
        Tree tree = new Tree(new GridPoint2(1, 3));
        Field field = new Field(groundLayer.getWidth(), groundLayer.getHeight(), Collections.singletonList(tree));
        Tank tank = new Tank(new GridPoint2(1, 1), 0.4f, field);
        MovementInputAction movementInput = new MovementInputAction(
                tank,
                new GdxKeyState(Gdx.input),
                Arrays.asList(
                        new DirectionKeyBinding(Direction.UP, Keys.UP, Keys.W),
                        new DirectionKeyBinding(Direction.LEFT, Keys.LEFT, Keys.A),
                        new DirectionKeyBinding(Direction.DOWN, Keys.DOWN, Keys.S),
                        new DirectionKeyBinding(Direction.RIGHT, Keys.RIGHT, Keys.D)
                )
        );
        GdxGameView view = new GdxGameView(
                batch,
                new FieldRenderer(level, createSingleLayerMapRenderer(level, batch)),
                Arrays.asList(
                        new TankRenderer("images/tank_blue.png", tank, tilePositioner),
                        new TreeRenderer("images/greenTree.png", tree, tilePositioner)
                )
        );
        return new GameSession(new InputHandler(Collections.singletonList(movementInput)), tank, view);
    }
}
