package de.amr.games.pacman.ui.swing.mspacman;

import java.awt.Graphics2D;

import de.amr.games.pacman.heaven.God;
import de.amr.games.pacman.lib.V2i;
import de.amr.games.pacman.model.PacManGameModel;
import de.amr.games.pacman.sound.PacManGameSound;
import de.amr.games.pacman.sound.SoundManager;
import de.amr.games.pacman.ui.swing.PacManGameScene;

public class MsPacManIntermission1_TheyMeet implements PacManGameScene {

	private final V2i size;
	private final MsPacManGameSpriteBasedRendering rendering;
	private final SoundManager soundManager;
	private final PacManGameModel game;

	public MsPacManIntermission1_TheyMeet(V2i size, MsPacManGameSpriteBasedRendering rendering, SoundManager soundManager,
			PacManGameModel game) {
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
	public void start() {
		soundManager.loop(PacManGameSound.INTERMISSION_1, 1);
	}

	@Override
	public void update() {
		if (game.state.ticksRun() == God.clock.sec(10)) {
			game.state.duration(0);
		}
	}

	@Override
	public void draw(Graphics2D g) {

	}
}
