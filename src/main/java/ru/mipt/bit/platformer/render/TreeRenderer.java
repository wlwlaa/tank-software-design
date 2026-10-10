package ru.mipt.bit.platformer.render;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import ru.mipt.bit.platformer.model.Tree;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static ru.mipt.bit.platformer.util.GdxGameUtils.createBoundingRectangle;
import static ru.mipt.bit.platformer.util.GdxGameUtils.drawTextureRegionUnscaled;

public class TreeRenderer implements SceneRenderer {

    private final List<Tree> trees;
    private final TilePositioner tilePositioner;
    private final Texture texture;
    private final TextureRegion graphics;
    private final Rectangle rectangle;

    public TreeRenderer(String texturePath, Tree tree, TilePositioner tilePositioner) {
        this(texturePath, Collections.singletonList(tree), tilePositioner);
    }

    public TreeRenderer(String texturePath, List<Tree> trees, TilePositioner tilePositioner) {
        this.trees = new ArrayList<>(trees);
        this.tilePositioner = tilePositioner;
        texture = new Texture(texturePath);
        graphics = new TextureRegion(texture);
        rectangle = createBoundingRectangle(graphics);
    }

    @Override
    public void render(Batch batch) {
        for (Tree tree : trees) {
            tilePositioner.moveRectangleBetweenTileCenters(rectangle, tree.getCoordinates(), tree.getCoordinates(), 1f);
            drawTextureRegionUnscaled(batch, graphics, rectangle, 0f);
        }
    }

    @Override
    public void dispose() {
        texture.dispose();
    }
}
