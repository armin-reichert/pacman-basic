package de.amr.games.pacman.ui.swing.classic;

import static de.amr.games.pacman.controller.PacManGameState.HUNTING;
import static de.amr.games.pacman.heaven.God.clock;
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

import de.amr.games.pacman.controller.PacManGameState;
import de.amr.games.pacman.lib.V2i;
import de.amr.games.pacman.model.PacManGameModel;
import de.amr.games.pacman.model.creatures.Bonus;
import de.amr.games.pacman.model.creatures.Ghost;
import de.amr.games.pacman.model.creatures.GhostState;
import de.amr.games.pacman.model.creatures.Pac;
import de.amr.games.pacman.ui.api.PacManGameAnimations;
import de.amr.games.pacman.ui.api.PacManGameScene;
import de.amr.games.pacman.ui.swing.AbstractPacManPlayScene;
import de.amr.games.pacman.ui.swing.PacManGameSwingUI;

/**
 * Scene where the game is played.
 * 
 * @author Armin Reichert
 */
public class PacManClassicPlayScene extends AbstractPacManPlayScene implements PacManGameScene {

	private final PacManClassicAssets assets;

	public PacManClassicPlayScene(PacManGameSwingUI ui, V2i size, PacManClassicAssets assets, PacManGameModel game) {
		super(ui, size, game);
		this.assets = assets;
	}

	@Override
	public Optional<PacManGameAnimations> animations() {
		return Optional.of(assets);
	}

	@Override
	protected void drawLevelCounter(Graphics2D g) {
		int x = t(game.level.world.xTiles() - 4);
		int first = Math.max(1, game.currentLevelNumber - 6);
		for (int levelNumber = first; levelNumber <= game.currentLevelNumber; ++levelNumber) {
			V2i symbolTile = assets.symbolTiles[game.levelSymbols.get(levelNumber - 1)];
			g.drawImage(assets.spriteAt(symbolTile), x, size.y - t(2), null);
			x -= t(2);
		}
	}

	@Override
	protected void drawMaze(Graphics2D g) {
		if (assets.mazeFlashing.isRunning() || assets.mazeFlashing.isComplete()) {
			g.drawImage(assets.mazeFlashing.currentFrameThenAdvance(), 0, t(3), null);
			return;
		}
		g.drawImage(assets.mazeFull, 0, t(3), null);
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
		drawBonus(g, game.bonus);
	}

	protected void drawBonus(Graphics2D g, Bonus bonus) {
		if (bonus.edibleTicksLeft > 0) {
			drawGuy(g, bonus, assets.spriteAt(assets.symbolTiles[bonus.symbol]));
		}
		if (bonus.eatenTicksLeft > 0) {
			if (game.bonus.points != 1000) {
				drawGuy(g, game.bonus, assets.numbers.get(game.bonus.points));
			} else {
				// this sprite is somewhat nasty
				g.drawImage(assets.numbers.get(1000), (int) (bonus.position.x) - HTS - 2, (int) (bonus.position.y) - HTS, null);
			}
		}
	}

	@Override
	protected Color getScoreColor() {
		return Color.yellow;
	}

	@Override
	protected Font getScoreFont() {
		return assets.scoreFont;
	}

	@Override
	protected BufferedImage lifeSprite() {
		return assets.life;
	}

	@Override
	protected BufferedImage sprite(Pac pac) {
		if (pac.dead) {
			if (assets.pacCollapsing.isRunning() || assets.pacCollapsing.isComplete()) {
				return assets.pacCollapsing.currentFrameThenAdvance();
			}
			return assets.pacMouthClosed;
		}
		if (pac.speed == 0) {
			return assets.pacMouthClosed;
		}
		if (!pac.couldMove) {
			return assets.pacMouthOpen.get(pac.dir);
		}
		return assets.pacMunching.get(pac.dir).currentFrameThenAdvance();
	}

	@Override
	protected BufferedImage sprite(Ghost ghost) {
		if (ghost.bounty > 0) {
			return assets.numbers.get(ghost.bounty);
		}
		if (ghost.state == GhostState.DEAD || ghost.state == GhostState.ENTERING_HOUSE) {
			return assets.ghostEyes.get(ghost.wishDir);
		}
		if (ghost.is(FRIGHTENED)) {
			if (game.pac.powerTicksLeft <= assets.ghostFlashing.getDuration() * game.level.numFlashes
					&& game.state == PacManGameState.HUNTING) {
				return assets.ghostFlashing.currentFrameThenAdvance();
			}
			return assets.ghostBlue.currentFrameThenAdvance();
		}
		if (ghost.is(LOCKED) && game.pac.powerTicksLeft > 0) {
			return assets.ghostBlue.currentFrameThenAdvance();
		}
		return assets.ghostWalking.get(ghost.id).get(ghost.wishDir).currentFrameThenAdvance();
	}
}