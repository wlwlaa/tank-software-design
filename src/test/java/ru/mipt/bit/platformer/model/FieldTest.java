package ru.mipt.bit.platformer.model;

import com.badlogic.gdx.math.GridPoint2;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class FieldTest {

    @Test
    public void permitsMovementToFreeCoordinatesInsideField() {
        Field field = fieldWithTreeAt(1, 1);

        assertTrue(field.canMoveTo(new GridPoint2(0, 0)));
        assertTrue(field.canMoveTo(new GridPoint2(3, 2)));
    }

    @Test
    public void rejectsCoordinatesOutsideField() {
        Field field = fieldWithTreeAt(1, 1);

        assertFalse(field.canMoveTo(new GridPoint2(-1, 0)));
        assertFalse(field.canMoveTo(new GridPoint2(0, -1)));
        assertFalse(field.canMoveTo(new GridPoint2(4, 0)));
        assertFalse(field.canMoveTo(new GridPoint2(0, 3)));
    }

    @Test
    public void rejectsCoordinatesOccupiedByTree() {
        assertFalse(fieldWithTreeAt(1, 1).canMoveTo(new GridPoint2(1, 1)));
    }

    @Test
    public void copiesAndProtectsTreeCollection() {
        List<Tree> source = new ArrayList<>();
        source.add(new Tree(new GridPoint2(1, 1)));
        Field field = new Field(4, 3, source);

        source.clear();

        assertEquals(1, field.getTrees().size());
    }

    @Test(expected = UnsupportedOperationException.class)
    public void returnedTreeCollectionCannotBeChanged() {
        fieldWithTreeAt(1, 1).getTrees().clear();
    }

    private Field fieldWithTreeAt(int x, int y) {
        return new Field(4, 3, Collections.singletonList(new Tree(new GridPoint2(x, y))));
    }
}
