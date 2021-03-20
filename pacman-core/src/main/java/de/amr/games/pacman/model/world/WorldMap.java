package de.amr.games.pacman.model.world;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.amr.games.pacman.lib.V2i;

/**
 * Map of the game world. Normally created from a textual representation.
 * 
 * @author Armin Reichert
 */
public class WorldMap {

	public static final byte UNDEFINED = -1, SPACE = 0, WALL = 1, PILL = 2, ENERGIZER = 3, DOOR = 4, TUNNEL = 5;

	private static List<String> variableNames = Arrays.asList("width", "height");

	private static void parseError(String message, Object... args) {
		throw new RuntimeException("Error parsing map: " + String.format(message, args));
	}

	private byte[][] data;
	private String path;

	private Map<String, String> variables = new HashMap<>();

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
		List<String> dataLines = new ArrayList<>();
		try (BufferedReader rdr = new BufferedReader(new InputStreamReader(getClass().getResourceAsStream(path)))) {
			rdr.lines().forEach(line -> {
				if (line.startsWith("!")) {
					// comment line, ignore
				} else if (line.startsWith(":")) {
					// line with variable assignment <variable> = <value>
					line = line.substring(1).trim();
					String[] parts = line.split("=");
					if (parts.length != 2) {
						parseError("Unparseable line: %s", line);
					}
					String lhs = parts[0].trim();
					String rhs = parts[1].trim();
					if (variableNames.contains(lhs)) {
						variables.put(lhs, rhs);
					}
				} else {
					// data line
					dataLines.add(line);
				}
			});
			int width = 0, height = 0;
			try {
				width = Integer.parseInt(variables.get("width"));
			} catch (Exception x) {
				parseError("Map contains no valid width definition");
			}
			if (width == 0) {
				parseError("Map width must be a positive number");
			}
			try {
				height = Integer.parseInt(variables.get("height"));
			} catch (Exception x) {
				parseError("Map contains no valid height definition");
			}
			if (height == 0) {
				parseError("Map height must be a positive number");
			}
			if (dataLines.size() != height) {
				parseError("Specified height %d is not consistent with number of data lines %d", height, dataLines.size());
			}
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

	public int width() {
		return data[0].length;
	}

	public int height() {
		return data.length;
	}

	public byte data(V2i tile) {
		return data[tile.y][tile.x];
	}

	public byte data(int col, int row) {
		return data[row][col];
	}
}