package de.amr.games.pacman.ui.swing.mspacman;

import static de.amr.games.pacman.game.heaven.God.clock;
import static de.amr.games.pacman.game.worlds.PacManGameWorld.t;
import static de.amr.games.pacman.lib.Direction.UP;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.Arrays;

import de.amr.games.pacman.game.core.PacManGameModel;
import de.amr.games.pacman.lib.Direction;
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

	private BufferedImage pacWalking(Direction dir, boolean animated) {
		if (animated) {
			int frame = clock.frame(5, 3);
			return assets.section(frame, assets.dirIndex(dir));
		} else {
			return assets.section(1, assets.dirIndex(dir));
		}
	}

	private final long animationStart = 60;
	private final V2i frameTopLeftTile = new V2i(6, 8);
	private final V2i frameSize = new V2i(32, 16);
	private final float speed = 0.5f;
	private final int ghostTargetX = t(frameTopLeftTile.x) - 18;
	private int[] ghostX = new int[4];
	private int[] ghostY = new int[4];
	private Direction[] ghostDir = new Direction[4];
	private boolean[] ghostWalking = new boolean[4];
	private boolean[] ghostReachedTarget = new boolean[4];
	private boolean pacWalking;
	private boolean pacReachedTarget;
	private int pacX, pacY;

	@Override
	public void start() {
		Arrays.fill(ghostX, size.x);
		Arrays.fill(ghostY, t(frameTopLeftTile.y) + 4 * (frameSize.y + 1));
		Arrays.fill(ghostWalking, false);
		Arrays.fill(ghostDir, Direction.LEFT);
		Arrays.fill(ghostReachedTarget, false);
		pacWalking = false;
		pacReachedTarget = false;
		pacX = size.x;
		pacY = t(frameTopLeftTile.y) + 4 * (frameSize.y + 1);
		game.state.resetTimer();
	}

	@Override
	public void end() {
		for (int ghostID = 0; ghostID < 4; ++ghostID) {
			for (Direction dir : Direction.values()) {
				assets.ghostsWalking.get(ghostID).get(dir).reset();
			}
		}
		for (Direction dir : Direction.values()) {
			assets.pacWalking.get(dir).reset();
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
			ghostWalking[0] = true;
			ui.sounds().ifPresent(sm -> sm.playSound(PacManGameSound.CREDIT));
		}

		g.setFont(assets.scoreFont);
		g.setColor(Color.ORANGE);
		drawCenteredText(g, "\"MS PAC-MAN\"", t(5));
		drawFrame(g, clock.frame(2, frameSize.x / 2));

		for (int ghost = 0; ghost <= 3; ++ghost) {
			if (ghostReachedTarget[ghost]) {
				assets.ghostsWalking.get(ghost).get(UP).stop();
				g.drawImage(assets.ghostsWalking.get(ghost).get(UP).frame(), ghostTargetX, t(frameTopLeftTile.y) + 16 * ghost,
						null);
				continue;
			}
			if (ghostWalking[ghost]) {
				g.setFont(assets.scoreFont);
				// display text
				if (ghost == 0) {
					g.setColor(Color.WHITE);
					g.drawString(ui.translation("WITH"), t(8), t(11));
				}
				g.setColor(GHOST_COLORS[ghost]);
				drawCenteredText(g, ui.translation("MSPACMAN.GHOST." + ghost + ".NICKNAME"), t(14));

				// walk
				if (ghostDir[ghost] == Direction.LEFT) {
					ghostX[ghost] -= speed;
				} else if (ghostDir[ghost] == Direction.UP) {
					ghostY[ghost] -= speed;
				}
				if (ghostX[ghost] == ghostTargetX) {
					ghostDir[ghost] = Direction.UP;
				}
				g.drawImage(assets.ghostsWalking.get(ghost).get(ghostDir[ghost]).frame(), ghostX[ghost], ghostY[ghost], null);

				// target reached?
				if (ghostY[ghost] <= t(frameTopLeftTile.y) + ghost * 16) {
					ghostReachedTarget[ghost] = true;
					ghostWalking[ghost] = false;
					assets.ghostsWalking.get(ghost).get(ghostDir[ghost]).stop();
					assets.ghostsWalking.get(ghost).get(ghostDir[ghost]).reset();
					if (ghost < 3) {
						ghostWalking[ghost + 1] = true;
						ui.sounds().ifPresent(sm -> sm.playSound(PacManGameSound.CREDIT));
					} else {
						pacWalking = true;
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

		if (pacX <= t(13)) {
			pacWalking = false;
			pacReachedTarget = true;
		}

		if (pacWalking) {
			pacX -= speed;
			g.drawImage(pacWalking(Direction.LEFT, true), pacX, pacY, null);
		} else if (pacReachedTarget) {
			g.drawImage(pacWalking(Direction.LEFT, false), pacX, pacY, null);
		}

		if (pacReachedTarget) {
			drawPointsAnimation(g, 26);
			drawPressKeyToStart(g);
			ui.sounds().ifPresent(sm -> sm.stopAllSounds());
		}

		// restart intro after 30 seconds
		if (animationTime == clock.sec(30)) {
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