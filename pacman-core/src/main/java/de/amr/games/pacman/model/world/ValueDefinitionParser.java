package de.amr.games.pacman.model.world;

import static de.amr.games.pacman.lib.Logging.log;

import java.util.Map;

import de.amr.games.pacman.lib.V2i;

/**
 * Parses a value definition of one the following forms:
 * 
 * <pre>
 * val int_value = 42
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
		String s = text.trim();
		if (s.startsWith("(")) {
			return parseVector(s);
		} else {
			return parseInt(s);
		}
	}

	private V2i parseVector(final String text) {
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

	private int parseInt(final String text) {
		try {
			return Integer.parseInt(text);
		} catch (Exception x) {
			error("Error parsing int value from '%s'", text);
			return 0;
		}
	}
}