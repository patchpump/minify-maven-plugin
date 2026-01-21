package patchpump.minify.maven.common;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;

/**
 * Replacement for YU CssCompressor which is inherently fragile.
 * 
 * Strips whitespace and comments with an honest attempt to not break things.
 */
public final class SafeCssCompressor {

	private final String css;

	public SafeCssCompressor(Reader in) throws IOException {
		this.css = readFully(in);
	}

	public void compress(Writer out) throws IOException {
		out.write(minify(css));
	}

	private static String readFully(Reader in) throws IOException {
		StringBuilder sb = new StringBuilder(16 * 1024);
		char[] buf = new char[8192];
		int n;
		while ((n = in.read(buf)) != -1) {
			sb.append(buf, 0, n);
		}
		return sb.toString();
	}

	private static String minify(String input) {
		StringBuilder out = new StringBuilder(input.length());

		boolean inString = false;
		char stringQuote = 0;
		boolean stringEscaped = false;

		boolean inComment = false;

		boolean inUrlUnquoted = false;
		boolean urlEscaped = false;

		boolean pendingSpace = false;
		char lastOut = 0;

		final int len = input.length();

		for (int i = 0; i < len; i++) {
			char c = input.charAt(i);

			if (!inString && !inUrlUnquoted && i + 1 < len) {
				char next = input.charAt(i + 1);

				if (!inComment && c == '/' && next == '*') {
					inComment = true;
					pendingSpace = true;
					i++;
					continue;
				}

				if (inComment && c == '*' && next == '/') {
					inComment = false;
					i++;
					continue;
				}
			}

			if (inComment) {
				continue;
			}

			if (!inString && (c == '"' || c == '\'')) {
				flushPendingSpace(out, pendingSpace, lastOut, c);
				pendingSpace = false;

				out.append(c);
				lastOut = c;

				inString = true;
				stringQuote = c;
				stringEscaped = false;
				continue;
			}

			if (inString) {
				out.append(c);
				lastOut = c;

				if (stringEscaped) {
					stringEscaped = false;
					continue;
				}

				if (c == '\\') {
					stringEscaped = true;
					continue;
				}

				if (c == stringQuote) {
					inString = false;
				}
				continue;
			}

			if (!inUrlUnquoted && looksLikeUrlFunctionStart(input, i)) {
				flushPendingSpace(out, pendingSpace, lastOut, input.charAt(i));
				pendingSpace = false;

				out.append(input.charAt(i));
				out.append(input.charAt(i + 1));
				out.append(input.charAt(i + 2));
				lastOut = input.charAt(i + 2);
				i += 3;

				while (i < len && isWhitespace(input.charAt(i))) {
					out.append(input.charAt(i));
					lastOut = input.charAt(i);
					i++;
				}
				if (i < len && input.charAt(i) == '(') {
					out.append('(');
					lastOut = '(';
				} else {
					i--;
					continue;
				}

				int j = i + 1;
				while (j < len && isWhitespace(input.charAt(j))) {
					j++;
				}
				if (j < len) {
					char q = input.charAt(j);
					if (q == '"' || q == '\'') {
						continue;
					}
				}

				inUrlUnquoted = true;
				urlEscaped = false;
				continue;
			}

			if (inUrlUnquoted) {
				out.append(c);
				lastOut = c;

				if (urlEscaped) {
					urlEscaped = false;
					continue;
				}

				if (c == '\\') {
					urlEscaped = true;
					continue;
				}

				if (c == ')') {
					inUrlUnquoted = false;
				}
				continue;
			}

			if (isWhitespace(c)) {
				pendingSpace = true;
				continue;
			}

			flushPendingSpace(out, pendingSpace, lastOut, c);
			pendingSpace = false;

			if (isUniversallySafePunctuation(c)) {
				out.append(c);
				lastOut = c;
				continue;
			}

			out.append(c);
			lastOut = c;
		}

		rtrimSpaces(out);
		return out.toString();
	}

	private static void flushPendingSpace(StringBuilder out, boolean pendingSpace, char lastOut, char nextToken) {
		if (!pendingSpace)
			return;
		if (out.length() == 0)
			return;

		if (lastOut == '{' || lastOut == ';' || lastOut == ',')
			return;
		if (nextToken == '}' || nextToken == '{' || nextToken == ';' || nextToken == ',')
			return;

		if (lastOut != ' ') {
			out.append(' ');
		}
	}

	private static void rtrimSpaces(StringBuilder sb) {
		int n = sb.length();
		while (n > 0 && sb.charAt(n - 1) == ' ') {
			n--;
		}
		sb.setLength(n);
	}

	private static boolean isWhitespace(char c) {
		return c == ' ' || c == '\n' || c == '\r' || c == '\t' || c == '\f';
	}

	private static boolean isUniversallySafePunctuation(char c) {
		return c == '{' || c == '}' || c == ';' || c == ',';
	}

	private static boolean looksLikeUrlFunctionStart(String s, int i) {
		if (i > 0 && isIdentCharAscii(s.charAt(i - 1))) {
			return false;
		}
		if (i + 3 >= s.length()) {
			return false;
		}
		if (!isAsciiCaseInsensitive(s.charAt(i), 'u') || !isAsciiCaseInsensitive(s.charAt(i + 1), 'r') || !isAsciiCaseInsensitive(s.charAt(i + 2), 'l')) {
			return false;
		}
		int j = i + 3;
		while (j < s.length() && isWhitespace(s.charAt(j))) {
			j++;
		}
		return j < s.length() && s.charAt(j) == '(';
	}

	private static boolean isIdentCharAscii(char c) {
		return (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') || (c >= '0' && c <= '9') || c == '_' || c == '-';
	}

	private static boolean isAsciiCaseInsensitive(char actual, char expectedLower) {
		if (actual == expectedLower) {
			return true;
		}
		if (actual >= 'A' && actual <= 'Z') {
			return (char) (actual + ('a' - 'A')) == expectedLower;
		}
		return false;
	}
}
