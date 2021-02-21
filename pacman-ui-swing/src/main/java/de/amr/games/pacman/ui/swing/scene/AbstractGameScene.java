package de.amr.games.pacman.ui.swing.scene;

import java.awt.Dimension;
import java.awt.Graphics2D;

import de.amr.games.pacman.model.GameModel;

public class AbstractGameScene implements GameScene {

	protected final Dimension size;
	protected GameModel game;

	public AbstractGameScene(Dimension size) {
		this.size = size;
	}

	public void setGame(GameModel game) {
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