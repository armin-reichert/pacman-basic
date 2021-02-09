package de.amr.games.pacman.ui.swing.assets;

import java.awt.Font;
import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.net.URL;

import javax.imageio.ImageIO;

public class AssetLoader {

	public static URL url(String path) {
		return AssetLoader.class.getResource(path);
	}

	public static BufferedImage image(String path) {
		try (InputStream is = url(path).openStream()) {
			return ImageIO.read(is);
		} catch (Exception x) {
			throw new AssetException("Could not load image with path '%s'", path);
		}
	}

	public static Font font(String fontPath, int size) {
		try (InputStream fontData = url(fontPath).openStream()) {
			return Font.createFont(Font.TRUETYPE_FONT, fontData).deriveFont((float) size);
		} catch (Exception x) {
			throw new AssetException("Could not load font with path '%s'", fontPath);
		}
	}

}
