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

	public final BufferedImage sheet;
	public final int raster;

	public Spritesheet(BufferedImage image, int pixels) {
		sheet = image;
		raster = pixels;
	}

	public BufferedImage region(int x, int y, int width, int height) {
		return sheet.getSubimage(x, y, width, height);
	}

	public BufferedImage spriteRegion(int originX, int originY, int tileX, int tileY, int numTilesX, int numTilesY) {
		return sheet.getSubimage(originX + tileX * raster, originY + tileY * raster, numTilesX * raster,
				numTilesY * raster);
	}

	public BufferedImage spriteRegion(int tileX, int tileY, int numTilesX, int numTilesY) {
		return spriteRegion(0, 0, tileX, tileY, numTilesX, numTilesY);
	}

	public BufferedImage sprite(int originX, int originY, int tileX, int tileY) {
		return spriteRegion(originX, originY, tileX, tileY, 1, 1);
	}

	public BufferedImage sprite(int tileX, int tileY) {
		return sprite(0, 0, tileX, tileY);
	}

	public BufferedImage spriteAt(V2i tile) {
		return sprite(tile.x, tile.y);
	}
}