package de.amr.games.pacman.lib;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

public class MapReader {

	public static byte[][] readMap(String path) {
		int size_x = 28, size_y = 36;
		byte[][] map = new byte[size_y][size_x];
		try (InputStream is = MapReader.class.getResourceAsStream(path)) {
			if (is == null) {
				throw new RuntimeException("Resource not found: " + path);
			}
			BufferedReader rdr = new BufferedReader(new InputStreamReader(is));
			for (int y = 0; y < size_y; ++y) {
				String line = rdr.readLine();
				for (int x = 0; x < size_x; ++x) {
					map[y][x] = decode(line.charAt(x));
				}
			}
		} catch (Exception x) {
			throw new RuntimeException("Error reading map from path " + path, x);
		}
		return map;
	}

	private static byte decode(char c) {
		switch (c) {
		case '0':
			return 0;
		case '1':
			return 1;
		case '2':
			return 2;
		default:
			throw new IllegalArgumentException("Unknown map character: " + c);
		}
	}

}
