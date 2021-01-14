package de.amr.games.pacman.ui.swing;

import java.awt.Font;
import java.awt.image.BufferedImage;
import java.io.InputStream;

import javax.imageio.ImageIO;

public class Assets {

	public static BufferedImage image(String path) {
		try (InputStream is = Assets.class.getResourceAsStream(path)) {
			return ImageIO.read(is);
		} catch (Exception x) {
			throw new AssetsException("Could not load image with path '%s'", path);
		}
	}

	public static Font font(String fontPath, int size) {
		try (InputStream fontData = Assets.class.getResourceAsStream(fontPath)) {
			return Font.createFont(Font.TRUETYPE_FONT, fontData).deriveFont((float) size);
		} catch (Exception x) {
			throw new AssetsException("Could not load font with path '%s'", fontPath);
		}
	}
}