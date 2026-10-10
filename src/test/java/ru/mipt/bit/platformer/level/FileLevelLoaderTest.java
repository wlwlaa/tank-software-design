package ru.mipt.bit.platformer.level;

import com.badlogic.gdx.math.GridPoint2;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import ru.mipt.bit.platformer.model.Field;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

public class FileLevelLoaderTest {

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    @Test
    public void loadsExampleAndMapsTopRowToLargestY() throws IOException {
        String[] rows = {"___T__T___", "__TT__TTTT", "_________T",
                "TTTT__T__T", "_____X____", "__________"};
        LevelDefinition level = parse(String.join("\n", rows));

        assertEquals(10, level.getWidth());
        assertEquals(6, level.getHeight());
        assertEquals(15, level.getObstacles().size());
        assertEquals(new GridPoint2(5, 1), level.getPlayerStart());
        Field field = new Field(level.getWidth(), level.getHeight(), level.getObstacles());
        for (int row = 0; row < rows.length; row++) {
            for (int column = 0; column < rows[row].length(); column++) {
                assertEquals(rows[row].charAt(column) != 'T',
                        field.canMoveTo(new GridPoint2(column, rows.length - row - 1)));
            }
        }
    }

    @Test
    public void acceptsLfAndCrLfWithOrWithoutFinalNewline() throws IOException {
        String[] contents = {"_T\nX_", "_T\nX_\n", "_T\r\nX_", "_T\r\nX_\r\n"};
        for (String content : contents) {
            LevelDefinition level = parse(content);

            assertEquals(2, level.getWidth());
            assertEquals(2, level.getHeight());
            assertEquals(new GridPoint2(0, 0), level.getPlayerStart());
            assertEquals(1, level.getObstacles().size());
            assertFalse(new Field(2, 2, level.getObstacles()).canMoveTo(new GridPoint2(1, 1)));
        }
    }

    @Test
    public void supportsSingleCellLevel() throws IOException {
        LevelDefinition level = parse("X\n");

        assertEquals(1, level.getWidth());
        assertEquals(1, level.getHeight());
        assertTrue(level.getObstacles().isEmpty());
        assertEquals(new GridPoint2(0, 0), level.getPlayerStart());
    }

    @Test
    public void readsLevelFromFile() throws IOException {
        Path path = temporaryFolder.newFile("level.txt").toPath();
        Files.write(path, "T_\n_X\n".getBytes(StandardCharsets.UTF_8));

        LevelDefinition level = new FileLevelLoader(path).load();

        assertEquals(new GridPoint2(1, 0), level.getPlayerStart());
        assertEquals(1, level.getObstacles().size());
        assertFalse(new Field(2, 2, level.getObstacles()).canMoveTo(new GridPoint2(0, 1)));
    }

    @Test
    public void rejectsEmptyFile() {
        IllegalArgumentException error = assertThrows(IllegalArgumentException.class, () -> parse(""));

        assertTrue(error.getMessage().contains("empty"));
    }

    @Test
    public void rejectsEmptyRowsIncludingExtraBlankLineAtEnd() {
        assertInvalidAt("\nX", 1, 1);
        assertInvalidAt("X_\n\n__", 2, 1);
        assertInvalidAt("X\n\n", 2, 1);
    }

    @Test
    public void rejectsShortAndLongRowsWithErrorPosition() {
        assertInvalidAt("X__\n__", 2, 3);
        assertInvalidAt("X_\n___", 2, 3);
    }

    @Test
    public void rejectsUnknownSymbolsAndWhitespaceWithErrorPosition() {
        assertInvalidAt("X_\n_?", 2, 2);
        assertInvalidAt("X ", 1, 2);
        assertInvalidAt("X\t", 1, 2);
    }

    @Test
    public void decodesUtf8BeforeValidatingSymbols() throws IOException {
        Path path = temporaryFolder.newFile("invalid.txt").toPath();
        Files.write(path, "_ЖX".getBytes(StandardCharsets.UTF_8));

        IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
                () -> new FileLevelLoader(path).load());

        assertTrue(error.getMessage().contains("row 1, column 2"));
        assertTrue(error.getMessage().contains("Ж"));
    }

    @Test
    public void rejectsMissingPlayerStart() {
        IllegalArgumentException error = assertThrows(IllegalArgumentException.class, () -> parse("T_\n__"));

        assertTrue(error.getMessage().contains("exactly one X"));
    }

    @Test
    public void rejectsDuplicatePlayerStartAtSecondPosition() {
        assertInvalidAt("X_\n_X", 2, 2);
    }

    @Test
    public void reportsIoFailureForMissingFile() {
        Path missing = temporaryFolder.getRoot().toPath().resolve("missing.txt");

        assertThrows(IOException.class, () -> new FileLevelLoader(missing).load());
    }

    @Test
    public void preservesReaderIoFailure() {
        Reader reader = new Reader() {
            @Override
            public int read(char[] buffer, int offset, int length) throws IOException {
                throw new IOException("Read failed");
            }

            @Override
            public void close() {
            }
        };

        IOException error = assertThrows(IOException.class, () -> FileLevelLoader.parse(reader));

        assertEquals("Read failed", error.getMessage());
    }

    @Test
    public void rejectsNullInputs() {
        assertThrows(NullPointerException.class, () -> new FileLevelLoader(null));
        assertThrows(NullPointerException.class, () -> FileLevelLoader.parse(null));
    }

    private LevelDefinition parse(String content) throws IOException {
        return FileLevelLoader.parse(new StringReader(content));
    }

    private void assertInvalidAt(String content, int row, int column) {
        IllegalArgumentException error = assertThrows(IllegalArgumentException.class, () -> parse(content));

        assertTrue(error.getMessage(), error.getMessage().contains("row " + row + ", column " + column));
    }
}
