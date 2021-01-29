package de.amr.games.pacman.ui.swing;

import java.awt.image.BufferedImage;

public class Spritesheet {

	private int offsetX, offsetY;
	private final BufferedImage image;
	private final int tileSize;

	public Spritesheet(BufferedImage image, int tileSize) {
		this.image = image;
		this.tileSize = tileSize;
	}

	public void setOrigin(int x, int y) {
		offsetX = x;
		offsetY = y;
	}

	public BufferedImage section(int x, int y, int w, int h) {
		return image.getSubimage(x, y, w, h);
	}

	public BufferedImage tiles(int tileX, int tileY, int numTilesX, int numTilesY) {
		return image.getSubimage(offsetX + tileX * tileSize, offsetY + tileY * tileSize, numTilesX * tileSize, numTilesY * tileSize);
	}

	public BufferedImage tile(int tileX, int tileY) {
		return tiles(tileX, tileY, 1, 1);
	}
}