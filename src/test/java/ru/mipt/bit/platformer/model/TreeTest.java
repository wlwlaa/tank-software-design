package ru.mipt.bit.platformer.model;

import com.badlogic.gdx.math.GridPoint2;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class TreeTest {

    @Test
    public void occupiesOnlyItsOwnCoordinates() {
        Tree tree = new Tree(new GridPoint2(1, 3));

        assertTrue(tree.occupies(new GridPoint2(1, 3)));
        assertFalse(tree.occupies(new GridPoint2(1, 2)));
    }

    @Test
    public void protectsCoordinatesFromExternalChanges() {
        GridPoint2 source = new GridPoint2(1, 3);
        Tree tree = new Tree(source);
        source.set(5, 5);

        GridPoint2 returnedCoordinates = tree.getCoordinates();
        returnedCoordinates.set(7, 7);

        assertEquals(new GridPoint2(1, 3), tree.getCoordinates());
    }
}
