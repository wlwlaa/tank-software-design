package ru.mipt.bit.platformer.level;

import com.badlogic.gdx.math.GridPoint2;
import org.junit.Test;
import ru.mipt.bit.platformer.model.Obstacle;
import ru.mipt.bit.platformer.model.Tree;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;

public class LevelDefinitionTest {

    @Test
    public void storesDimensionsAndProtectsPlayerCoordinates() {
        GridPoint2 source = new GridPoint2(1, 2);
        LevelDefinition level = new LevelDefinition(3, 4, Collections.emptyList(), source);

        source.set(20, 20);
        level.getPlayerStart().set(-1, -1);

        assertEquals(3, level.getWidth());
        assertEquals(4, level.getHeight());
        assertEquals(new GridPoint2(1, 2), level.getPlayerStart());
    }

    @Test
    public void copiesAndProtectsObstacleCollection() {
        List<Tree> source = new ArrayList<>();
        Tree tree = new Tree(new GridPoint2(1, 1));
        source.add(tree);
        LevelDefinition level = new LevelDefinition(3, 4, source, new GridPoint2(0, 0));

        source.clear();

        assertEquals(Collections.singletonList(tree), level.getObstacles());
        assertThrows(UnsupportedOperationException.class, () -> level.getObstacles().clear());
    }

    @Test
    public void rejectsNonPositiveDimensions() {
        int[][] invalidDimensions = {{0, 1}, {1, 0}, {-1, 2}, {2, -1}};
        for (int[] dimensions : invalidDimensions) {
            assertThrows(IllegalArgumentException.class, () -> new LevelDefinition(
                    dimensions[0], dimensions[1], Collections.emptyList(), new GridPoint2(0, 0)));
        }
    }

    @Test
    public void rejectsPlayerOutsideEachBoundary() {
        GridPoint2[] invalidStarts = {new GridPoint2(-1, 0), new GridPoint2(0, -1),
                new GridPoint2(3, 0), new GridPoint2(0, 4)};
        for (GridPoint2 start : invalidStarts) {
            assertThrows(IllegalArgumentException.class,
                    () -> new LevelDefinition(3, 4, Collections.emptyList(), start));
        }
    }

    @Test
    public void rejectsPlayerInTreeCell() {
        Tree tree = new Tree(new GridPoint2(1, 1));

        assertThrows(IllegalArgumentException.class, () -> new LevelDefinition(
                3, 4, Collections.singletonList(tree), new GridPoint2(1, 1)));
    }

    @Test
    public void checksPlayerAgainstArbitraryObstacleTypes() {
        Obstacle wall = coordinates -> coordinates.x == 1;

        assertThrows(IllegalArgumentException.class, () -> new LevelDefinition(
                3, 4, Collections.singletonList(wall), new GridPoint2(1, 3)));
    }

    @Test
    public void rejectsNullInputsAndObstacles() {
        assertThrows(NullPointerException.class,
                () -> new LevelDefinition(3, 4, null, new GridPoint2(0, 0)));
        assertThrows(NullPointerException.class,
                () -> new LevelDefinition(3, 4, Collections.emptyList(), null));
        assertThrows(NullPointerException.class, () -> new LevelDefinition(
                3, 4, Collections.singletonList(null), new GridPoint2(0, 0)));
    }
}
