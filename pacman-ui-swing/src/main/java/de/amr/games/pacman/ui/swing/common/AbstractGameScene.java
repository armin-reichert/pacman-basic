package de.amr.games.pacman.ui.swing.common;

import java.awt.Dimension;
import java.awt.Graphics2D;

import de.amr.games.pacman.model.PacManGameModel;
import de.amr.games.pacman.ui.swing.GameScene;

public class AbstractGameScene implements GameScene {

	protected final Dimension size;
	protected PacManGameModel game;

	public AbstractGameScene(Dimension size) {
		this.size = size;
	}

	public void setGame(PacManGameModel game) {
		this.game = game;
	}

	@Override
	public void update() {
	}

	@Override
	public void render(Graphics2D g) {
	}

	@Override
	public Dimension sizeInPixel() {
		return size;
	}
}