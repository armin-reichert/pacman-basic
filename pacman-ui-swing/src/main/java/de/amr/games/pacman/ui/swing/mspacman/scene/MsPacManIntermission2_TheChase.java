package de.amr.games.pacman.ui.swing.mspacman.scene;

import java.awt.Graphics2D;

import de.amr.games.pacman.heaven.God;
import de.amr.games.pacman.lib.V2i;
import de.amr.games.pacman.model.MsPacManGame;
import de.amr.games.pacman.ui.api.PacManGameScene;
import de.amr.games.pacman.ui.sound.PacManGameSound;
import de.amr.games.pacman.ui.sound.SoundManager;
import de.amr.games.pacman.ui.swing.mspacman.rendering.MsPacManGameRendering;

public class MsPacManIntermission2_TheChase implements PacManGameScene {

	private final V2i size;
	private final MsPacManGameRendering rendering;
	private final SoundManager soundManager;
	private final MsPacManGame game;

	public MsPacManIntermission2_TheChase(V2i size, MsPacManGameRendering rendering, SoundManager soundManager,
			MsPacManGame game) {
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
		soundManager.loopSound(PacManGameSound.INTERMISSION_2, 1);
	}

	@Override
	public void update() {
		if (game.state.ticksRun() == God.clock.sec(23)) {
			game.state.duration(0);
		}
	}

	@Override
	public void draw(Graphics2D g) {

	}
}
