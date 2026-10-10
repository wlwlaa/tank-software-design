package ru.mipt.bit.platformer.render;

import com.badlogic.gdx.math.Rectangle;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class FieldRendererTest {

    @Test
    public void fitsWideLevelWithoutDistortingTiles() {
        assertFitsLevel(2560, 768, 1280, 1024);
    }

    @Test
    public void fitsTallLevelAndUpdatesForDifferentWindowProportions() {
        assertFitsLevel(512, 2048, 1280, 1024);
        assertFitsLevel(512, 2048, 600, 1200);
    }

    private void assertFitsLevel(float width, float height, int screenWidth, int screenHeight) {
        Rectangle bounds = FieldRenderer.fitView(new Rectangle(), width, height, screenWidth, screenHeight);

        assertTrue(bounds.contains(0, 0));
        assertTrue(bounds.contains(width, height));
        assertEquals((float) screenWidth / screenHeight, bounds.width / bounds.height, 0.0001f);
        assertEquals(width / 2, bounds.x + bounds.width / 2, 0.0001f);
        assertEquals(height / 2, bounds.y + bounds.height / 2, 0.0001f);
    }
}
