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
package de.amr.games.pacman.model.common.world;

import java.text.ParseException;

import de.amr.games.pacman.lib.V2i;

/**
 * Parses definitions of the following forms:
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
public class DefinitionParser {

	public static class Definition {
		public String name;
		public Object value;
	}

	private void parse_error(String message, Object... args) {
		throw new RuntimeException(String.format(message, args));
	}

	/**
	 * @param line line of text
	 * @return definition specified by this line or {@code null} if line does not contain a definition
	 * @throws ParseException
	 */
	public Definition parse(String line) {
		if (line.startsWith("val ")) {
			line = line.substring(4).trim();
			String[] sides = line.split("=");
			if (sides.length != 2) {
				parse_error("Malformed definition '%s'", line);
			}
			Definition def = new Definition();
			def.name = sides[0].trim();
			def.value = parseRightHandSide(sides[1].trim());
			return def;
		}
		return null;
	}

	private Object parseRightHandSide(String text) {
		String s = text.trim();
		if (s.startsWith("(")) {
			return parseVector(s);
		} else if (s.startsWith("\"")) {
			return parseString(s);
		} else {
			return parseInt(s);
		}
	}

	private V2i parseVector(String text) {
		String s = text;
		if (!s.endsWith(")")) {
			parse_error("'%s' does not specify a vector", text);
		}
		s = s.substring(1, s.length() - 1); // remove enclosing parentheses
		String[] components = s.split(",");
		if (components.length != 2) {
			parse_error("'%s' does not specify a vector", text);
		}
		int x = parseInt(components[0].trim());
		int y = parseInt(components[1].trim());
		return new V2i(x, y);
	}

	private String parseString(String text) {
		if (text.length() < 2) {
			parse_error("'%s' is not enclosed in quotes", text);
		}
		if (!text.endsWith("\"")) {
			parse_error("'%s' has no closing quote", text);
		}
		return text.substring(1, text.length() - 1);
	}

	private int parseInt(String text) {
		try {
			return Integer.parseInt(text);
		} catch (NumberFormatException x) {
			parse_error("'%s' cannot be parsed as an integer", text);
		}
		return 0;
	}
}