package de.amr.games.pacman.ui.swing.mspacman.entities;

import static de.amr.games.pacman.ui.swing.PacManGameUI_Swing.RENDERING_MSPACMAN;

import java.awt.Graphics2D;

import de.amr.games.pacman.model.guys.GameEntity;

public class JuniorBag extends GameEntity {

	public boolean released;
	public boolean open;
	public int bounces;

	@Override
	public void move() {
		if (released) {
			velocity = velocity.sum(0, 0.04f);
		}
		super.move();
	}

	public void draw(Graphics2D g) {
		if (open) {
			RENDERING_MSPACMAN.drawJuniorSprite(g, position.x, position.y);
		} else {
			RENDERING_MSPACMAN.drawBlueBagSprite(g, position.x, position.y);
		}
	}
}