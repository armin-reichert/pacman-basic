package de.amr.games.pacman.ui.swing.rendering.mspacman;

import static de.amr.games.pacman.model.world.PacManGameWorld.TS;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import de.amr.games.pacman.model.common.GameEntity;
import de.amr.games.pacman.model.mspacman.JuniorBag;

public class JuniorBag2D {

	private final JuniorBag bag;
	private BufferedImage blueBag;
	private BufferedImage junior;

	public JuniorBag2D(JuniorBag bag) {
		this.bag = bag;
	}

	public void setBlueBag(BufferedImage blueBag) {
		this.blueBag = blueBag;
	}

	public void setJunior(BufferedImage junior) {
		this.junior = junior;
	}

	private void drawEntity(Graphics2D g, GameEntity entity, BufferedImage sprite) {
		int dx = -(sprite.getWidth() - TS) / 2, dy = -(sprite.getHeight() - TS) / 2;
		g.drawImage(sprite, (int) (entity.position.x + dx), (int) (entity.position.y + dy), null);
	}

	public void render(Graphics2D g) {
		if (bag.visible) {
			if (bag.open) {
				drawEntity(g, bag, junior);
			} else {
				drawEntity(g, bag, blueBag);
			}
		}
	}
}