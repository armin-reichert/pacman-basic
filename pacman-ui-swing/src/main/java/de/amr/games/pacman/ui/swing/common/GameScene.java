package de.amr.games.pacman.ui.swing.common;

import java.awt.Dimension;
import java.awt.Graphics2D;

import de.amr.games.pacman.controller.PacManGameController;
import de.amr.games.pacman.ui.sound.SoundManager;
import de.amr.games.pacman.ui.swing.rendering.SwingRendering;

/**
 * Common game scene base class.
 * 
 * @author Armin Reichert
 */
public abstract class GameScene {

	protected final PacManGameController controller;
	protected final Dimension size;
	protected final SwingRendering rendering;
	protected final SoundManager sounds;

	public GameScene(PacManGameController controller, Dimension size, SwingRendering rendering, SoundManager sounds) {
		this.controller = controller;
		this.size = size;
		this.rendering = rendering;
		this.sounds = sounds;
	}

	public Dimension size() {
		return size;
	}

	public void start() {
	}

	public abstract void update();

	public void end() {
	}

	public abstract void render(Graphics2D g);

}