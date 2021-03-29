package de.amr.games.pacman.ui.swing.scenes.common;

import java.awt.Dimension;
import java.awt.Graphics2D;

import de.amr.games.pacman.controller.PacManGameController;
import de.amr.games.pacman.controller.PacManGameState;
import de.amr.games.pacman.controller.event.PacManGameEvent;
import de.amr.games.pacman.ui.swing.assets.SoundManager;
import de.amr.games.pacman.ui.swing.rendering.CommonPacManGameRendering;

/**
 * Common game scene base class.
 * 
 * @author Armin Reichert
 */
public abstract class GameScene {

	protected final PacManGameController gameController;
	protected final Dimension size;
	protected final CommonPacManGameRendering rendering;
	protected final SoundManager sounds;

	public GameScene(PacManGameController controller, Dimension size, CommonPacManGameRendering rendering, SoundManager sounds) {
		this.gameController = controller;
		this.size = size;
		this.rendering = rendering;
		this.sounds = sounds;
	}

	public Dimension size() {
		return size;
	}

	public void start() {
		gameController.addGameEventListener(this::onGameEvent);
	}

	public abstract void update();

	public void end() {
		gameController.removeGameEventListener(this::onGameEvent);
	}

	protected void onGameEvent(PacManGameEvent gameEvent) {
	}

	public void onGameStateChange(PacManGameState oldState, PacManGameState newState) {
	}

	public abstract void render(Graphics2D g);

}