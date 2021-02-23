package de.amr.games.pacman.ui.swing.scene;

import java.awt.Dimension;
import java.awt.Graphics2D;

import de.amr.games.pacman.model.common.GameModel;
import de.amr.games.pacman.sound.SoundManager;
import de.amr.games.pacman.ui.swing.rendering.SwingRendering;

/**
 * Common game scene base class.
 * 
 * @author Armin Reichert
 */
public abstract class GameScene {

	protected final Dimension size;
	protected final SwingRendering rendering;
	protected GameModel game;
	protected SoundManager sounds;

	public GameScene(Dimension size, SwingRendering rendering, SoundManager sounds) {
		this.size = size;
		this.rendering = rendering;
		this.sounds = sounds;
	}

	public GameScene(Dimension size, SwingRendering rendering) {
		this.size = size;
		this.rendering = rendering;
	}

	public abstract void update();

	public void start() {
	}

	public void end() {
	}

	public abstract void render(Graphics2D g);

	public Dimension size() {
		return size;
	}

	public void setGame(GameModel game) {
		this.game = game;
	}
}