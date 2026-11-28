package screen;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.event.InputMethodEvent;
import java.awt.font.TextHitInfo;
import java.text.AttributedString;

import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.text.AbstractDocument;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.PlainDocument;

import org.junit.jupiter.api.Test;

/**
 * Checks {@link NameInputFilter}: the allowed-character whitelist, the
 * 10-character cap, and that the cap is computed from the net length
 * change of an edit (current length - replaced range length + inserted
 * length), not from the inserted text's length alone. That distinction
 * matters for an in-progress Hangul composition, which Swing delivers
 * through {@code replace()}, not {@code insertString()}.
 *
 * Disallowed characters are removed right after an edit, on the Swing
 * event thread, so the tests wait for that thread with {@link #settle()}
 * before reading the result. No display is needed.
 */
class NameInputFilterTest {

    private static PlainDocument newFilteredDocument() {
        PlainDocument document = new PlainDocument();
        document.setDocumentFilter(new NameInputFilter());
        return document;
    }

    /** Waits until the pending clean-up on the Swing event thread ran. */
    private static void settle() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
        });
    }

    private static String textOf(final Document document)
            throws BadLocationException {
        return document.getText(0, document.getLength());
    }

    @Test
    void allowedCharactersPassThrough() throws Exception {
        PlainDocument document = newFilteredDocument();
        document.insertString(0, "Ab3한", null);
        settle();

        assertEquals("Ab3한", textOf(document));
    }

    /** Standalone jamo (e.g. ㅋㅋ) must be allowed as a name on their own. */
    @Test
    void standaloneJamoIsAllowed() throws Exception {
        PlainDocument document = newFilteredDocument();
        document.insertString(0, "ㅋㅋ", null);
        settle();

        assertEquals("ㅋㅋ", textOf(document));
    }

    /** Punctuation is removed, the allowed characters of the text stay. */
    @Test
    void disallowedCharactersAreRemoved() throws Exception {
        PlainDocument document = newFilteredDocument();
        document.insertString(0, "A!b@3#", null);
        settle();

        assertEquals("Ab3", textOf(document));
    }

    /** Space must never stay in a name: it separates the save file's fields. */
    @Test
    void spaceIsRemoved() throws Exception {
        PlainDocument document = newFilteredDocument();
        document.insertString(0, "A B C", null);
        settle();

        assertEquals("ABC", textOf(document));
    }

    @Test
    void insertStopsAtTenCharacters() throws Exception {
        PlainDocument document = newFilteredDocument();
        document.insertString(0, "01234567890123", null);
        settle();

        assertEquals("0123456789", textOf(document));
    }

    @Test
    void replaceStopsAtTenCharacters() throws Exception {
        PlainDocument document = newFilteredDocument();
        document.replace(0, 0, "01234567890123", null);
        settle();

        assertEquals(NameInputFilter.MAX_LENGTH, document.getLength());
    }

    /**
     * At the cap, an in-progress Hangul syllable keeps being re-composed
     * by replace() calls that swap the last character for another single
     * character (ㅎ, 하, 한 in the 10th slot). Each edit nets to zero
     * length change and must be allowed.
     */
    @Test
    void composingReplaceAtTheCapIsNotBlocked() throws Exception {
        PlainDocument document = newFilteredDocument();
        document.insertString(0, "123456789", null);
        document.replace(9, 0, "ㅎ", null);
        assertEquals(NameInputFilter.MAX_LENGTH, document.getLength());

        document.replace(9, 1, "하", null);
        assertEquals("123456789하", textOf(document));

        document.replace(9, 1, "한", null);
        assertEquals("123456789한", textOf(document));
    }

    /** A replace at the cap that would grow the document is shortened. */
    @Test
    void growingReplaceAtTheCapIsTruncatedNotBlocked() throws Exception {
        PlainDocument document = newFilteredDocument();
        document.insertString(0, "0123456789", null);
        document.replace(9, 1, "XYZ", null);
        settle();

        assertEquals("012345678X", textOf(document));
    }

    /** Backspace/Delete must always work: remove() is not filtered. */
    @Test
    void removeIsNeverFiltered() throws Exception {
        PlainDocument document = newFilteredDocument();
        document.insertString(0, "0123456789", null);
        document.remove(5, 5);

        assertEquals("01234", textOf(document));
    }

    /**
     * An input method commits the composing syllable together with the
     * Space that ended it ("글 "). The edit itself must keep its full
     * length, as the input method expects, and the space is removed only
     * afterwards. Dropping it inside the edit made macOS delete the
     * character before the committed one.
     */
    @Test
    void commitWithSpaceKeepsItsLengthThenDropsTheSpace() throws Exception {
        PlainDocument document = newFilteredDocument();
        SwingUtilities.invokeAndWait(() -> {
            try {
                document.insertString(0, "한", null);
                document.replace(1, 0, "글", null);
                document.replace(1, 1, "글 ", null);
                assertEquals("한글 ", textOf(document));
            } catch (BadLocationException e) {
                throw new AssertionError(e);
            }
        });
        settle();

        assertEquals("한글", textOf(document));
    }

    /** A space committed at the cap does not push a character out. */
    @Test
    void commitWithSpaceAtTheCapKeepsAllCharacters() throws Exception {
        PlainDocument document = newFilteredDocument();
        document.insertString(0, "123456789", null);
        document.replace(9, 0, "한", null);
        document.replace(9, 1, "한 ", null);
        settle();

        assertEquals("123456789한", textOf(document));
    }

    /** Sends an input method event the way a Hangul input method does. */
    private static void inputMethodText(final JTextField field,
            final String text, final int committed) {
        AttributedString attributed = new AttributedString(text);
        field.dispatchEvent(new InputMethodEvent(field,
                InputMethodEvent.INPUT_METHOD_TEXT_CHANGED,
                attributed.getIterator(), committed,
                TextHitInfo.leading(text.length() - committed), null));
    }

    private static JTextField newFilteredField() {
        JTextField field = new JTextField();
        ((AbstractDocument) field.getDocument())
                .setDocumentFilter(new NameInputFilter());
        return field;
    }

    /**
     * Type 한글 with an input method, then press Space: 한글 stays. These
     * events go through Swing's own input method handling; the macOS
     * input method bug itself cannot be reproduced without a display, so
     * this only checks that the clean-up works on a real text field.
     */
    @Test
    void spaceAfterComposedSyllablesKeepsThem() throws Exception {
        JTextField[] field = new JTextField[1];
        SwingUtilities.invokeAndWait(() -> {
            field[0] = newFilteredField();
            inputMethodText(field[0], "한", 0);
            inputMethodText(field[0], "한", 1);
            inputMethodText(field[0], "글", 0);
            inputMethodText(field[0], "글 ", 2);
        });
        settle();

        assertEquals("한글", field[0].getText());
    }

    /** Type ㄴㅇ with an input method, then press Space: ㄴㅇ stays. */
    @Test
    void spaceAfterComposedJamoKeepsThem() throws Exception {
        JTextField[] field = new JTextField[1];
        SwingUtilities.invokeAndWait(() -> {
            field[0] = newFilteredField();
            inputMethodText(field[0], "ㄴ", 0);
            inputMethodText(field[0], "ㄴ", 1);
            inputMethodText(field[0], "ㅇ", 0);
            inputMethodText(field[0], "ㅇ ", 2);
        });
        settle();

        assertEquals("ㄴㅇ", field[0].getText());
    }

    @Test
    void keepAllowedCleansUpAName() {
        assertEquals("한글ab1", NameInputFilter.keepAllowed("한 글a!b 1"));
        assertEquals("", NameInputFilter.keepAllowed(null));
    }

    @Test
    void isAllowedAcceptsOnlyHangulLettersAndDigits() {
        assertTrue(NameInputFilter.isAllowed('가'));
        assertTrue(NameInputFilter.isAllowed('ㅎ'));
        assertTrue(NameInputFilter.isAllowed('z'));
        assertTrue(NameInputFilter.isAllowed('7'));
        assertFalse(NameInputFilter.isAllowed(' '));
        assertFalse(NameInputFilter.isAllowed('!'));
        assertFalse(NameInputFilter.isAllowed('あ'));
    }
}
