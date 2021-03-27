package de.amr.games.pacman.model.world;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import de.amr.games.pacman.lib.Logging;
import de.amr.games.pacman.lib.V2i;

/**
 * Map of the game world. Normally created from a textual representation.
 * 
 * @author Armin Reichert
 */
public class WorldMap {

	public static final byte UNDEFINED = -1, SPACE = 0, WALL = 1, PILL = 2, ENERGIZER = 3, DOOR = 4, TUNNEL = 5;

	public static WorldMap from(String resourcePath) {
		Logging.log("Read world map from %s", resourcePath);
		try (BufferedReader rdr = new BufferedReader(
				new InputStreamReader(WorldMap.class.getResourceAsStream(resourcePath)))) {
			WorldMap map = new WorldMap();
			map.parse(rdr);
			return map;
		} catch (IOException x) {
			throw new RuntimeException(x);
		}
	}

	private static void error(String message, Object... args) {
		throw new RuntimeException("Error parsing map: " + String.format(message, args));
	}

	private byte[][] data;

	public V2i size;
	public V2i house_top_left;
	public V2i house_bottom_right;
	public V2i house_entry;
	public V2i house_seat_left;
	public V2i house_seat_center;
	public V2i house_seat_right;
	public List<V2i> scatterTiles;
	public V2i pacman_home;
	public V2i bonus_home;
	public List<V2i> upwardsBlockedTiles;

	private byte decode(char c) {
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

	public byte data(V2i tile) {
		return data[tile.y][tile.x];
	}

	public byte data(int col, int row) {
		return data[row][col];
	}

	public boolean isInsideGhostHouse(V2i tile) {
		return tile.x >= house_top_left.x && tile.x <= house_bottom_right.x //
				&& tile.y >= house_top_left.y && tile.y <= house_bottom_right.y;
	}

	private void parse(BufferedReader rdr) {
		Map<String, Object> storedValues = new HashMap<>();
		List<String> dataLines = new ArrayList<>();
		try {
			rdr.lines().forEach(line -> {

				if (line.startsWith("!")) {
					// skip comment line
				}

				else if (line.startsWith(":")) {
					// <variable> = <value>
					line = line.substring(1).trim();
					String[] tokens = line.split("=");
					if (tokens.length != 2) {
						error("Unparseable line: %s", line);
					}
					String lhs = tokens[0].trim();
					String rhs = tokens[1].trim();
					storedValues.put(lhs, parseRhs(rhs));
				}

				else {
					dataLines.add(line);
				}

			});

			// assign property values from parsed definitions:
			size = getV2i("size", storedValues);
			house_top_left = getV2i("house_top_left", storedValues);
			house_bottom_right = getV2i("house_bottom_right", storedValues);
			house_entry = getV2i("house_entry", storedValues);
			house_seat_left = getV2i("house_seat_left", storedValues);
			house_seat_center = getV2i("house_seat_center", storedValues);
			house_seat_right = getV2i("house_seat_right", storedValues);
			pacman_home = getV2i("pacman_home", storedValues);
			bonus_home = getOptionalV2i("bonus_home", storedValues);
			scatterTiles = getV2iList("scatter", storedValues);
			upwardsBlockedTiles = getV2iList("upwards_blocked", storedValues);

			if (dataLines.size() != size.y) {
				error("Specified map height %d does not match number of data lines %d", size.y, dataLines.size());
			}
			data = new byte[size.y][size.x];
			for (int row = 0; row < size.y; ++row) {
				for (int col = 0; col < size.x; ++col) {
					char c = dataLines.get(row).charAt(col);
					byte value = decode(c);
					if (value == UNDEFINED) {
						error("Found undefined map character at row %d, col %d: '%s'", row, col, c);
						data[row][col] = SPACE;
					} else {
						data[row][col] = value;
					}
				}
			}
		} catch (Exception x) {
			x.printStackTrace();
		}
	}

	private Object parseRhs(String rhs) {
		rhs = rhs.trim();
		if (rhs.startsWith("(")) {
			return parseVector(rhs);
		} else {
			return parseInt(rhs);
		}
	}

	private V2i parseVector(final String rhs) {
		String s = rhs;
		if (!s.endsWith(")")) {
			error("Error parsing vector from %s", rhs);
			return null;
		}
		s = s.substring(1, s.length() - 1); // chomp parentheses
		String[] components = s.split(",");
		if (components.length != 2) {
			error("Error parsing vector from %s", rhs);
			return null;
		}
		int x = parseInt(components[0].trim());
		int y = parseInt(components[1].trim());
		return new V2i(x, y);
	}

	private Integer parseInt(String rhs) {
		try {
			return Integer.parseInt(rhs);
		} catch (Exception x) {
			error("Could not parse integer variable from text '%s'", rhs);
			return null;
		}
	}

	private V2i getV2i(String varName, Map<String, Object> storedValues) {
		Object storedValue = storedValues.get(varName);
		if (storedValue == null) {
			error("Variable '%s' is not defined", varName);
			return V2i.NULL;
		}
		if (!(storedValue instanceof V2i)) {
			error("Variable '%s' does not contain a vector", varName);
			return V2i.NULL;
		}
		return (V2i) storedValue;
	}

	private V2i getOptionalV2i(String varName, Map<String, Object> storedValues) {
		Object storedValue = storedValues.get(varName);
		if (storedValue == null) {
			return null;
		}
		if (!(storedValue instanceof V2i)) {
			error("Variable '%s' does not contain a vector", varName);
			return V2i.NULL;
		}
		return (V2i) storedValue;
	}

	private List<V2i> getV2iList(String listVarPrefix, Map<String, Object> storedValues) {
		return storedValues.keySet().stream().filter(varName -> varName.startsWith(listVarPrefix + ".")).sorted()
				.map(varName -> getV2i(varName, storedValues)).collect(Collectors.toList());
	}
}