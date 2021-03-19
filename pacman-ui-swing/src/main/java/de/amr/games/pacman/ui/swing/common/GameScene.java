package de.amr.games.pacman.ui.swing.common;

import java.awt.Dimension;
import java.awt.Graphics2D;

import de.amr.games.pacman.controller.PacManGameController;
import de.amr.games.pacman.controller.PacManGameState;
import de.amr.games.pacman.ui.sound.SoundManager;
import de.amr.games.pacman.ui.swing.rendering.PacManGameRendering2D;

/**
 * Common game scene base class.
 * 
 * @author Armin Reichert
 */
public abstract class GameScene {

	protected final PacManGameController controller;
	protected final Dimension size;
	protected final PacManGameRendering2D rendering;
	protected final SoundManager sounds;

	public GameScene(PacManGameController controller, Dimension size, PacManGameRendering2D rendering,
			SoundManager sounds) {
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

	public void onGameStateChange(PacManGameState oldState, PacManGameState newState) {
	}

	public abstract void render(Graphics2D g);

}