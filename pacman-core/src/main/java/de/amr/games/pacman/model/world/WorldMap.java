package de.amr.games.pacman.model.world;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

import de.amr.games.pacman.lib.Logging;
import de.amr.games.pacman.lib.V2i;

/**
 * Map of the game world. Normally created from a textual representation.
 * 
 * @author Armin Reichert
 */
public class WorldMap {

	public static final byte UNDEFINED = -1, SPACE = 0, WALL = 1, PILL = 2, ENERGIZER = 3, DOOR = 4, TUNNEL = 5;

	private static void parseError(String message, Object... args) {
		throw new RuntimeException("Error parsing map: " + String.format(message, args));
	}

	private final Map<String, Integer> definitions = new HashMap<>();
	private String path;
	private byte[][] data;

	public int width;
	public int height;

	public int house_x_min;
	public int house_x_max;
	public int house_y_min;
	public int house_y_max;
	public V2i house_entry;
	public V2i house_seat_left;
	public V2i house_seat_center;
	public V2i house_seat_right;

	public V2i pacHome = new V2i(13, 26);
	public V2i[] ghostScatterTargets = { new V2i(25, 0), new V2i(2, 0), new V2i(27, 35), new V2i(27, 35) };

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

	public WorldMap(String path) {
		this.path = path;
		parse();
	}

	private void parse() {

		Logging.log("Parsing map %s", path);

		List<String> dataLines = new ArrayList<>();
		try (BufferedReader rdr = new BufferedReader(new InputStreamReader(getClass().getResourceAsStream(path)))) {
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
					definitions.put(lhs, parseIntVar(lhs, rhs));
				}

				else {
					dataLines.add(line);
				}

			});

			// assign variables from definitions found in map
			width = assignInt("width", w -> w > 0, "Width must be greater than 0, but is %d");
			height = assignInt("height", h -> h > 0, "Height must be greater than 0, but is %d");
			if (dataLines.size() != height) {
				parseError("Specified height %d is not consistent with number of data lines %d", height, dataLines.size());
			}
			house_x_min = assignInt("house_x_min", x -> x > 0, "House min_x must be in map range, but is %d");
			house_x_max = assignInt("house_x_max", x -> x > 0, "House max_x must be in map range, but is %d");
			house_y_min = assignInt("house_y_min", y -> y > 0, "House min_y must be in map range, but is %d");
			house_y_max = assignInt("house_y_max", x -> x > 0, "House max_y must be in map range, but is %d");
			house_entry = assignVec("house_entry_x", "house_entry_y");
			house_seat_left = assignVec("house_seat_left_x", "house_seat_left_y");
			house_seat_center = assignVec("house_seat_center_x", "house_seat_center_y");
			house_seat_right = assignVec("house_seat_right_x", "house_seat_right_y");

			data = new byte[height][width];
			for (int row = 0; row < height; ++row) {
				for (int col = 0; col < width; ++col) {
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

	private int parseIntVar(String varName, String text) {
		try {
			return Integer.parseInt(text);
		} catch (Exception x) {
			parseError("Could not parse integer variable %s from text '%s'", varName, text);
			return 0;
		}
	}

	private int assignInt(String varName, Predicate<Integer> validity, String errorMessage) {
		if (!definitions.containsKey(varName)) {
			parseError("Variable %s is not defined", varName);
			return -1;
		}
		int value = definitions.get(varName);
		if (validity.test(value)) {
			return value;
		}
		parseError(errorMessage, value);
		return -1;
	}

	private V2i assignVec(String x, String y) {
		if (!definitions.containsKey(x)) {
			parseError("x-component '%s' not defined", x);
			return V2i.NULL;
		}
		if (!definitions.containsKey(y)) {
			parseError("y-component '%s' not defined", y);
			return V2i.NULL;
		}
		return new V2i(definitions.get(x), definitions.get(y));
	}

	public byte data(V2i tile) {
		return data[tile.y][tile.x];
	}

	public byte data(int col, int row) {
		return data[row][col];
	}

	public boolean isInsideGhostHouse(V2i tile) {
		return tile.x >= house_x_min && tile.x <= house_x_max && tile.y >= house_y_min && tile.y <= house_y_max;
	}
}