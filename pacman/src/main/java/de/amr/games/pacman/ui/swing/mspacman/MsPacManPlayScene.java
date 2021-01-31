package de.amr.games.pacman.ui.swing.mspacman;

import static de.amr.games.pacman.controller.PacManGameState.HUNTING;
import static de.amr.games.pacman.heaven.God.clock;
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
import java.awt.image.BufferedImage;
import java.util.Optional;

import de.amr.games.pacman.lib.Direction;
import de.amr.games.pacman.lib.V2i;
import de.amr.games.pacman.model.PacManGameModel;
import de.amr.games.pacman.model.creatures.Ghost;
import de.amr.games.pacman.model.creatures.Pac;
import de.amr.games.pacman.ui.api.PacManGameAnimations;
import de.amr.games.pacman.ui.api.PacManGameScene;
import de.amr.games.pacman.ui.swing.AbstractPacManPlayScene;
import de.amr.games.pacman.ui.swing.Animation;
import de.amr.games.pacman.ui.swing.PacManGameSwingUI;

/**
 * Scene where the Ms. Pac-Man game is played.
 * 
 * @author Armin Reichert
 */
public class MsPacManPlayScene extends AbstractPacManPlayScene implements PacManGameScene {

	private final MsPacManAssets assets;

	public MsPacManPlayScene(PacManGameSwingUI ui, V2i size, MsPacManAssets assets, PacManGameModel game) {
		super(ui, size, game);
		this.assets = assets;
	}

	@Override
	public Optional<PacManGameAnimations> animations() {
		return Optional.of(assets);
	}

	@Override
	protected Color getScoreColor() {
		switch (game.level.mazeNumber) {
		case 1:
			return new Color(255, 183, 174);
		case 2:
			return new Color(71, 183, 255);
		case 3:
			return new Color(222, 151, 81);
		case 4:
			return new Color(33, 33, 255);
		case 5:
			return new Color(255, 183, 255);
		case 6:
			return new Color(255, 183, 174);
		default:
			return Color.WHITE;
		}
	}

	@Override
	protected Font getScoreFont() {
		return assets.scoreFont;
	}

	@Override
	protected void drawLevelCounter(Graphics2D g) {
		int x = t(game.level.world.xTiles() - 4);
		for (int levelNumber = 1; levelNumber <= Math.min(game.currentLevelNumber, 7); ++levelNumber) {
			byte symbol = game.levelSymbols.get(levelNumber - 1);
			g.drawImage(assets.spriteAt(assets.symbolTiles[symbol]), x, size.y - t(2), null);
			x -= t(2);
		}
	}

	@Override
	protected void drawMaze(Graphics2D g) {
		Animation<BufferedImage> mazeFlashing = assets.mazeFlashingAnimations.get(game.level.mazeNumber - 1);
		if (mazeFlashing.isRunning() || mazeFlashing.isComplete()) {
			BufferedImage frame = mazeFlashing.currentFrameThenAdvance();
			if (frame != null) {
				g.drawImage(frame, 0, t(3), null);
			}
			return;
		}
		g.drawImage(assets.mazeFull[game.level.mazeNumber - 1], 0, t(3), null);
		game.level.world.tiles().filter(game.level::isFoodRemoved).forEach(tile -> {
			g.setColor(Color.BLACK);
			g.fillRect(t(tile.x), t(tile.y), TS, TS);
		});
		// TODO use animation instead?
		if (clock.ticksTotal % 20 < 10 && game.state == HUNTING) {
			game.level.world.energizerTiles().forEach(tile -> {
				g.setColor(Color.BLACK);
				g.fillRect(t(tile.x), t(tile.y), TS, TS);
			});
		}
		drawBonus(g);
	}

	private static final int BONUS_JUMP[] = { -2, 0, 2 };

	private void drawBonus(Graphics2D g) {
		int x = (int) (game.bonus.position.x) - HTS;
		int y = (int) (game.bonus.position.y) - HTS;
		if (game.bonus.edibleTicksLeft > 0) {
			int frame = clock.frame(20, BONUS_JUMP.length);
			if (game.bonus.dir == Direction.LEFT || game.bonus.dir == Direction.RIGHT) {
				y += BONUS_JUMP[frame]; // TODO this is not yet correct
			}
			g.drawImage(assets.spriteAt(assets.symbolTiles[game.bonus.symbol]), x, y, null);
		} else if (game.bonus.eatenTicksLeft > 0) {

			g.drawImage(assets.spriteAt(assets.bonusValueTiles.get(game.bonus.points)), x, y, null);
		}
	}

	@Override
	protected BufferedImage lifeSprite() {
		return assets.life;
	}

	@Override
	protected BufferedImage sprite(Pac pac) {
		if (pac.dead) {
			if (assets.pacSpinning.isRunning() || assets.pacSpinning.isComplete()) {
				return assets.pacSpinning.currentFrameThenAdvance();
			}
			return assets.pacMouthOpen.get(pac.dir);
		}
		if (pac.speed == 0 || !pac.couldMove) {
			return assets.pacMouthOpen.get(pac.dir);
		}
		return assets.pacMunching.get(pac.dir).currentFrameThenAdvance();
	}

	@Override
	protected BufferedImage sprite(Ghost ghost) {
		if (ghost.bounty > 0) {
			return assets.spriteAt(assets.bountyNumberTiles.get(ghost.bounty));
		}
		if (ghost.is(DEAD) || ghost.is(ENTERING_HOUSE)) {
			return assets.ghostEyes.get(ghost.wishDir);
		}
		if (ghost.is(FRIGHTENED)) {
			if (assets.ghostFlashing(ghost).isRunning()) {
				return assets.ghostFlashing(ghost).currentFrameThenAdvance();
			}
			return assets.ghostBlue.currentFrameThenAdvance();
		}
		if (ghost.is(LOCKED) && game.pac.powerTicksLeft > 0) {
			return assets.ghostBlue.currentFrameThenAdvance();
		}
		return assets.ghostWalking.get(ghost.id).get(ghost.wishDir).currentFrameThenAdvance();
	}
}