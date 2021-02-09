package de.amr.games.pacman.ui.swing.mspacman.scene;

import java.awt.Graphics2D;

import de.amr.games.pacman.heaven.God;
import de.amr.games.pacman.lib.V2i;
import de.amr.games.pacman.model.PacManGameModel;
import de.amr.games.pacman.sound.PacManGameSound;
import de.amr.games.pacman.sound.SoundManager;
import de.amr.games.pacman.ui.swing.PacManGameScene;
import de.amr.games.pacman.ui.swing.mspacman.rendering.MsPacManGameSpriteBasedRendering;

public class MsPacManIntermission3_Junior implements PacManGameScene {

	private final V2i size;
	private final MsPacManGameSpriteBasedRendering rendering;
	private final SoundManager soundManager;
	private final PacManGameModel game;

	public MsPacManIntermission3_Junior(V2i size, MsPacManGameSpriteBasedRendering rendering, SoundManager soundManager,
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
		soundManager.loop(PacManGameSound.INTERMISSION_3, 1);
	}

	@Override
	public void update() {
		if (game.state.ticksRun() == God.clock.sec(6)) {
			game.state.duration(0);
		}
	}

	@Override
	public void draw(Graphics2D g) {
	}
}
