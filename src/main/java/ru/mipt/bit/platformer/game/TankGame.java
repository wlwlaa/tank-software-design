package ru.mipt.bit.platformer.game;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.GridPoint2;
import ru.mipt.bit.platformer.input.InputHandler;
import ru.mipt.bit.platformer.input.MovementInputAction;
import ru.mipt.bit.platformer.model.Field;
import ru.mipt.bit.platformer.model.Tank;
import ru.mipt.bit.platformer.model.Tree;
import ru.mipt.bit.platformer.render.FieldRenderer;
import ru.mipt.bit.platformer.render.TankRenderer;

import java.util.Collections;

import static com.badlogic.gdx.graphics.GL20.GL_COLOR_BUFFER_BIT;

public class TankGame extends ApplicationAdapter {

    private Batch batch;
    private Field field;
    private Tank tank;
    private FieldRenderer fieldRenderer;
    private TankRenderer tankRenderer;
    private InputHandler inputHandler;

    @Override
    public void create() {
        batch = new SpriteBatch();
        fieldRenderer = new FieldRenderer(batch);
        field = new Field(
                fieldRenderer.getWidth(),
                fieldRenderer.getHeight(),
                Collections.singletonList(new Tree(new GridPoint2(1, 3)))
        );
        tank = new Tank(new GridPoint2(1, 1));
        tankRenderer = new TankRenderer("images/tank_blue.png");
        inputHandler = new InputHandler(
                Collections.singletonList(new MovementInputAction(tank, field))
        );
    }

    @Override
    public void render() {
        clearScreen();

        inputHandler.handleInput();
        tank.update(Gdx.graphics.getDeltaTime(), field);

        fieldRenderer.renderMap();
        batch.begin();
        tankRenderer.render(batch, tank, fieldRenderer);
        fieldRenderer.renderObjects(batch, field);
        batch.end();
    }

    private void clearScreen() {
        Gdx.gl.glClearColor(0f, 0f, 0.2f, 1f);
        Gdx.gl.glClear(GL_COLOR_BUFFER_BIT);
    }

    @Override
    public void dispose() {
        tankRenderer.dispose();
        fieldRenderer.dispose();
        batch.dispose();
    }
}
