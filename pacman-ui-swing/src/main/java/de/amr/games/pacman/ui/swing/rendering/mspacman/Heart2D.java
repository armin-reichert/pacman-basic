package de.amr.games.pacman.ui.swing.rendering.mspacman;

import static de.amr.games.pacman.model.world.PacManGameWorld.TS;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import de.amr.games.pacman.model.common.GameEntity;

public class Heart2D {

	private final GameEntity heart;
	private BufferedImage image;

	public Heart2D(GameEntity heart) {
		this.heart = heart;
	}

	public void setImage(BufferedImage image) {
		this.image = image;
	}

	public BufferedImage getImage() {
		return image;
	}

	public void render(Graphics2D g) {
		if (heart.visible) {
			int dx = -(image.getWidth() - TS) / 2, dy = -(image.getHeight() - TS) / 2;
			g.drawImage(image, (int) heart.position.x + dx, (int) heart.position.y + dy, null);
		}
	}
}