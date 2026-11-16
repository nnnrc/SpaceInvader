package screen;

import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
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
 * @author <a href="mailto:RobertoIA1987@gmail.com">Roberto Izquierdo Amo</a>
 *
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
	 *             Text to filter.
	 * @return The same text, with disallowed characters removed.
	 */
	private static String keepAllowed(final String text) {
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
	 * Truncates filtered text so that, once it replaces a given range,
	 * the document does not exceed {@link #MAX_LENGTH} characters.
	 *
	 * @param filteredText
	 *                      Text already stripped of disallowed
	 *                      characters.
	 * @param currentLength
	 *                      The document's current length.
	 * @param replacedLength
	 *                       Length of the range this text is replacing
	 *                       (0 for a plain insert).
	 * @return Text truncated to fit within the length cap.
	 */
	private static String truncateToFit(final String filteredText,
			final int currentLength, final int replacedLength) {
		int lengthAfterRemoval = currentLength - replacedLength;
		int roomLeft = MAX_LENGTH - lengthAfterRemoval;
		if (roomLeft <= 0)
			return "";
		if (filteredText.length() <= roomLeft)
			return filteredText;
		return filteredText.substring(0, roomLeft);
	}

	@Override
	public void insertString(final FilterBypass fb, final int offset,
			final String string, final AttributeSet attr)
			throws BadLocationException {
		String text = truncateToFit(keepAllowed(string),
				fb.getDocument().getLength(), 0);
		if (!text.isEmpty())
			super.insertString(fb, offset, text, attr);
	}

	@Override
	public void replace(final FilterBypass fb, final int offset,
			final int length, final String text, final AttributeSet attrs)
			throws BadLocationException {
		String filtered = truncateToFit(keepAllowed(text),
				fb.getDocument().getLength(), length);
		// Still perform the replace even when filtered is empty: this is
		// how an in-progress Hangul composition is cleared/shortened
		// (e.g. backspacing mid-syllable), not just how new text is
		// rejected.
		super.replace(fb, offset, length, filtered, attrs);
	}

	// remove() is intentionally not overridden: deleting characters
	// (Backspace/Delete) must always be allowed, regardless of what the
	// deleted characters were.
}
