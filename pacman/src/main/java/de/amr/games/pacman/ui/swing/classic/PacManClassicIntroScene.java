package de.amr.games.pacman.ui.swing.classic;

import static de.amr.games.pacman.heaven.God.clock;
import static de.amr.games.pacman.lib.Direction.LEFT;
import static de.amr.games.pacman.lib.Direction.RIGHT;
import static de.amr.games.pacman.world.PacManGameWorld.t;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.Optional;
import java.util.stream.IntStream;

import de.amr.games.pacman.lib.Direction;
import de.amr.games.pacman.lib.V2f;
import de.amr.games.pacman.lib.V2i;
import de.amr.games.pacman.model.PacManGame;
import de.amr.games.pacman.ui.api.PacManGameAnimations;
import de.amr.games.pacman.ui.api.PacManGameScene;
import de.amr.games.pacman.ui.api.Sound;
import de.amr.games.pacman.ui.swing.PacManGameSwingUI;

/**
 * Intro presenting the ghosts and showing the chasing animations.
 * 
 * @author Armin Reichert
 */
public class PacManClassicIntroScene implements PacManGameScene {

	private static final Color[] GHOST_COLORS = { Color.RED, Color.PINK, Color.CYAN, Color.ORANGE };

	private final PacManGameSwingUI ui;
	private final V2i size;
	private final PacManClassicAssets assets;
	private final PacManGame game;
	private int lastKilledGhostID;

	public PacManClassicIntroScene(PacManGameSwingUI ui, V2i size, PacManClassicAssets assets, PacManGame game) {
		this.ui = ui;
		this.size = size;
		this.assets = assets;
		this.game = game;
	}

	@Override
	public Optional<PacManGameAnimations> animations() {
		return Optional.of(assets);
	}

	@Override
	public V2i size() {
		return size;
	}

	@Override
	public void start() {
		game.pac.position = new V2f(size.x, t(22));
		game.pac.dir = LEFT;
		game.pac.speed = 0.8f;
		game.ghosts[0].position = new V2f(game.pac.position.x + 24, t(22));
		game.ghosts[0].dir = LEFT;
		game.ghosts[0].speed = 0.8f;
		lastKilledGhostID = -1;
	}

	@Override
	public void end() {
		for (int ghostID = 0; ghostID < 4; ++ghostID) {
			for (Direction dir : Direction.values()) {
				assets.ghostWalking.get(ghostID).get(dir).restart();
			}
		}
		for (Direction dir : Direction.values()) {
			assets.pacMunching.get(dir).restart();
		}
		game.state.resetTimer();
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
				ui.sounds().ifPresent(sm -> sm.playSound(Sound.CREDIT));
			});
			game.state.runAfter(clock.sec(ghostStart), () -> {
				g.drawImage(assets.ghostWalking.get(ghost).get(RIGHT).thing(0), t(2) - 3, y - 2, null);
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
			ui.sounds().ifPresent(sm -> sm.loopSound(Sound.GHOST_SIREN_1));
		});

		game.state.runAfter(clock.sec(13), () -> {
			if (game.pac.dir == LEFT) {
				drawGhostsChasingPacMan(g);
			} else {
				drawPacManChasingGhosts(g);
			}
		});

		game.state.runAt(clock.sec(24), () -> {
			ui.sounds().ifPresent(sm -> sm.stopSound(Sound.PACMAN_POWER));
		});

		game.state.runAfter(clock.sec(24), () -> {
			drawPressKeyToStart(g);
		});

		game.state.runAt(clock.sec(30), () -> {
			end();
			start();
		});
	}

	private void drawPressKeyToStart(Graphics2D g) {
		g.setColor(Color.ORANGE);
		g.setFont(assets.scoreFont);
		if (game.state.ticksRun() % 40 < 20) {
			drawCenteredText(g, ui.translation("PRESS_KEY_TO_PLAY"), size.y - 20);
		}
	}

	private void drawPointsAnimation(Graphics2D g) {
		g.setColor(Color.PINK);
		g.fillRect(t(9) + 6, t(27) + 2, 2, 2);
		if (game.state.ticksRun() % 40 < 20) {
			g.fillOval(t(9), t(29) - 2, 10, 10);
		}
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
		if (game.state.ticksRun() % 40 < 20) {
			g.setColor(Color.PINK);
			g.fillOval(t(2), t(22) + 2, 10, 10);
		}
		g.drawImage(pacSprite(), (int) game.pac.position.x, (int) game.pac.position.y, null);
		for (int ghostID = 0; ghostID < 4; ++ghostID) {
			g.drawImage(assets.ghostWalking.get(ghostID).get(LEFT).currentFrameThenAdvance(),
					(int) game.ghosts[0].position.x + 16 * ghostID, (int) game.pac.position.y, null);
		}
		if (game.pac.position.x > t(2)) {
			V2f velocity = new V2f(game.pac.dir.vec).scaled(game.pac.speed);
			game.pac.position = game.pac.position.sum(velocity);
			V2f ghostVelocity = new V2f(game.ghosts[0].dir.vec).scaled(game.ghosts[0].speed);
			game.ghosts[0].position = game.ghosts[0].position.sum(ghostVelocity);
		} else {
			game.pac.dir = RIGHT;
			game.ghosts[0].dir = RIGHT;
			game.ghosts[0].speed = 0.4f;
			ui.sounds().ifPresent(sm -> sm.stopSound(Sound.GHOST_SIREN_1));
			ui.sounds().ifPresent(sm -> sm.loopSound(Sound.PACMAN_POWER));
		}
	}

	private void drawPacManChasingGhosts(Graphics2D g) {
		int y = t(22);
		for (int ghostID = 0; ghostID < 4; ++ghostID) {
			int x = (int) game.ghosts[0].position.x + 16 * ghostID;
			if (game.pac.position.x < x) {
				g.drawImage(assets.ghostBlue.currentFrameThenAdvance(), x, y, null);
			} else if (game.pac.position.x > x && game.pac.position.x <= x + 16) {
				int bounty = (int) (Math.pow(2, ghostID) * 200);
				g.drawImage(assets.numbers.get(bounty), x, y, null);
				if (lastKilledGhostID != ghostID) {
					lastKilledGhostID++;
					ui.sounds().ifPresent(sm -> sm.playSound(Sound.GHOST_EATEN));
				}
			}
		}
		g.drawImage(pacSprite(), (int) game.pac.position.x, (int) game.pac.position.y, null);
		if (game.pac.position.x < size.x) {
			V2f velocity = new V2f(game.pac.dir.vec).scaled(game.pac.speed);
			game.pac.position = game.pac.position.sum(velocity);
			V2f ghostVelocity = new V2f(game.ghosts[0].dir.vec).scaled(game.ghosts[0].speed);
			game.ghosts[0].position = game.ghosts[0].position.sum(ghostVelocity);
		}
	}

	private BufferedImage pacSprite() {
		return game.pac.speed != 0 ? assets.pacMunching.get(game.pac.dir).currentFrameThenAdvance()
				: assets.pacMouthOpen.get(game.pac.dir);
	}

}