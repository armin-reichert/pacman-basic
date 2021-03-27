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

	private static void parseError(String message, Object... args) {
		throw new RuntimeException("Error parsing map: " + String.format(message, args));
	}

	private final Map<String, Object> assignments = new HashMap<>();
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
						parseError("Unparseable line: %s", line);
					}
					String lhs = tokens[0].trim();
					String rhs = tokens[1].trim();
					assignments.put(lhs, parseRhs(rhs));
				}

				else {
					dataLines.add(line);
				}

			});

			// assign property values from definitions found in map:

			size = getV2i("size");

			house_top_left = getV2i("house_top_left");
			house_bottom_right = getV2i("house_bottom_right");
			house_entry = getV2i("house_entry");
			house_seat_left = getV2i("house_seat_left");
			house_seat_center = getV2i("house_seat_center");
			house_seat_right = getV2i("house_seat_right");

			pacman_home = getV2i("pacman_home");

			scatterTiles = assignments.keySet().stream().filter(key -> key.startsWith("scatter.")).sorted().map(this::getV2i)
					.collect(Collectors.toList());

			upwardsBlockedTiles = assignments.keySet().stream().filter(key -> key.startsWith("upwards_blocked."))
					.map(this::getV2i).collect(Collectors.toList());

			if (dataLines.size() != size.y) {
				parseError("Specified map height %d does not match number of data lines %d", size.y, dataLines.size());
			}
			data = new byte[size.y][size.x];
			for (int row = 0; row < size.y; ++row) {
				for (int col = 0; col < size.x; ++col) {
					char c = dataLines.get(row).charAt(col);
					byte value = decode(c);
					if (value == UNDEFINED) {
						parseError("Found undefined map character at row %d, col %d: '%s'", row, col, c);
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
			parseError("Error parsing vector from %s", rhs);
			return null;
		}
		s = s.substring(1, s.length() - 1); // chomp parentheses
		String[] components = s.split(",");
		if (components.length != 2) {
			parseError("Error parsing vector from %s", rhs);
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
			parseError("Could not parse integer variable from text '%s'", rhs);
			return null;
		}
	}

	private V2i getV2i(String varName) {
		if (!assignments.containsKey(varName)) {
			parseError("Variable '%s' is not defined", varName);
			return V2i.NULL;
		}
		if (!(assignments.get(varName) instanceof V2i)) {
			parseError("Variable '%s' does not contain a vector", varName);
			return V2i.NULL;
		}
		return (V2i) assignments.get(varName);
	}
}