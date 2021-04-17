package de.amr.games.pacman.ui.swing.scenes.common;

import java.awt.Dimension;
import java.awt.Graphics2D;

import de.amr.games.pacman.controller.PacManGameController;
import de.amr.games.pacman.controller.event.PacManGameEvent;
import de.amr.games.pacman.controller.event.PacManGameEventListener;
import de.amr.games.pacman.model.common.AbstractGameModel;
import de.amr.games.pacman.ui.swing.assets.SoundManager;
import de.amr.games.pacman.ui.swing.rendering.common.AbstractPacManGameRendering;

/**
 * Common game scene base class.
 * 
 * @author Armin Reichert
 */
public abstract class GameScene implements PacManGameEventListener {

	protected final PacManGameController gameController;
	protected final Dimension size;
	protected final AbstractPacManGameRendering rendering;
	protected final SoundManager sounds;

	public GameScene(PacManGameController controller, Dimension size, AbstractPacManGameRendering rendering,
			SoundManager sounds) {
		this.gameController = controller;
		this.size = size;
		this.rendering = rendering;
		this.sounds = sounds;
	}

	public AbstractGameModel game() {
		return gameController.game();
	}

	public Dimension size() {
		return size;
	}

	public abstract void init();

	public abstract void update();

	public abstract void end();

	@Override
	public void onGameEvent(PacManGameEvent event) {
	}

	public abstract void render(Graphics2D g);

}