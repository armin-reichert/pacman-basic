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

import static de.amr.games.pacman.lib.Logging.log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
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
import de.amr.games.pacman.model.common.world.DefinitionParser.Definition;

/**
 * World map defined by textual representation.
 * 
 * @author Armin Reichert
 */
public class TextualWorldMap {

	public static final byte UNDEF = -1, SPACE = 0, WALL = 1, PELLET = 2, ENERGIZER = 3, TUNNEL = 4, LEFT_DOOR_WING = 5,
			RIGHT_DOOR_WING = 6;

	private static byte token(char c) {
		return switch (c) {
		case ' ' -> SPACE;
		case '#' -> WALL;
		case 'T' -> TUNNEL;
		case 'L' -> LEFT_DOOR_WING;
		case 'R' -> RIGHT_DOOR_WING;
		case '.' -> PELLET;
		case '*' -> ENERGIZER;
		default -> UNDEF;
		};
	}

	private static void log_error(String message, Object... args) {
		log("Error parsing map: %s", String.format(message, args));
	}

	private final Map<String, Object> definitions = new HashMap<>();
	private byte[][] content;

	public TextualWorldMap(String path) {
		InputStream is = TextualWorldMap.class.getResourceAsStream(path);
		if (is == null) {
			throw new RuntimeException("Could not access resource " + path);
		}
		try (BufferedReader r = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
			parse(r.lines());
		} catch (IOException x) {
			throw new RuntimeException(x);
		}
	}

	private void parse(Stream<String> lines) {
		var parser = new DefinitionParser();
		var dataLines = new ArrayList<String>();
		lines.forEach(line -> {
			if (line.startsWith("!")) {
				// comment, ignore
			} else {
				Definition def = parser.parse(line);
				if (def != null) {
					definitions.put(def.name, def.value);
				} else {
					dataLines.add(line);
				}
			}
		});

		V2i size = vector("size");
		if (dataLines.size() != size.y) {
			log_error("Defined map height %d does not match number of data lines %d", size.y, dataLines.size());
		}

		content = new byte[size.y][size.x]; // stored row-wise!
		for (int row = 0; row < size.y; ++row) {
			for (int col = 0; col < size.x; ++col) {
				char c = dataLines.get(row).charAt(col);
				byte token = token(c);
				if (token == UNDEF) {
					log_error("Found undefined map character at row %d, col %d: '%s'", row, col, c);
					content[row][col] = SPACE;
				} else {
					content[row][col] = token;
				}
			}
		}
	}

	public byte data(V2i tile) {
		return data(tile.x, tile.y);
	}

	public byte data(int x, int y) {
		return content[y][x]; // row-wise order!
	}

	public Integer integer(String name) {
		return (Integer) definitions.get(name);
	}

	public String string(String name) {
		return (String) definitions.get(name);
	}

	public V2i vector(String name) {
		Object value = definitions.get(name);
		if (value == null) {
			return null;
		}
		if (!(value instanceof V2i)) {
			log_error("Value '%s' is not a vector", name);
			return null;
		}
		return (V2i) value;
	}

	public Optional<V2i> vectorOpt(String name) {
		Object value = definitions.get(name);
		if (value == null) {
			return Optional.empty();
		}
		if (!(value instanceof V2i)) {
			log_error("Value '%s' does not contain a vector", name);
			return Optional.empty();
		}
		return Optional.of((V2i) value);
	}

	/**
	 * @param listName the list name (prefix before the dot in list variable assignments), e.g. <code>level</code> for list
	 *             entries like <code>level.42</code>
	 * @return list of all values for given list name or {@code null} if no such list value exists
	 */
	public List<V2i> vectorList(String listName) {
		if (definitions.keySet().stream().noneMatch(key -> key.startsWith(listName + "."))) {
			return null;
		}
		//@formatter:off
		return Misc.trim(
				definitions.keySet().stream()
					.filter(varName -> varName.startsWith(listName + "."))
					.sorted()
					.map(this::vector)
					.collect(Collectors.toList())
		);
		//@formatter:on
	}
}