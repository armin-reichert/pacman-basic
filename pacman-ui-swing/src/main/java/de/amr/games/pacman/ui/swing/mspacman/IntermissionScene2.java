package de.amr.games.pacman.ui.swing.mspacman;

import static de.amr.games.pacman.heaven.God.clock;
import static de.amr.games.pacman.ui.swing.mspacman.MsPacManGameRendering.assets;
import static de.amr.games.pacman.ui.swing.mspacman.MsPacManGameScenes.rendering;
import static de.amr.games.pacman.ui.swing.mspacman.MsPacManGameScenes.soundManager;
import static de.amr.games.pacman.world.PacManGameWorld.t;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;

import de.amr.games.pacman.lib.CountdownTimer;
import de.amr.games.pacman.model.PacManGameModel;
import de.amr.games.pacman.sound.PacManGameSound;
import de.amr.games.pacman.ui.swing.GameScene;

/**
 * Intermission scene 2: "The chase".
 * 
 * @author Armin Reichert
 */
public class IntermissionScene2 implements GameScene {

	enum Phase {

		FLAP, READY_TO_PLAY;

		final CountdownTimer timer = new CountdownTimer();
	}

	private final Dimension size;
	private final PacManGameModel game;

	private Phase phase;

	private void enter(Phase newPhase, long ticks) {
		phase = newPhase;
		phase.timer.setDuration(ticks);
	}

	public IntermissionScene2(Dimension size, PacManGameModel game) {
		this.size = size;
		this.game = game;
	}

	@Override
	public Dimension sizeInPixel() {
		return size;
	}

	@Override
	public void start() {
		soundManager.loop(PacManGameSound.INTERMISSION_2, 1);
		assets.flapAnim.restart();

		enter(Phase.FLAP, clock.sec(30));
	}

	@Override
	public void update() {

		switch (phase) {

		case FLAP:
			if (phase.timer.expired()) {
				enter(Phase.READY_TO_PLAY, clock.sec(5));
			}
			break;

		case READY_TO_PLAY:
			if (phase.timer.expired()) {
				game.state.duration(0);
			}
			break;

		default:
			break;
		}
		phase.timer.tick();
	}

	@Override
	public void render(Graphics2D g) {
		if (phase == Phase.FLAP) {
			drawFlapAnimation(g, t(3), t(10));
		}
	}

	private void drawFlapAnimation(Graphics2D g, int flapX, int flapY) {
		rendering.drawImage(g, assets.flapAnim.animate(), flapX, flapY, true);
		g.setColor(new Color(222, 222, 225));
		g.setFont(assets.getScoreFont());
		g.drawString("2", flapX + 20, flapY + 30);
		if (assets.flapAnim.isRunning()) {
			g.drawString("THE CHASE", flapX + 40, flapY + 20);
		}
	}
}
