package de.amr.games.pacman.ui.swing.mspacman.entities;

import static de.amr.games.pacman.ui.swing.PacManGameUI_Swing.RENDERING_MSPACMAN;

import java.awt.Graphics2D;

import de.amr.games.pacman.model.guys.GameEntity;

public class Stork extends GameEntity {

	public Stork() {
		animation = RENDERING_MSPACMAN.storkFlying();
	}

	public void draw(Graphics2D g) {
		if (visible) {
			RENDERING_MSPACMAN.drawStorkSprite(g, position.x, position.y);
		}
	}
}