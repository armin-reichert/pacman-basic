package de.amr.games.pacman.ui.swing.classic;

import static de.amr.games.pacman.game.heaven.God.clock;
import static de.amr.games.pacman.game.worlds.PacManGameWorld.t;
import static de.amr.games.pacman.lib.Direction.LEFT;
import static de.amr.games.pacman.lib.Direction.RIGHT;
import static de.amr.games.pacman.ui.swing.classic.PacManClassicAssets.DIR;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.stream.IntStream;

import de.amr.games.pacman.game.core.PacManGameModel;
import de.amr.games.pacman.lib.Direction;
import de.amr.games.pacman.lib.V2i;
import de.amr.games.pacman.ui.api.PacManGameSound;
import de.amr.games.pacman.ui.swing.PacManGameSwingUI;
import de.amr.games.pacman.ui.swing.scene.PacManGameScene;

/**
 * Intro presenting the ghosts and showing the chasing animations.
 * 
 * @author Armin Reichert
 */
public class PacManClassicIntroScene implements PacManGameScene {

	private static final Color[] GHOST_COLORS = { Color.RED, Color.PINK, Color.CYAN, Color.ORANGE };

	private final PacManGameSwingUI ui;
	private final PacManGameModel game;
	private final V2i size;
	private final PacManClassicAssets assets;

	private float pacManX;
	private float leftmostGhostX;
	private int lastKilledGhost;
	private boolean ghostsChasingPacMan;

	public PacManClassicIntroScene(PacManGameSwingUI ui, PacManGameModel game, V2i size, PacManClassicAssets assets) {
		this.ui = ui;
		this.game = game;
		this.size = size;
		this.assets = assets;
	}

	@Override
	public V2i size() {
		return size;
	}

	@Override
	public void start() {
		game.state.resetTimer();
		pacManX = size.x;
		leftmostGhostX = pacManX + 24;
		lastKilledGhost = -1;
		ghostsChasingPacMan = true;
	}

	@Override
	public void end() {
	}

	@Override
	public void draw(Graphics2D g) {
		game.state.runAfter(clock.sec(1), () -> {
			drawCenteredImage(g, assets.gameLogo, 3);
		});

		game.state.runAfter(clock.sec(2), () -> {
			g.setColor(Color.WHITE);
			g.setFont(assets.scoreFont);
			drawCenteredText(g, ui.translation("CHARACTER_NICKNAME"), t(8));
		});

		IntStream.rangeClosed(0, 3).forEach(ghost -> {
			int ghostStart = 3 + 2 * ghost;
			int y = t(10 + 3 * ghost);
			game.state.runAt(clock.sec(ghostStart), () -> {
				ui.sounds().ifPresent(sm -> sm.playSound(PacManGameSound.CREDIT));
			});
			game.state.runAfter(clock.sec(ghostStart), () -> {
				g.drawImage(assets.section(0, 4 + ghost), t(2) - 3, y - 2, null);
			});
			game.state.runAfter(clock.sec(ghostStart + 0.5), () -> {
				drawGhostCharacterAndName(g, ghost, y, false);
			});
			game.state.runAfter(clock.sec(ghostStart + 1), () -> {
				drawGhostCharacterAndName(g, ghost, y, true);
			});
		});

		game.state.runAfter(clock.sec(12), () -> {
			drawPointsAnimation(g);
		});

		game.state.runAt(clock.sec(13), () -> {
			ui.sounds().ifPresent(sm -> sm.loopSound(PacManGameSound.GHOST_SIREN_1));
		});

		game.state.runAfter(clock.sec(13), () -> {
			if (ghostsChasingPacMan) {
				drawGhostsChasingPacMan(g);
			} else {
				drawPacManChasingGhosts(g);
			}
		});

		game.state.runAt(clock.sec(24), () -> {
			ui.sounds().ifPresent(sm -> sm.stopSound(PacManGameSound.PACMAN_POWER));
		});

		game.state.runAfter(clock.sec(24), () -> {
			drawPressKeyToStart(g);
		});

		game.state.runAt(clock.sec(30), this::start);
	}

	private void drawPressKeyToStart(Graphics2D g) {
		g.setColor(Color.ORANGE);
		g.setFont(assets.scoreFont);
		clock.runOrBeIdle(20, () -> {
			drawCenteredText(g, ui.translation("PRESS_KEY_TO_PLAY"), size.y - 20);
		});
	}

	private void drawPointsAnimation(Graphics2D g) {
		g.setColor(Color.PINK);
		g.fillRect(t(9) + 6, t(27) + 2, 2, 2);
		clock.runOrBeIdle(20, () -> {
			g.fillOval(t(9), t(29) - 2, 10, 10);
		});
		g.setColor(Color.WHITE);
		g.setFont(assets.scoreFont);
		g.drawString("10", t(12), t(28));
		g.drawString("50", t(12), t(30));
		g.setFont(assets.scoreFont.deriveFont(6f));
		g.drawString(ui.translation("POINTS"), t(15), t(28));
		g.drawString(ui.translation("POINTS"), t(15), t(30));
	}

	private void drawGhostCharacterAndName(Graphics2D g, int ghostID, int y, boolean both) {
		String character = ui.translation("CLASSIC.GHOST." + ghostID + ".CHARACTER");
		String nickname = "\"" + ui.translation("CLASSIC.GHOST." + ghostID + ".NICKNAME") + "\"";
		Color color = GHOST_COLORS[ghostID];
		g.setColor(color);
		g.setFont(assets.scoreFont);
		g.drawString("-" + character, t(4), y + 11);
		if (both) {
			g.drawString(nickname, t(15), y + 11);
		}
	}

	private void drawGhostsChasingPacMan(Graphics2D g) {
		int y = t(22);
		clock.runOrBeIdle(20, () -> {
			g.setColor(Color.PINK);
			g.fillOval(t(2), y + 2, 10, 10);
		});
		g.drawImage(pacManWalkingSprite(LEFT), (int) pacManX, y, null);
		for (int ghost = 0; ghost < 4; ++ghost) {
			g.drawImage(ghostWalkingSprite(LEFT, ghost), (int) leftmostGhostX + 16 * ghost, y, null);
		}
		if (pacManX > t(2)) {
			pacManX -= 0.8f;
			leftmostGhostX -= 0.8f;
		} else {
			ghostsChasingPacMan = false;
			ui.sounds().ifPresent(sm -> sm.stopSound(PacManGameSound.GHOST_SIREN_1));
			ui.sounds().ifPresent(sm -> sm.loopSound(PacManGameSound.PACMAN_POWER));
		}
	}

	private void drawPacManChasingGhosts(Graphics2D g) {
		int y = t(22);
		BufferedImage frightenedGhost = ghostFrightenedSprite();
		for (int ghost = 0; ghost < 4; ++ghost) {
			int x = (int) leftmostGhostX + ghost * 16;
			if (pacManX < x) {
				g.drawImage(frightenedGhost, x, y, null);
			} else if (pacManX > x && pacManX <= x + 16) {
				int bounty = (int) (Math.pow(2, ghost) * 200);
				g.drawImage(assets.numbers.get(bounty), x, y, null);
				if (lastKilledGhost != ghost) {
					lastKilledGhost++;
					ui.sounds().ifPresent(sm -> sm.playSound(PacManGameSound.GHOST_EATEN));
				}
			}
		}
		g.drawImage(pacManWalkingSprite(RIGHT), (int) pacManX, y, null);
		if (pacManX < size.x) {
			pacManX += 0.6f;
			leftmostGhostX += 0.3f;
		}
	}

	private BufferedImage pacManWalkingSprite(Direction dir) {
		int frame = clock.frame(5, 3);
		return frame == 2 ? assets.section(frame, 0) : assets.section(frame, DIR.get(dir));
	}

	private BufferedImage ghostWalkingSprite(Direction dir, int ghost) {
		int frame = clock.frame(5, 2);
		return assets.section(2 * DIR.get(dir) + frame, 4 + ghost);
	}

	private BufferedImage ghostFrightenedSprite() {
		int frame = clock.frame(5, 2);
		return assets.section(8 + frame, 4);
	}
}