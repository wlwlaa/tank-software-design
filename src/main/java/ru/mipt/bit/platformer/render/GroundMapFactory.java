package ru.mipt.bit.platformer.render;

import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;

import static ru.mipt.bit.platformer.util.GdxGameUtils.getSingleLayer;

public class GroundMapFactory {

    public TiledMap create(int width, int height) {
        TiledMap map = new TmxMapLoader().load("level.tmx");
        try {
            resize(map, width, height);
            return map;
        } catch (RuntimeException exception) {
            map.dispose();
            throw exception;
        }
    }

    static void resize(TiledMap map, int width, int height) {
        if (width <= 0 || height <= 0) {
            throw new IllegalArgumentException("Ground dimensions must be positive");
        }
        TiledMapTileLayer template = getSingleLayer(map);
        TiledMapTileLayer ground = new TiledMapTileLayer(width, height,
                template.getTileWidth(), template.getTileHeight());
        ground.setName(template.getName());
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                ground.setCell(x, y, template.getCell(x % template.getWidth(), y % template.getHeight()));
            }
        }
        map.getLayers().remove(template);
        map.getLayers().add(ground);
        map.getProperties().put("width", width);
        map.getProperties().put("height", height);
    }
}
