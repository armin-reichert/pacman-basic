package de.amr.games.pacman.model.world;

import static de.amr.games.pacman.lib.Logging.log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import de.amr.games.pacman.lib.V2i;

/**
 * Game world map, created from simple textual representation.
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

	public static WorldMap load(String path) {
		try (BufferedReader r = new BufferedReader(
				new InputStreamReader(WorldMap.class.getResourceAsStream(path), StandardCharsets.UTF_8))) {
			return parse(r.lines());
		} catch (IOException x) {
			throw new RuntimeException(x);
		}
	}

	private static WorldMap parse(Stream<String> lines) {
		var map = new WorldMap();
		var definitionParser = new ValueDefinitionParser();
		var dataLines = new ArrayList<String>();
		lines.forEach(line -> {
			if (line.startsWith("!")) {
				// comment, ignore
			} else {
				// value definition?
				Map.Entry<String, ?> def = definitionParser.parse(line);
				if (def != null) {
					map.definitions.put(def.getKey(), def.getValue());
				} else {
					dataLines.add(line);
				}
			}
		});

		V2i size = map.vector("size");
		if (dataLines.size() != size.y) {
			parse_error("Defined map height %d does not match number of data lines %d", size.y, dataLines.size());
		}

		map.content = new byte[size.y][size.x]; // stored row-wise!
		for (int row = 0; row < size.y; ++row) {
			for (int col = 0; col < size.x; ++col) {
				char c = dataLines.get(row).charAt(col);
				byte value = decode(c);
				if (value == UNDEFINED) {
					parse_error("Found undefined map character at row %d, col %d: '%s'", row, col, c);
					map.content[row][col] = SPACE;
				} else {
					map.content[row][col] = value;
				}
			}
		}
		return map;
	}

	private static void parse_error(String message, Object... args) {
		log("Error parsing map: %s", String.format(message, args));
	}

	private final Map<String, Object> definitions = new HashMap<>();
	private byte[][] content;

	public byte data(V2i tile) {
		return data(tile.x, tile.y);
	}

	public byte data(int x, int y) {
		return content[y][x]; // row-wise order!
	}

	public V2i vector(String valueName) {
		Object value = definitions.get(valueName);
		if (value == null) {
			parse_error("Value '%s' is not defined", valueName);
			return V2i.NULL;
		}
		if (!(value instanceof V2i)) {
			parse_error("Value '%s' does not contain a vector", valueName);
			return V2i.NULL;
		}
		return (V2i) value;
	}

	public Optional<V2i> vectorOptional(String valueName) {
		Object value = definitions.get(valueName);
		if (value == null) {
			return Optional.empty();
		}
		if (!(value instanceof V2i)) {
			parse_error("Value '%s' does not contain a vector", valueName);
			return Optional.empty();
		}
		return Optional.of((V2i) value);
	}

	/**
	 * @param listName the list name (prefix before the dot in list variable assignments), e.g.
	 *                 <code>level</code> for list entries like <code>level.42</code>
	 * @return list of all values for given list name
	 */
	public List<V2i> vectorList(String listName) {
		return definitions.keySet().stream()//
				.filter(varName -> varName.startsWith(listName + "."))//
				.sorted()//
				.map(this::vector)//
				.collect(Collectors.toList());
	}
}