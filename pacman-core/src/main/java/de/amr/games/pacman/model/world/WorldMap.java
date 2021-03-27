package de.amr.games.pacman.model.world;

import static de.amr.games.pacman.lib.Logging.log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import de.amr.games.pacman.lib.V2i;

/**
 * Map of the game world, created from a textual representation.
 * 
 * @author Armin Reichert
 */
public class WorldMap {

	public static final byte UNDEFINED = -1, SPACE = 0, WALL = 1, PILL = 2, ENERGIZER = 3, DOOR = 4, TUNNEL = 5;

	private static byte decode(char c) {
		switch (c) {
		case ' ':
			return SPACE;
		case '#':
			return WALL;
		case 'T':
			return TUNNEL;
		case '-':
			return DOOR;
		case '.':
			return PILL;
		case '*':
			return ENERGIZER;
		default:
			return UNDEFINED;
		}
	}

	private static void error(String message, Object... args) {
		log("Error parsing map: %s", String.format(message, args));
	}

	public static WorldMap from(String resourcePath) {
		log("Read world map '%s'", resourcePath);
		try (BufferedReader rdr = new BufferedReader(
				new InputStreamReader(WorldMap.class.getResourceAsStream(resourcePath)))) {
			WorldMap map = new WorldMap();
			map.parse(rdr.lines());
			return map;
		} catch (IOException x) {
			throw new RuntimeException(x);
		}
	}

	private Map<String, Object> values = new HashMap<>();
	private byte[][] content;

	public byte data(V2i tile) {
		return data(tile.x, tile.y);
	}

	public byte data(int x, int y) {
		return content[y][x]; // row-wise order!
	}

	public V2i vector(String varName) {
		Object value = values.get(varName);
		if (value == null) {
			error("Variable '%s' is not defined", varName);
			return V2i.NULL;
		}
		if (!(value instanceof V2i)) {
			error("Variable '%s' does not contain a vector", varName);
			return V2i.NULL;
		}
		return (V2i) value;
	}

	public Optional<V2i> vectorOptional(String varName) {
		Object value = values.get(varName);
		if (value == null) {
			return Optional.empty();
		}
		if (!(value instanceof V2i)) {
			error("Variable '%s' does not contain a vector", varName);
			return Optional.empty();
		}
		return Optional.of((V2i) value);
	}

	/**
	 * 
	 * @param listName the list name (prefix before the dot in list variable assignments), e.g.
	 *                 <code>level</code> for list entries like <code>level.42</code>
	 * @return list of all values for given list name
	 */
	public List<V2i> list(String listName) {
		return values.keySet().stream()//
				.filter(varName -> varName.startsWith(listName + "."))//
				.sorted()//
				.map(this::vector)//
				.collect(Collectors.toList());
	}

	private void parse(Stream<String> lines) {
		List<String> dataLines = new ArrayList<>();
		lines.forEach(line -> {
			if (line.startsWith("!")) {
				// skip comment lines
			} else if (line.startsWith("val ")) {
				// val <variable> = <value>
				line = line.substring(4).trim();
				String[] sides = line.split("=");
				if (sides.length != 2) {
					error("Malformed val statement: %s", line);
				}
				String lhs = sides[0].trim();
				String rhs = sides[1].trim();
				values.put(lhs, parseRightHandSide(rhs));
			} else {
				dataLines.add(line);
			}
		});

		V2i size = vector("size");
		if (dataLines.size() != size.y) {
			error("Specified map height %d does not match number of data lines %d", size.y, dataLines.size());
		}
		content = new byte[size.y][size.x]; // stored row-wise!
		for (int row = 0; row < size.y; ++row) {
			for (int col = 0; col < size.x; ++col) {
				char c = dataLines.get(row).charAt(col);
				byte value = decode(c);
				if (value == UNDEFINED) {
					error("Found undefined map character at row %d, col %d: '%s'", row, col, c);
					content[row][col] = SPACE;
				} else {
					content[row][col] = value;
				}
			}
		}
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
			error("Error parsing vector from %s", text);
			return null;
		}
		s = s.substring(1, s.length() - 1); // remove enclosing parentheses
		String[] components = s.split(",");
		if (components.length != 2) {
			error("Error parsing vector from %s", text);
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
			error("Could not parse integer variable from text '%s'", text);
			return 0;
		}
	}
}