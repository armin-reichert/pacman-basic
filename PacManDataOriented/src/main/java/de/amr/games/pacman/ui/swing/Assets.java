package de.amr.games.pacman.ui.swing;

import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import javax.imageio.ImageIO;

/**
 * Assets used in Pac-Man game.
 * 
 * @author Armin Reichert
 */
public class Assets {

	public final BufferedImage imageLogo;
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

		imageLogo = image("/logo.png");
		imageMazeFull = image("/maze_full.png");
		imageMazeEmpty = image("/maze_empty.png");
		imageMazeEmptyWhite = image("/maze_empty_white.png");
		imageLive = sheet(8, 1);

		scoreFont = font("/PressStart2P-Regular.ttf", 8);

		//@formatter:off
		symbols = Map.of(
			"Cherries",   sheet(2, 3),
			"Strawberry", sheet(3, 3),
			"Peach",      sheet(4, 3),
			"Apple",      sheet(5, 3),
			"Grapes",     sheet(6, 3),
			"Galaxian",   sheet(7, 3),
			"Bell",       sheet(8, 3),
			"Key",        sheet(9, 3)
		);
	
		numbers = Map.of(
			100,  sheet(0, 9),
			300,  sheet(1, 9),
			500,  sheet(2, 9),
			700,  sheet(3, 9),
			1000, section(4, 9, 2, 1),
			2000, section(3, 10, 3, 1),
			3000, section(3, 11, 3, 1),
			5000, section(3, 12, 3, 1)
		);
	
		bountyNumbers = Map.of(
			200,  sheet(0, 8),
			400,  sheet(1, 8),
			800,  sheet(2, 8),
			1600, sheet(3, 8)
		);
		//@formatter:on
	}

	public BufferedImage section(int x, int y, int w, int h) {
		return spriteSheet.getSubimage(x * 16, y * 16, w * 16, h * 16);
	}

	public BufferedImage sheet(int x, int y) {
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