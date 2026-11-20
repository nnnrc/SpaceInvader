package screen;

import javax.swing.text.PlainDocument;

/**
 * Standalone checks for {@link NameInputFilter}: the allowed-character
 * whitelist, the 10-character cap, and that the cap is computed from the
 * net length change of an edit (current length - replaced range length +
 * inserted length), not from the inserted text's length alone — the
 * distinction that matters for an in-progress Hangul composition, which
 * Swing delivers through {@code replace()}, not {@code insertString()}.
 * No GUI/display is required; a bare {@link PlainDocument} with the
 * filter installed is enough.
 */
public final class NameInputFilterTest {

    private NameInputFilterTest() {
    }

    private static void check(final boolean condition, final String message) {
        if (!condition)
            throw new AssertionError(message);
    }

    public static void main(final String[] args) throws Exception {
        checkAllowedCharactersPassThrough();
        checkStandaloneJamoIsAllowed();
        checkDisallowedCharactersAreDropped();
        checkSpaceIsDropped();
        checkInsertStopsAtTenCharacters();
        checkReplaceStopsAtTenCharacters();
        checkComposingReplaceAtTheCapIsNotBlocked();
        checkGrowingReplaceAtTheCapIsTruncatedNotBlocked();
        checkRemoveIsNeverFiltered();

        System.out.println("NameInputFilter checks passed.");
    }

    private static PlainDocument newFilteredDocument() {
        PlainDocument document = new PlainDocument();
        document.setDocumentFilter(new NameInputFilter());
        return document;
    }

    /** English letters, digits and composed Hangul syllables are kept. */
    private static void checkAllowedCharactersPassThrough() throws Exception {
        PlainDocument document = newFilteredDocument();
        document.insertString(0, "Ab3한", null); // 한
        check("Ab3한".equals(document.getText(0, document.getLength())),
                "Letters, digits and a Hangul syllable are all kept");
    }

    /**
     * Standalone Hangul compatibility jamo (e.g. ㅋㅋ), which never combine
     * into a full syllable, must still be allowed as a name on their own.
     */
    private static void checkStandaloneJamoIsAllowed() throws Exception {
        PlainDocument document = newFilteredDocument();
        document.insertString(0, "ㅋㅋ", null); // ㅋㅋ
        check("ㅋㅋ".equals(document.getText(0, document.getLength())),
                "Standalone jamo like ㅋㅋ must be allowed");
    }

    /** Punctuation and other scripts are silently dropped, not rejected
     * wholesale — the allowed characters in a mixed string still land. */
    private static void checkDisallowedCharactersAreDropped() throws Exception {
        PlainDocument document = newFilteredDocument();
        document.insertString(0, "A!b@3#", null);
        check("Ab3".equals(document.getText(0, document.getLength())),
                "Punctuation is dropped, allowed characters still inserted");
    }

    /** Space specifically must never appear (it is also the save file's
     * line-based record delimiter, not just a display concern). */
    private static void checkSpaceIsDropped() throws Exception {
        PlainDocument document = newFilteredDocument();
        document.insertString(0, "A B C", null);
        check("ABC".equals(document.getText(0, document.getLength())),
                "Space must be dropped from the name");
    }

    /** insertString() must not let the document grow past the cap. */
    private static void checkInsertStopsAtTenCharacters() throws Exception {
        PlainDocument document = newFilteredDocument();
        document.insertString(0, "01234567890123", null);
        check(document.getLength() == NameInputFilter.MAX_LENGTH,
                "insertString must stop exactly at the 10-character cap");
        check("0123456789".equals(document.getText(0, document.getLength())),
                "The first 10 allowed characters are kept, the rest dropped");
    }

    /** replace() must also respect the cap for a plain over-length paste. */
    private static void checkReplaceStopsAtTenCharacters() throws Exception {
        PlainDocument document = newFilteredDocument();
        document.replace(0, 0, "01234567890123", null);
        check(document.getLength() == NameInputFilter.MAX_LENGTH,
                "replace must also stop exactly at the 10-character cap");
    }

    /**
     * The core IME-composition correctness check. At exactly the
     * 10-character cap, an in-progress Hangul syllable keeps being
     * re-composed via replace() calls that swap the last character for
     * another single character (e.g. ㅎ -> 하 -> 한 in the 10th slot).
     * Each such edit nets to zero length change and must be allowed, or
     * composition would silently stop working one character before the
     * limit.
     */
    private static void checkComposingReplaceAtTheCapIsNotBlocked()
            throws Exception {
        PlainDocument document = newFilteredDocument();
        document.insertString(0, "123456789", null); // 9 characters
        document.replace(9, 0, "ㅎ", null); // + ㅎ -> 10 characters (at cap)
        check(document.getLength() == NameInputFilter.MAX_LENGTH,
                "Composing the 10th character must reach the cap");

        // The IME now replaces that single composing character with the
        // next stage of composition (still one character): net change
        // is zero, so this must succeed even though the document is
        // already exactly at the cap.
        document.replace(9, 1, "하", null); // ㅎ -> 하
        check(document.getLength() == NameInputFilter.MAX_LENGTH,
                "A same-length composing replace at the cap must not be blocked");
        check("123456789하".equals(document.getText(0, document.getLength())),
                "The composing character is updated in place at the cap");

        document.replace(9, 1, "한", null); // 하 -> 한
        check("123456789한".equals(document.getText(0, document.getLength())),
                "Composition can keep updating in place at the cap");
    }

    /**
     * If a replace() at the cap would actually grow the document (not
     * just swap one character for another), the filter must truncate the
     * inserted text to fit rather than silently drop the whole edit.
     */
    private static void checkGrowingReplaceAtTheCapIsTruncatedNotBlocked()
            throws Exception {
        PlainDocument document = newFilteredDocument();
        document.insertString(0, "0123456789", null); // at cap
        document.replace(9, 1, "XYZ", null); // would grow by 2 if unchecked
        check(document.getLength() == NameInputFilter.MAX_LENGTH,
                "A growing replace at the cap must be truncated to fit, not ignored");
        check("012345678X".equals(document.getText(0, document.getLength())),
                "Only as much of the replacement as fits is kept");
    }

    /** Deleting characters (Backspace/Delete) must always work, even
     * though remove() is not overridden by the filter at all. */
    private static void checkRemoveIsNeverFiltered() throws Exception {
        PlainDocument document = newFilteredDocument();
        document.insertString(0, "0123456789", null);
        document.remove(5, 5);
        check("01234".equals(document.getText(0, document.getLength())),
                "Removing characters must always be allowed");
    }
}
