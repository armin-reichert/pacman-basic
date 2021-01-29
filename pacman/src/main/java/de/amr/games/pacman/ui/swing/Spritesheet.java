package de.amr.games.pacman.ui.swing;

import java.awt.image.BufferedImage;

import de.amr.games.pacman.lib.V2i;

public class Spritesheet {

	private int offsetX, offsetY;
	private final BufferedImage image;
	private final int tileSize;

	public Spritesheet(BufferedImage image, int tileSize) {
		this.image = image;
		this.tileSize = tileSize;
	}

	protected V2i v2(int x, int y) {
		return new V2i(x, y);
	}

	public void setOrigin(int x, int y) {
		offsetX = x;
		offsetY = y;
	}

	public BufferedImage subImage(int x, int y, int w, int h) {
		return image.getSubimage(x, y, w, h);
	}

	public BufferedImage spritesAt(int tileX, int tileY, int numTilesX, int numTilesY) {
		return image.getSubimage(offsetX + tileX * tileSize, offsetY + tileY * tileSize, numTilesX * tileSize,
				numTilesY * tileSize);
	}

	public BufferedImage spriteAt(int tileX, int tileY) {
		return spritesAt(tileX, tileY, 1, 1);
	}

	public BufferedImage spriteAt(V2i coord) {
		return spriteAt(coord.x, coord.y);
	}
}