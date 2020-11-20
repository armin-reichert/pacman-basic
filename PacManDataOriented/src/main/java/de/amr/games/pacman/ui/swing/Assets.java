package de.amr.games.pacman.ui.swing;

import static de.amr.games.pacman.World.TS;

import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import javax.imageio.ImageIO;

public class Assets {

	public final BufferedImage imageMazeFull;
	public final BufferedImage imageMazeEmpty;
	public final BufferedImage imageMazeEmptyWhite;
	public final BufferedImage spriteSheet;
	public final BufferedImage imageLive;
	public final Map<String, BufferedImage> symbols;
	public final Map<Integer, BufferedImage> numbers;
	public final Map<Integer, BufferedImage> bountyNumbers;
	public final Font scoreFont;

	public Assets() {
		spriteSheet = image("/sprites.png");

		imageMazeFull = image("/maze_full.png");
		imageMazeEmpty = image("/maze_empty.png");
		imageMazeEmptyWhite = image("/maze_empty_white.png");
		imageLive = section(8, 1);

		scoreFont = font("/PressStart2P-Regular.ttf", TS);

		//@formatter:off
		symbols = Map.of(
			"Cherries",   section(2, 3),
			"Strawberry", section(3, 3),
			"Peach",      section(4, 3),
			"Apple",      section(5, 3),
			"Grapes",     section(6, 3),
			"Galaxian",   section(7, 3),
			"Bell",       section(8, 3),
			"Key",        section(9, 3)
		);
	
		numbers = Map.of(
			100,  section(0, 9),
			300,  section(1, 9),
			500,  section(2, 9),
			700,  section(3, 9),
			1000, section(4, 9, 2, 1),
			2000, section(4, 10, 2, 1),
			3000, section(4, 11, 2, 1),
			5000, section(4, 12, 2, 1)
		);
	
		bountyNumbers = Map.of(
			200,  section(0, 8),
			400,  section(1, 8),
			800,  section(2, 8),
			1600, section(3, 8)
		);
		//@formatter:on
	}

	public BufferedImage section(int x, int y, int w, int h) {
		return spriteSheet.getSubimage(x * 16, y * 16, w * 16, h * 16);
	}

	public BufferedImage section(int x, int y) {
		return section(x, y, 1, 1);
	}

	public BufferedImage image(String path) {
		InputStream is = getClass().getResourceAsStream(path);
		if (is == null) {
			throw new RuntimeException(String.format("Could not access resource, path='%s'", path));
		}
		try {
			return ImageIO.read(is);
		} catch (IOException x) {
			throw new RuntimeException(String.format("Could not load image, path='%s'", path));
		}
	}

	public Font font(String fontPath, int size) {
		try (InputStream fontData = getClass().getResourceAsStream(fontPath)) {
			return Font.createFont(Font.TRUETYPE_FONT, fontData).deriveFont((float) size);
		} catch (IOException x) {
			throw new RuntimeException(String.format("Could not access font, path='%s'", fontPath));
		} catch (FontFormatException x) {
			throw new RuntimeException(String.format("Could not create font, path='%s'", fontPath));
		}
	}
}