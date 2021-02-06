package de.amr.games.pacman.ui.swing.mspacman;

import static de.amr.games.pacman.model.creatures.GhostState.DEAD;
import static de.amr.games.pacman.model.creatures.GhostState.ENTERING_HOUSE;
import static de.amr.games.pacman.model.creatures.GhostState.FRIGHTENED;
import static de.amr.games.pacman.model.creatures.GhostState.LOCKED;
import static de.amr.games.pacman.world.PacManGameWorld.HTS;
import static de.amr.games.pacman.world.PacManGameWorld.TS;
import static de.amr.games.pacman.world.PacManGameWorld.t;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.util.function.Function;

import de.amr.games.pacman.controller.PacManGameState;
import de.amr.games.pacman.lib.Animation;
import de.amr.games.pacman.lib.Direction;
import de.amr.games.pacman.model.AbstractPacManGame;
import de.amr.games.pacman.model.creatures.Bonus;
import de.amr.games.pacman.model.creatures.Creature;
import de.amr.games.pacman.model.creatures.Ghost;
import de.amr.games.pacman.model.creatures.Pac;
import de.amr.games.pacman.ui.api.PacManGameAnimations;
import de.amr.games.pacman.ui.sound.PacManGameSoundManager;

/**
 * Rendering for the Ms. Pac-Man game.
 * 
 * @author Armin Reichert
 */
public class MsPacManRendering implements PacManGameAnimations {

	public final MsPacManAssets assets;
	public final PacManGameSoundManager soundManager;
	public final Function<String, String> translator;

	public MsPacManRendering(Function<String, String> translator) {
		assets = new MsPacManAssets();
		soundManager = new PacManGameSoundManager(assets.soundMap::get);
		this.translator = translator;
	}

	@Override
	public Animation<BufferedImage> pacMunching(Direction dir) {
		return assets.pacMunching.get(dir);
	}

	@Override
	public Animation<BufferedImage> pacDying() {
		return assets.pacSpinning;
	}

	@Override
	public Animation<BufferedImage> ghostWalking(Ghost ghost, Direction dir) {
		return assets.ghostsWalking.get(ghost.id).get(dir);
	}

	@Override
	public Animation<BufferedImage> ghostFrightened(Ghost ghost, Direction dir) {
		return assets.ghostBlue;
	}

	@Override
	public Animation<BufferedImage> ghostFlashing() {
		return assets.ghostFlashing;
	}

	@Override
	public Animation<BufferedImage> ghostReturningHome(Ghost ghost, Direction dir) {
		return assets.ghostEyes.get(dir);
	}

	@Override
	public Animation<BufferedImage> mazeFlashing(int mazeNumber) {
		return assets.mazesFlashing.get(mazeNumber - 1);
	}

	@Override
	public Animation<Boolean> energizerBlinking() {
		return assets.energizerBlinking;
	}

	public void signalReadyState(Graphics2D g) {
		g.setFont(assets.getScoreFont());
		g.setColor(Color.YELLOW);
		g.drawString(translator.apply("READY"), t(11), t(21));
	}

	public void signalGameOverState(Graphics2D g) {
		g.setFont(assets.getScoreFont());
		g.setColor(Color.RED);
		g.drawString(translator.apply("GAME"), t(9), t(21));
		g.drawString(translator.apply("OVER"), t(15), t(21));
	}

	public void drawScore(Graphics2D g, AbstractPacManGame game) {
		g.setFont(assets.getScoreFont());
		g.translate(0, 2);
		g.setColor(Color.WHITE);
		g.drawString(translator.apply("SCORE"), t(1), t(1));
		g.drawString(translator.apply("HI_SCORE"), t(16), t(1));
		g.translate(0, 1);
		if (game.state != PacManGameState.INTRO && !game.attractMode) {
			g.setColor(assets.getMazeWallColor(game.level.mazeNumber - 1));
			g.drawString(String.format("%08d", game.score), t(1), t(2));
			g.setColor(Color.LIGHT_GRAY);
			g.drawString(String.format("L%02d", game.currentLevelNumber), t(9), t(2));
			g.setColor(assets.getMazeWallColor(game.level.mazeNumber - 1));
			g.drawString(String.format("%08d", game.highscorePoints), t(16), t(2));
			g.setColor(Color.LIGHT_GRAY);
			g.drawString(String.format("L%02d", game.highscoreLevel), t(24), t(2));
		}
		g.translate(0, -3);
	}

	public void drawLivesCounter(Graphics2D g, AbstractPacManGame game, int x, int y) {
		int maxLivesDisplayed = 5;
		int livesDisplayed = game.started ? game.lives - 1 : game.lives;
		for (int i = 0; i < Math.min(livesDisplayed, maxLivesDisplayed); ++i) {
			g.drawImage(assets.spriteAt(1, 0), x + t(2 * i), y, null);
		}
		if (game.lives > maxLivesDisplayed) {
			g.setColor(Color.YELLOW);
			g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 6));
			g.drawString("+" + (game.lives - maxLivesDisplayed), x + t(10) - 4, y + t(2));
		}
	}

	public void drawLevelCounter(Graphics2D g, AbstractPacManGame game, int rightX, int y) {
		int x = rightX;
		for (int firstlevelNumber = 1; firstlevelNumber <= Math.min(game.currentLevelNumber, 7); ++firstlevelNumber) {
			byte symbol = game.levelSymbols.get(firstlevelNumber - 1);
			g.drawImage(assets.spriteAt(assets.symbolsSSL[symbol]), x, y, null);
			x -= t(2);
		}
	}

	public void drawMaze(Graphics2D g, AbstractPacManGame game) {
		if (mazeFlashing(game.level.mazeNumber).hasStarted()) {
			g.drawImage(mazeFlashing(game.level.mazeNumber).animate(), 0, t(3), null);
			return;
		}
		g.drawImage(assets.mazesFull.get(game.level.mazeNumber - 1), 0, t(3), null);
		game.level.world.tiles().filter(game.level::isFoodRemoved).forEach(tile -> {
			g.setColor(Color.BLACK);
			g.fillRect(t(tile.x), t(tile.y), TS, TS);
		});
		if (energizerBlinking().isRunning() && energizerBlinking().animate()) {
			game.level.world.energizerTiles().forEach(tile -> {
				g.setColor(Color.BLACK);
				g.fillRect(t(tile.x), t(tile.y), TS, TS);
			});
		}
		drawBonus(g, game.bonus);
	}

	public void drawPac(Graphics2D g, Pac pac) {
		drawGuy(g, pac, sprite(pac));
	}

	public void drawGhost(Graphics2D g, Ghost ghost, AbstractPacManGame game) {
		drawGuy(g, ghost, sprite(ghost, game));
	}

	public void drawBonus(Graphics2D g, Bonus bonus) {
		int x = (int) bonus.position.x - HTS;
		int y = (int) bonus.position.y - HTS + assets.bonusJumps.animate();
		if (bonus.edibleTicksLeft > 0) {
			g.drawImage(assets.spriteAt(assets.symbolsSSL[bonus.symbol]), x, y, null);
		} else if (bonus.eatenTicksLeft > 0) {
			g.drawImage(assets.spriteAt(assets.bonusValuesSSL.get(bonus.points)), x, y, null);
		}
	}

	private void drawGuy(Graphics2D g, Creature guy, BufferedImage sprite) {
		if (guy.visible) {
			Graphics2D g2 = (Graphics2D) g.create();
			int dx = (sprite.getWidth() - TS) / 2, dy = (sprite.getHeight() - TS) / 2;
			g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
			g2.drawImage(sprite, (int) (guy.position.x) - dx, (int) (guy.position.y) - dy, null);
			g2.dispose();
		}
	}

	private BufferedImage sprite(Pac pac) {
		if (pac.dead) {
			return pacDying().hasStarted() ? pacDying().animate() : pacMunching(pac.dir).frame();
		}
		return pac.speed == 0 || !pac.couldMove ? pacMunching(pac.dir).frame(1) : pacMunching(pac.dir).animate();
	}

	private BufferedImage sprite(Ghost ghost, AbstractPacManGame game) {
		if (ghost.bounty > 0) {
			return assets.spriteAt(assets.bountyNumbersSSL.get(ghost.bounty));
		}
		if (ghost.is(DEAD) || ghost.is(ENTERING_HOUSE)) {
			return ghostReturningHome(ghost, ghost.dir).animate();
		}
		if (ghost.is(FRIGHTENED)) {
			return ghostFlashing().isRunning() ? ghostFlashing().frame() : ghostFrightened(ghost, ghost.dir).animate();
		}
		if (ghost.is(LOCKED) && game.pac.powerTicksLeft > 0) {
			return ghostFrightened(ghost, ghost.dir).animate();
		}
		return ghostWalking(ghost, ghost.wishDir).animate();
	}
}