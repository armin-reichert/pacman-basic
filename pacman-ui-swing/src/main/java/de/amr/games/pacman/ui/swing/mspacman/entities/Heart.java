package de.amr.games.pacman.ui.swing.mspacman.entities;

import static de.amr.games.pacman.ui.swing.PacManGameUI_Swing.RENDERING_MSPACMAN;

import java.awt.Graphics2D;

import de.amr.games.pacman.model.guys.GameEntity;

public class Heart extends GameEntity {

	public void draw(Graphics2D g) {
		RENDERING_MSPACMAN.drawHeart(g, this);
	}
}
