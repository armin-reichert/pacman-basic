package de.amr.games.pacman.ui.swing.pacman.scene;

import static de.amr.games.pacman.heaven.God.clock;
import static de.amr.games.pacman.lib.Logging.log;

import java.awt.Graphics2D;

import de.amr.games.pacman.lib.V2i;
import de.amr.games.pacman.model.AbstractPacManGame;
import de.amr.games.pacman.ui.api.PacManGameScene;
import de.amr.games.pacman.ui.sound.PacManGameSound;
import de.amr.games.pacman.ui.sound.SoundManager;
import de.amr.games.pacman.ui.swing.pacman.rendering.PacManGameRendering;

/**
 * Scene where the game is played.
 * 
 * @author Armin Reichert
 */
public class PacManGameIntermission3 implements PacManGameScene {

	private final V2i size;
	private final PacManGameRendering rendering;
	private final SoundManager soundManager;
	private final AbstractPacManGame game;

	public PacManGameIntermission3(V2i size, PacManGameRendering rendering, SoundManager soundManager,
			AbstractPacManGame game) {
		this.size = size;
		this.rendering = rendering;
		this.soundManager = soundManager;
		this.game = game;
	}

	@Override
	public V2i sizeInPixel() {
		return size;
	}

	@Override
	public void draw(Graphics2D g) {
	}

	@Override
	public void start() {
		log("Start intermission scene %s", getClass().getSimpleName());
		soundManager.playSound(PacManGameSound.INTERMISSION_3);
	}

	@Override
	public void end() {
		log("End intermission scene %s", getClass().getSimpleName());
	}

	@Override
	public void update() {
		if (game.state.ticksRun() == clock.sec(7)) {
			game.state.duration(0);
		}
	}
}