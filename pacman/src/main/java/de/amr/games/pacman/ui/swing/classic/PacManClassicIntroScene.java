package de.amr.games.pacman.ui.swing.classic;

import static de.amr.games.pacman.heaven.God.clock;
import static de.amr.games.pacman.heaven.God.differsAtMost;
import static de.amr.games.pacman.lib.Direction.LEFT;
import static de.amr.games.pacman.lib.Direction.RIGHT;
import static de.amr.games.pacman.model.creatures.GhostState.FRIGHTENED;
import static de.amr.games.pacman.world.PacManGameWorld.TS;
import static de.amr.games.pacman.world.PacManGameWorld.t;

import java.awt.Color;
import java.awt.Graphics2D;
import java.util.stream.IntStream;

import de.amr.games.pacman.lib.Animation;
import de.amr.games.pacman.lib.V2f;
import de.amr.games.pacman.lib.V2i;
import de.amr.games.pacman.model.PacManGame;
import de.amr.games.pacman.model.creatures.Ghost;
import de.amr.games.pacman.model.creatures.Pac;
import de.amr.games.pacman.ui.api.PacManGameScene;
import de.amr.games.pacman.ui.sound.PacManGameSound;

/**
 * Intro presenting the ghosts and showing the chasing animations.
 * 
 * @author Armin Reichert
 */
public class PacManClassicIntroScene implements PacManGameScene {

	private final V2i size;
	private final PacManClassicRendering rendering;
	private final PacManGame game;

	private final Animation<Boolean> blinking;
	private final int chaseY = t(23);
	private final int ghostGap = 18;
	private int lastKilledGhostID;
	private boolean chasingPac;

	public PacManClassicIntroScene(V2i size, PacManClassicRendering rendering, PacManGame game) {
		this.size = size;
		this.game = game;
		this.rendering = rendering;
		blinking = Animation.of(true, false).frameDuration(20).endless().run();
	}

	@Override
	public V2i size() {
		return size;
	}

	@Override
	public void start() {
		game.pac.visible = true;
		game.pac.position = new V2f(size.x, chaseY);
		game.pac.dir = LEFT;
		game.pac.speed = 0.8f;
		game.pac.couldMove = true;
		rendering.letPacMunch();
		game.ghosts().forEach(ghost -> {
			ghost.visible = true;
			ghost.position = new V2f(size.x + 24 + ghostGap * ghost.id, chaseY);
			ghost.wishDir = ghost.dir = LEFT;
			ghost.speed = 0.8f;
			ghost.couldMove = true;
			ghost.bounty = 0;
		});
		rendering.letGhostsFidget(game.ghosts(), true);
		chasingPac = true;
		lastKilledGhostID = -1;
	}

	@Override
	public void end() {
		game.state.resetTimer();
	}

	@Override
	public void update() {
		/* animation is done in draw() method */
	}

	@Override
	public void draw(Graphics2D g) {
		game.state.runAfter(clock.sec(1), () -> {
			drawHCenteredImage(g, rendering.assets.gameLogo, 3);
		});

		game.state.runAfter(clock.sec(2), () -> {
			g.setColor(Color.WHITE);
			g.setFont(rendering.assets.scoreFont);
			drawHCenteredText(g, rendering.translator.apply("CHARACTER_NICKNAME"), t(8));
		});

		// Introduce ghosts
		IntStream.range(0, 4).forEach(id -> {
			int ghostY = t(10) + t(3 * id);
			int ghostIntroStartSeconds = 3 + 2 * id;
			game.state.runAt(clock.sec(ghostIntroStartSeconds), () -> {
				rendering.soundManager.playSound(PacManGameSound.CREDIT);
			});
			game.state.runAfter(clock.sec(ghostIntroStartSeconds), () -> {
				introduceGhost(g, id, ghostY, false, false);
			});
			game.state.runAfter(clock.sec(ghostIntroStartSeconds + 0.5), () -> {
				introduceGhost(g, id, ghostY, true, false);
			});
			game.state.runAfter(clock.sec(ghostIntroStartSeconds + 1), () -> {
				introduceGhost(g, id, ghostY, true, true);
			});
		});

		game.state.runAfter(clock.sec(12), () -> {
			showPointsAnimation(g);
		});

		game.state.runAt(clock.sec(13), () -> {
			rendering.soundManager.loopSound(PacManGameSound.GHOST_SIREN_1);
		});

		game.state.runAfter(clock.sec(13), () -> {
			if (chasingPac) {
				showGhostsChasingPacMan(g, game.pac);
			} else {
				showPacManChasingGhosts(g, game.pac);
			}
		});

		game.state.runAt(clock.sec(24), () -> {
			rendering.soundManager.stopSound(PacManGameSound.PACMAN_POWER);
		});

		game.state.runAfter(clock.sec(24), () -> {
			showPressKeyToStart(g);
		});

		game.state.runAt(clock.sec(30), () -> {
			end();
			start();
		});
	}

	private void introduceGhost(Graphics2D g, int id, int y, boolean showCharacter, boolean showNickname) {
		g.drawImage(rendering.assets.ghostsWalking.get(id).get(RIGHT).frame(), t(1), y, null);
		if (showCharacter) {
			String character = rendering.translator.apply("CLASSIC.GHOST." + id + ".CHARACTER");
			Color color = rendering.assets.ghostColors[id];
			g.setColor(color);
			g.setFont(rendering.assets.scoreFont);
			g.drawString("-" + character, t(4), y + 13);
		}
		if (showNickname) {
			String nickname = "\"" + rendering.translator.apply("CLASSIC.GHOST." + id + ".NICKNAME") + "\"";
			Color color = rendering.assets.ghostColors[id];
			g.setColor(color);
			g.setFont(rendering.assets.scoreFont);
			g.drawString(nickname, t(15), y + 13);
		}
	}

	private void showGhostsChasingPacMan(Graphics2D g, Pac pac) {
		if (blinking.animate()) {
			g.setColor(Color.PINK);
			g.fillOval(t(2), chaseY, TS, TS);
		}
		pac.moveFreely();
		game.ghosts().forEach(Ghost::moveFreely);
		if (differsAtMost(pac.position.x, 2 * TS, 1) && pac.dir == LEFT) {
			// let Pac turn around
			chasingPac = false;
			pac.dir = RIGHT;
			game.ghosts().forEach(ghost -> {
				ghost.dir = ghost.wishDir = RIGHT;
				ghost.speed = 0.4f;
				ghost.state = FRIGHTENED;
				rendering.letGhostBeFrightened(ghost, true);
			});
			rendering.soundManager.stopSound(PacManGameSound.GHOST_SIREN_1);
			rendering.soundManager.loopSound(PacManGameSound.PACMAN_POWER);
		}
		rendering.drawPac(g, pac);
		game.ghosts().forEach(ghost -> {
			rendering.drawGhost(g, ghost, game);
		});
	}

	private void showPacManChasingGhosts(Graphics2D g, Pac pac) {
		if (pac.position.x < size.x + 20) {
			pac.moveFreely();
			game.ghosts().forEach(Ghost::moveFreely);
			for (Ghost ghost : game.ghosts) {
				if (pac.position.x < ghost.position.x) {
					rendering.drawGhost(g, ghost, game);
				} else if (pac.position.x > ghost.position.x && pac.position.x <= ghost.position.x + 2 * TS) {
					ghost.bounty = (int) (Math.pow(2, ghost.id) * 200);
					rendering.drawGhost(g, ghost, game);
					if (lastKilledGhostID != ghost.id) {
						lastKilledGhostID++;
						rendering.soundManager.playSound(PacManGameSound.GHOST_EATEN);
					}
				}
			}
			rendering.drawPac(g, pac);
		}
	}

	private void showPressKeyToStart(Graphics2D g) {
		g.setColor(Color.ORANGE);
		g.setFont(rendering.assets.scoreFont);
		if (blinking.animate()) {
			drawHCenteredText(g, rendering.translator.apply("PRESS_KEY_TO_PLAY"), size.y - 20);
		}
	}

	private void showPointsAnimation(Graphics2D g) {
		g.setColor(Color.PINK);
		g.fillRect(t(9) + 6, t(27) + 2, 2, 2);
		if (blinking.animate()) {
			g.fillOval(t(9), t(29) - 2, 10, 10);
		}
		g.setColor(Color.WHITE);
		g.setFont(rendering.assets.scoreFont);
		g.drawString("10", t(12), t(28));
		g.drawString("50", t(12), t(30));
		g.setFont(rendering.assets.scoreFont.deriveFont(6f));
		g.drawString(rendering.translator.apply("POINTS"), t(15), t(28));
		g.drawString(rendering.translator.apply("POINTS"), t(15), t(30));
	}
}