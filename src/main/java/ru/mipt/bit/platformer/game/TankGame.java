package ru.mipt.bit.platformer.game;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;

import java.util.function.Supplier;

public class TankGame extends ApplicationAdapter {

    private final Supplier<GameSession> sessionFactory;
    private GameSession session;

    public TankGame(Supplier<GameSession> sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    @Override
    public void create() {
        session = sessionFactory.get();
    }

    @Override
    public void render() {
        session.render(Gdx.graphics.getDeltaTime());
    }

    @Override
    public void dispose() {
        session.dispose();
    }
}
