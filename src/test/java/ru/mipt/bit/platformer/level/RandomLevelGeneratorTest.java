package ru.mipt.bit.platformer.level;

import com.badlogic.gdx.math.GridPoint2;
import org.junit.Test;
import ru.mipt.bit.platformer.model.Field;
import ru.mipt.bit.platformer.model.Obstacle;
import ru.mipt.bit.platformer.model.Tree;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

public class RandomLevelGeneratorTest {

    @Test
    public void generatesRequestedTreesAndFreeStartInsideField() {
        for (long seed = 0; seed < 100; seed++) {
            LevelDefinition level = new RandomLevelGenerator(8, 7, 20, new Random(seed)).load();

            assertEquals(8, level.getWidth());
            assertEquals(7, level.getHeight());
            assertEquals(20, level.getObstacles().size());
            assertValidCells(level);
        }
    }

    @Test
    public void canFillEveryCellExceptPlayerStart() {
        for (long seed = 0; seed < 100; seed++) {
            LevelDefinition level = new RandomLevelGenerator(8, 7, 55, new Random(seed)).load();

            assertEquals(55, level.getObstacles().size());
            assertValidCells(level);
        }
    }

    @Test
    public void supportsSingleCellLevel() {
        LevelDefinition level = new RandomLevelGenerator(1, 1, 0, new Random(7)).load();

        assertEquals(new GridPoint2(0, 0), level.getPlayerStart());
        assertTrue(level.getObstacles().isEmpty());
    }

    @Test
    public void sameSeedReproducesPlayerAndTrees() {
        LevelDefinition first = new RandomLevelGenerator(10, 6, 15, new Random(42)).load();
        LevelDefinition second = new RandomLevelGenerator(10, 6, 15, new Random(42)).load();

        assertEquals(first.getPlayerStart(), second.getPlayerStart());
        assertEquals(treeCoordinates(first), treeCoordinates(second));
    }

    @Test
    public void varyingSeedsChangePlayerStartAndTreeLayout() {
        Set<GridPoint2> starts = new HashSet<>();
        Set<Set<GridPoint2>> layouts = new HashSet<>();
        for (long seed = 0; seed < 100; seed++) {
            LevelDefinition level = new RandomLevelGenerator(8, 7, 20, new Random(seed)).load();
            starts.add(level.getPlayerStart());
            layouts.add(treeCoordinates(level));
        }

        assertTrue(starts.size() > 1);
        assertTrue(layouts.size() > 1);
    }

    @Test
    public void emptyLargeFieldDoesNotRequireOneObjectPerCell() {
        LevelDefinition level = new RandomLevelGenerator(Integer.MAX_VALUE, 1, 0, new Random(42)).load();

        assertEquals(Integer.MAX_VALUE, level.getWidth());
        assertTrue(level.getObstacles().isEmpty());
        assertValidCells(level);
    }

    @Test
    public void rejectsNonPositiveDimensions() {
        int[][] invalidDimensions = {{0, 1}, {1, 0}, {-1, 2}, {2, -1}};
        for (int[] dimensions : invalidDimensions) {
            assertThrows(IllegalArgumentException.class,
                    () -> new RandomLevelGenerator(dimensions[0], dimensions[1], 0, new Random()));
        }
    }

    @Test
    public void rejectsNegativeAndExcessiveTreeCounts() {
        int[] invalidCounts = {-1, 6, 7, Integer.MAX_VALUE};
        for (int count : invalidCounts) {
            assertThrows(IllegalArgumentException.class,
                    () -> new RandomLevelGenerator(3, 2, count, new Random()));
        }
        assertThrows(IllegalArgumentException.class,
                () -> new RandomLevelGenerator(1, 1, 1, new Random()));
    }

    @Test
    public void rejectsCellCountOverflow() {
        assertThrows(IllegalArgumentException.class,
                () -> new RandomLevelGenerator(50_000, 50_000, 0, new Random()));
        assertThrows(IllegalArgumentException.class,
                () -> new RandomLevelGenerator(Integer.MAX_VALUE, Integer.MAX_VALUE, 0, new Random()));
    }

    @Test
    public void rejectsNullRandomSource() {
        assertThrows(NullPointerException.class, () -> new RandomLevelGenerator(3, 2, 1, null));
    }

    private void assertValidCells(LevelDefinition level) {
        Field field = new Field(level.getWidth(), level.getHeight(), level.getObstacles());
        assertTrue(field.canMoveTo(level.getPlayerStart()));
        Set<GridPoint2> occupied = new HashSet<>();
        occupied.add(level.getPlayerStart());
        for (Obstacle obstacle : level.getObstacles()) {
            assertTrue(obstacle instanceof Tree);
            GridPoint2 coordinates = ((Tree) obstacle).getCoordinates();
            assertTrue(coordinates.x >= 0 && coordinates.x < level.getWidth());
            assertTrue(coordinates.y >= 0 && coordinates.y < level.getHeight());
            assertTrue("Every occupied cell must be unique", occupied.add(coordinates));
        }
    }

    private Set<GridPoint2> treeCoordinates(LevelDefinition level) {
        Set<GridPoint2> coordinates = new HashSet<>();
        for (Obstacle obstacle : level.getObstacles()) {
            coordinates.add(((Tree) obstacle).getCoordinates());
        }
        return coordinates;
    }
}
