package de.amr.games.pacman.ui.swing.assets;

import java.awt.Color;
import java.awt.image.BufferedImage;

import de.amr.games.pacman.lib.V2i;

/**
 * A spritesheet.
 * 
 * @author Armin Reichert
 */
public class Spritesheet {

	public static BufferedImage createBrightEffect(BufferedImage src, Color borderColor, Color fillColor) {
		BufferedImage dst = new BufferedImage(src.getWidth(), src.getHeight(), src.getType());
		dst.getGraphics().drawImage(src, 0, 0, null);
		for (int x = 0; x < src.getWidth(); ++x) {
			for (int y = 0; y < src.getHeight(); ++y) {
				if (src.getRGB(x, y) == borderColor.getRGB()) {
					dst.setRGB(x, y, Color.WHITE.getRGB());
				} else if (src.getRGB(x, y) == fillColor.getRGB()) {
					dst.setRGB(x, y, Color.BLACK.getRGB());
				} else {
					dst.setRGB(x, y, src.getRGB(x, y));
				}
			}
		}
		return dst;
	}

	protected final BufferedImage sheet;
	protected final int gridSize;

	protected V2i origin = V2i.NULL;

	public Spritesheet(BufferedImage image, int rasterSize) {
		sheet = image;
		gridSize = rasterSize;
	}

	public void setOrigin(int x, int y) {
		origin = new V2i(x, y);
	}

	public BufferedImage spritesAt(int tileX, int tileY, int numTilesX, int numTilesY) {
		return sheet.getSubimage(origin.x + tileX * gridSize, origin.y + tileY * gridSize, numTilesX * gridSize,
				numTilesY * gridSize);
	}

	public BufferedImage spriteAt(int tileX, int tileY) {
		return spritesAt(tileX, tileY, 1, 1);
	}

	public BufferedImage spriteAt(V2i tile) {
		return spriteAt(tile.x, tile.y);
	}
}