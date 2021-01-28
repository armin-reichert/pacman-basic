package de.amr.games.pacman.ui.swing.mspacman;

import static de.amr.games.pacman.game.heaven.God.clock;
import static de.amr.games.pacman.game.worlds.PacManGameWorld.t;
import static de.amr.games.pacman.lib.Direction.LEFT;
import static de.amr.games.pacman.lib.Direction.UP;

import java.awt.Color;
import java.awt.Graphics2D;
import java.util.Arrays;

import de.amr.games.pacman.game.core.PacManGameModel;
import de.amr.games.pacman.game.creatures.Ghost;
import de.amr.games.pacman.lib.Direction;
import de.amr.games.pacman.lib.V2f;
import de.amr.games.pacman.lib.V2i;
import de.amr.games.pacman.ui.api.PacManGameSound;
import de.amr.games.pacman.ui.swing.PacManGameSwingUI;
import de.amr.games.pacman.ui.swing.scene.PacManGameScene;

public class MsPacManIntroScene implements PacManGameScene {

	private static final Color[] GHOST_COLORS = { Color.RED, Color.PINK, Color.CYAN, Color.ORANGE };

	private final PacManGameSwingUI ui;
	private final PacManGameModel game;
	private final V2i size;
	private final MsPacManAssets assets;

	public MsPacManIntroScene(PacManGameSwingUI ui, PacManGameModel game, V2i size, MsPacManAssets assets) {
		this.ui = ui;
		this.game = game;
		this.size = size;
		this.assets = assets;
	}

	@Override
	public V2i size() {
		return size;
	}

	private final long animationStart = 60;
	private final V2i frameTopLeftTile = new V2i(6, 8);
	private final V2i frameSize = new V2i(32, 16);
	private final int ghostTargetX = t(frameTopLeftTile.x) - 18;
	private boolean[] ghostReachedTarget = new boolean[4];
	private boolean pacReachedTarget;

	@Override
	public void start() {
		Arrays.fill(ghostReachedTarget, false);
		for (Ghost ghost : game.ghosts) {
			ghost.position = new V2f(size.x, t(frameTopLeftTile.y) + 4 * (frameSize.y + 1));
			ghost.speed = 0;
			ghost.dir = LEFT;
		}

		pacReachedTarget = false;
		game.pac.position = new V2f(size.x, t(frameTopLeftTile.y) + 4 * (frameSize.y + 1));
		game.pac.speed = 0;
		game.pac.dir = LEFT;

		game.state.resetTimer();
	}

	@Override
	public void end() {
		for (int ghostID = 0; ghostID < 4; ++ghostID) {
			for (Direction dir : Direction.values()) {
				assets.ghostsWalking.get(ghostID).get(dir).reset();
				assets.ghostsWalking.get(ghostID).get(dir).start();
			}
		}
		for (Direction dir : Direction.values()) {
			assets.pacWalking.get(dir).reset();
			assets.pacWalking.get(dir).start();
		}
	}

	@Override
	public void draw(Graphics2D g) {
		if (game.state.running < animationStart) {
			return;
		}
		// run animation
		long animationTime = game.state.running - animationStart;
		if (animationTime == animationStart) {
			game.ghosts[0].speed = 0.75f;
			ui.sounds().ifPresent(sm -> sm.playSound(PacManGameSound.CREDIT));
		}

		g.setFont(assets.scoreFont);
		g.setColor(Color.ORANGE);
		drawCenteredText(g, "\"MS PAC-MAN\"", t(5));
		drawFrame(g, clock.frame(2, frameSize.x / 2));

		for (Ghost ghost : game.ghosts) {
			if (ghostReachedTarget[ghost.id]) {
				assets.ghostsWalking.get(ghost.id).get(ghost.dir).stop();
				g.drawImage(assets.ghostsWalking.get(ghost.id).get(ghost.dir).frame(), ghostTargetX,
						t(frameTopLeftTile.y) + 16 * ghost.id, null);
				continue;
			}
			if (ghost.speed != 0) {
				g.setFont(assets.scoreFont);
				// display text
				if (ghost.id == 0) {
					g.setColor(Color.WHITE);
					g.drawString(ui.translation("WITH"), t(8), t(11));
				}
				g.setColor(GHOST_COLORS[ghost.id]);
				drawCenteredText(g, ui.translation("MSPACMAN.GHOST." + ghost.id + ".NICKNAME"), t(14));

				// walk
				V2f velocity = new V2f(ghost.dir.vec).scaled(ghost.speed);
				ghost.position = ghost.position.sum(velocity);
				if (ghost.position.x <= ghostTargetX) {
					ghost.dir = UP;
				}
				g.drawImage(assets.ghostsWalking.get(ghost.id).get(ghost.dir).frame(), (int) ghost.position.x,
						(int) ghost.position.y, null);

				// target reached?
				if (ghost.position.y <= t(frameTopLeftTile.y) + ghost.id * 16) {
					ghostReachedTarget[ghost.id] = true;
					ghost.speed = 0;
					if (ghost.id < 3) {
						game.ghosts[ghost.id + 1].speed = 0.75f;
						ui.sounds().ifPresent(sm -> sm.playSound(PacManGameSound.CREDIT));
					} else {
						game.pac.speed = 0.75f;
						ui.sounds().ifPresent(sm -> sm.loopSound(PacManGameSound.PACMAN_MUNCH));
					}
				}
			}
		}

		if (ghostReachedTarget[3]) {
			g.setColor(Color.WHITE);
			g.drawString(ui.translation("STARRING"), t(8), t(11));
			g.setColor(Color.YELLOW);
			g.drawString("MS PAC-MAN", t(11), t(14));
		}

		if (game.pac.position.x <= t(13)) {
			game.pac.speed = 0;
			pacReachedTarget = true;
		}

		if (game.pac.speed != 0) {
			V2f velocity = new V2f(game.pac.dir.vec).scaled(game.pac.speed);
			game.pac.position = game.pac.position.sum(velocity);
		} else if (pacReachedTarget) {
			assets.pacWalking.get(game.pac.dir).stop();
		}
		g.drawImage(assets.pacWalking.get(game.pac.dir).frame(), (int) game.pac.position.x, (int) game.pac.position.y,
				null);

		if (pacReachedTarget) {
			drawPointsAnimation(g, 26);
			drawPressKeyToStart(g);
			ui.sounds().ifPresent(sm -> sm.stopAllSounds());
		}

		// restart intro after 30 seconds
		if (animationTime == clock.sec(30)) {
			end();
			start();
		}
	}

	private void drawFrame(Graphics2D g, int light) {
		for (int dot = 0; dot < 2 * (frameSize.x + frameSize.y); ++dot) {
			int x = 0, y = 0;
			if (dot <= frameSize.x) {
				x = dot;
			} else if (dot < frameSize.x + frameSize.y) {
				x = frameSize.x;
				y = dot - frameSize.x;
			} else if (dot < 2 * frameSize.x + frameSize.y + 1) {
				x = 2 * frameSize.x + frameSize.y - dot;
				y = frameSize.y;
			} else {
				y = 2 * (frameSize.x + frameSize.y) - dot;
			}
			g.setColor((dot + light) % (frameSize.x / 2) == 0 ? Color.CYAN : Color.RED);
			g.fillRect(t(frameTopLeftTile.x) + 4 * x, t(frameTopLeftTile.y) + 4 * y, 2, 2);
		}
	}

	private void drawPressKeyToStart(Graphics2D g) {
		g.setColor(Color.ORANGE);
		g.setFont(assets.scoreFont);
		clock.runOrBeIdle(20, () -> {
			drawCenteredText(g, ui.translation("PRESS_KEY_TO_PLAY"), size.y - 20);
		});
	}

	private void drawPointsAnimation(Graphics2D g, int yTile) {
		g.setColor(Color.PINK);
		g.fillRect(t(9) + 6, t(yTile - 1) + 2, 2, 2);
		clock.runOrBeIdle(20, () -> {
			g.fillOval(t(9), t(yTile + 1) - 2, 10, 10);
		});
		g.setColor(Color.WHITE);
		g.setFont(assets.scoreFont);
		g.drawString("10", t(12), t(yTile));
		g.drawString("50", t(12), t(yTile + 2));
		g.setFont(assets.scoreFont.deriveFont(6f));
		g.drawString(ui.translation("POINTS"), t(15), t(yTile));
		g.drawString(ui.translation("POINTS"), t(15), t(yTile + 2));
	}

}