/*
MIT License

Copyright (c) 2021 Armin Reichert

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
 */
package de.amr.games.pacman.model.world;

import static de.amr.games.pacman.lib.Logging.log;

import java.util.Map;

import de.amr.games.pacman.lib.V2i;

/**
 * Parses a value definition of one the following forms:
 * 
 * <pre>
 * val int_value = 42
 * val string_value = "some_text"
 * val vector_value = (42,42)
 * val vector_array_value.0 = (0,1) 
 * val vector_array_value.1 = (0,2)
 * </pre>
 * 
 * @author Armin Reichert
 */
public class ValueDefinitionParser {

	private static void error(String message, Object... args) {
		log("Error parsing value definition '%s'", String.format(message, args));
	}

	public Map.Entry<String, Object> parse(String line) {
		if (line.startsWith("val ")) {
			// val <variable> = <value>
			line = line.substring(4).trim();
			String[] sides = line.split("=");
			if (sides.length != 2) {
				error("Malformed val statement '%s'", line);
			}
			String lhs = sides[0].trim();
			String rhs = sides[1].trim();
			return Map.entry(lhs, parseRightHandSide(rhs));
		}
		return null;
	}

	private Object parseRightHandSide(final String text) {
		String trimmedText = text.trim();
		if (trimmedText.startsWith("(")) {
			return parseVector(trimmedText);
		} else if (trimmedText.startsWith("\"")) {
			return parseString(trimmedText);
		} else {
			return parseInt(trimmedText);
		}
	}

	private V2i parseVector(String text) {
		String s = text;
		if (!s.endsWith(")")) {
			error("Error parsing vector from '%s'", text);
			return null;
		}
		s = s.substring(1, s.length() - 1); // remove enclosing parentheses
		String[] components = s.split(",");
		if (components.length != 2) {
			error("Error parsing vector from '%s'", text);
			return null;
		}
		int x = parseInt(components[0].trim());
		int y = parseInt(components[1].trim());
		return new V2i(x, y);
	}

	private String parseString(String text) {
		if (text.length() < 2) {
			error("Error parsing string, not enclosed in quotes");
			return null;
		}
		if (!text.endsWith("\"")) {
			error("Error parsing string, no closing quote");
			return null;
		}
		return text.substring(1, text.length() - 1);
	}

	private int parseInt(String text) {
		try {
			return Integer.parseInt(text);
		} catch (Exception x) {
			error("Error parsing int value from '%s'", text);
			return 0;
		}
	}
}