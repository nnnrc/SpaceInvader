package screen;

import javax.swing.SwingUtilities;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.DocumentFilter;

/**
 * Restricts a score name field to Hangul (composed syllables and
 * standalone jamo), English letters and digits, and caps it at
 * {@value #MAX_LENGTH} characters.
 *
 * Both {@link #insertString} and {@link #replace} are overridden,
 * because Swing delivers an IME's in-progress Hangul composition (e.g.
 * ㅎ, then ㅎ+ㅏ replacing it with 하, then that replaced with 한) through
 * {@code replace}, not {@code insertString}. Overriding only one of the
 * two would leave composition unfiltered.
 *
 * The length cap is enforced on the net result of the edit
 * (current length - replaced length + inserted length), not on the
 * inserted text's length alone: a composition update that replaces one
 * provisional character with another nets to zero length change and
 * must be allowed even exactly at the cap, or Hangul composition would
 * silently break one character before the limit.
 *
 * Disallowed characters (space, punctuation) are not dropped inside the
 * edit itself. An input method commits its text together with the key
 * that ended the composition (for example "글 " when Space is pressed
 * after 글) and expects the document to grow by exactly that much. If the
 * filter dropped the space right there, the document would end up one
 * character shorter than the input method expects, and on macOS it then
 * deletes the character before the committed one. So the edit is applied
 * as is, and the disallowed characters are removed right after it, on
 * the next turn of the Swing event thread.
 */
public final class NameInputFilter extends DocumentFilter {

	/** Maximum number of characters a score name may hold. */
	public static final int MAX_LENGTH = 10;

	/** First Hangul syllable code point (가). */
	private static final char HANGUL_SYLLABLES_FIRST = 0xAC00;
	/** Last Hangul syllable code point (힣). */
	private static final char HANGUL_SYLLABLES_LAST = 0xD7A3;
	/** First Hangul compatibility jamo code point (ㄱ), covering
	 * standalone consonants/vowels such as ㅋ or ㅎ that never combine
	 * into a full syllable. */
	private static final char HANGUL_JAMO_FIRST = 0x3131;
	/** Last Hangul compatibility jamo code point (ㆎ). */
	private static final char HANGUL_JAMO_LAST = 0x318E;

	/**
	 * Checks whether a single character may appear in a score name:
	 * Hangul (composed syllables or standalone jamo), an English letter,
	 * or a digit. Everything else — space, punctuation, other scripts —
	 * is rejected.
	 *
	 * @param c
	 *          Character to check.
	 * @return True if the character is allowed.
	 */
	public static boolean isAllowed(final char c) {
		if (c >= 'A' && c <= 'Z')
			return true;
		if (c >= 'a' && c <= 'z')
			return true;
		if (c >= '0' && c <= '9')
			return true;
		if (c >= HANGUL_SYLLABLES_FIRST && c <= HANGUL_SYLLABLES_LAST)
			return true;
		return c >= HANGUL_JAMO_FIRST && c <= HANGUL_JAMO_LAST;
	}

	/**
	 * Drops every disallowed character from a string, keeping the rest
	 * in order.
	 *
	 * @param text
	 *             Text to clean up.
	 * @return The same text, with disallowed characters removed.
	 */
	public static String keepAllowed(final String text) {
		if (text == null || text.isEmpty())
			return "";
		StringBuilder kept = new StringBuilder(text.length());
		for (int i = 0; i < text.length(); i++) {
			char c = text.charAt(i);
			if (isAllowed(c))
				kept.append(c);
		}
		return kept.toString();
	}

	/**
	 * Counts the allowed characters of a string.
	 *
	 * @param text
	 *             Text to count.
	 * @return Number of allowed characters.
	 */
	private static int countAllowed(final String text) {
		int count = 0;
		for (int i = 0; i < text.length(); i++) {
			if (isAllowed(text.charAt(i)))
				count++;
		}
		return count;
	}

	/**
	 * Shortens the text of an edit so that, once the edit is applied, the
	 * document holds at most {@link #MAX_LENGTH} allowed characters.
	 * Disallowed characters are kept here; they are removed right after
	 * the edit (see {@link #removeDisallowedLater}).
	 *
	 * @param text
	 *             Text the edit inserts.
	 * @param allowedLeft
	 *             Allowed characters left in the document once the
	 *             replaced range is gone.
	 * @return Text shortened to fit within the length cap.
	 */
	private static String truncateToFit(final String text,
			final int allowedLeft) {
		int roomLeft = MAX_LENGTH - allowedLeft;
		StringBuilder kept = new StringBuilder(text.length());
		for (int i = 0; i < text.length(); i++) {
			char c = text.charAt(i);
			if (!isAllowed(c)) {
				kept.append(c);
			} else if (roomLeft > 0) {
				kept.append(c);
				roomLeft--;
			}
		}
		return kept.toString();
	}

	/**
	 * Counts the allowed characters left in a document once a range is
	 * removed from it.
	 */
	private static int allowedLeftAfterRemoving(final Document document,
			final int offset, final int length) throws BadLocationException {
		String all = document.getText(0, document.getLength());
		return countAllowed(all.substring(0, offset))
				+ countAllowed(all.substring(offset + length));
	}

	/**
	 * Removes every disallowed character from a document, from the last
	 * one to the first so that earlier offsets stay valid.
	 *
	 * @param document
	 *                 Document to clean up.
	 */
	static void removeDisallowed(final Document document) {
		try {
			String all = document.getText(0, document.getLength());
			for (int i = all.length() - 1; i >= 0; i--) {
				if (!isAllowed(all.charAt(i)))
					document.remove(i, 1);
			}
		} catch (BadLocationException e) {
			// The document changed in between; the next edit cleans it up.
		}
	}

	/**
	 * Schedules {@link #removeDisallowed} once the current edit (and the
	 * input method event that caused it) is finished.
	 */
	private static void removeDisallowedLater(final Document document,
			final String text) {
		if (countAllowed(text) != text.length())
			SwingUtilities.invokeLater(() -> removeDisallowed(document));
	}

	@Override
	public void insertString(final FilterBypass fb, final int offset,
			final String string, final AttributeSet attr)
			throws BadLocationException {
		if (string == null || string.isEmpty())
			return;
		Document document = fb.getDocument();
		String text = truncateToFit(string,
				allowedLeftAfterRemoving(document, offset, 0));
		if (!text.isEmpty()) {
			super.insertString(fb, offset, text, attr);
			removeDisallowedLater(document, text);
		}
	}

	@Override
	public void replace(final FilterBypass fb, final int offset,
			final int length, final String text, final AttributeSet attrs)
			throws BadLocationException {
		Document document = fb.getDocument();
		String fitted = text == null ? "" : truncateToFit(text,
				allowedLeftAfterRemoving(document, offset, length));
		// Still perform the replace even when fitted is empty: this is
		// how an in-progress Hangul composition is cleared/shortened
		// (e.g. backspacing mid-syllable), not just how new text is
		// rejected.
		super.replace(fb, offset, length, fitted, attrs);
		removeDisallowedLater(document, fitted);
	}

	// remove() is intentionally not overridden: deleting characters
	// (Backspace/Delete) must always be allowed, regardless of what the
	// deleted characters were.
}
