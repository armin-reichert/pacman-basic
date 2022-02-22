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

import de.amr.games.pacman.lib.Misc;
import de.amr.games.pacman.lib.V2i;

/**
 * Game world map, created from simple textual representation.
 * 
 * @author Armin Reichert
 */
public class WorldMap {

	public static final byte UNDEFINED = -1, SPACE = 0, WALL = 1, PILL = 2, ENERGIZER = 3, DOOR = 4, TUNNEL = 5;

	private static byte decode(char c) {
		return switch (c) {
		case ' ' -> SPACE;
		case '#' -> WALL;
		case 'T' -> TUNNEL;
		case '-' -> DOOR;
		case '.' -> PILL;
		case '*' -> ENERGIZER;
		default -> UNDEFINED;
		};
	}

	public static WorldMap load(String path) {
		var map = new WorldMap();
		try (BufferedReader r = new BufferedReader(
				new InputStreamReader(WorldMap.class.getResourceAsStream(path), StandardCharsets.UTF_8))) {
			parse(map, r.lines());
			return map;
		} catch (IOException x) {
			throw new RuntimeException(x);
		}
	}

	private static void parse(WorldMap map, Stream<String> lines) {
		var parser = new ValueDefinitionParser();
		var dataLines = new ArrayList<String>();
		lines.forEach(line -> {
			if (line.startsWith("!")) {
				// comment, ignore
			} else {
				// value definition?
				Map.Entry<String, ?> def = parser.parse(line);
				if (def != null) {
					map.defs.put(def.getKey(), def.getValue());
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
	}

	private static void parse_error(String message, Object... args) {
		log("Error parsing map: %s", String.format(message, args));
	}

	private final Map<String, Object> defs = new HashMap<>();
	private byte[][] content;

	public byte data(V2i tile) {
		return data(tile.x, tile.y);
	}

	public byte data(int x, int y) {
		return content[y][x]; // row-wise order!
	}

	public Integer integer(String valueName) {
		return (Integer) defs.get(valueName);
	}

	public String string(String valueName) {
		return (String) defs.get(valueName);
	}

	public V2i vector(String valueName) {
		Object value = defs.get(valueName);
		if (value == null) {
			return null;
		}
		if (!(value instanceof V2i)) {
			parse_error("Value '%s' is not a vector", valueName);
			return null;
		}
		return (V2i) value;
	}

	public Optional<V2i> vectorOptional(String valueName) {
		Object value = defs.get(valueName);
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
	 * @param listName the list name (prefix before the dot in list variable assignments), e.g. <code>level</code> for
	 *                 list entries like <code>level.42</code>
	 * @return list of all values for given list name or {@code null} if no such list value exists
	 */
	public List<V2i> vectorList(String listName) {
		if (defs.keySet().stream().noneMatch(key -> key.startsWith(listName + "."))) {
			return null;
		}
		//@formatter:off
		return Misc.trim(
				defs.keySet().stream()
					.filter(varName -> varName.startsWith(listName + "."))
					.sorted()
					.map(this::vector)
					.collect(Collectors.toList())
		);
		//@formatter:on
	}
}