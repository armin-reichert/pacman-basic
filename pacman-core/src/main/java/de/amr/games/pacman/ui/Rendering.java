package de.amr.games.pacman.ui;

import java.util.function.Predicate;
import java.util.stream.Stream;

import de.amr.games.pacman.controller.PacManGameState;
import de.amr.games.pacman.lib.V2d;
import de.amr.games.pacman.lib.V2i;
import de.amr.games.pacman.model.common.AbstractGameModel;
import de.amr.games.pacman.model.common.GameEntity;
import de.amr.games.pacman.model.common.Ghost;
import de.amr.games.pacman.model.common.Pac;
import de.amr.games.pacman.model.mspacman.Flap;
import de.amr.games.pacman.model.mspacman.JuniorBag;
import de.amr.games.pacman.model.mspacman.Stork;
import de.amr.games.pacman.model.pacman.PacManBonus;

/**
 * This interface is used to discriminate the Swing and JavaFX UI implementations. It is not
 * mandatory for other UI implementations.
 * 
 * @author Armin Reichert
 *
 * @param <GC>    specific graphics context type
 * @param <COLOR> specific color type
 */
public interface Rendering<GC, COLOR, FONT, SPRITE> {

	FONT getScoreFont();

	/**
	 * Note: maze numbers are 1-based, maze index as stored here is 0-based.
	 * 
	 * @param mazeIndex
	 * @return
	 */
	COLOR getMazeWallBorderColor(int mazeIndex);

	/**
	 * Note: maze numbers are 1-based, maze index as stored here is 0-based.
	 * 
	 * @param mazeIndex
	 * @return
	 */
	COLOR getMazeWallColor(int mazeIndex);

	void drawLifeCounterSymbol(GC g, int x, int y);

	void drawLivesCounter(GC g, AbstractGameModel game, int x, int y);

	void drawPlayer(GC g, Pac pac);

	void drawGhost(GC g, Ghost ghost, boolean frightened);

	void drawBonus(GC g, PacManBonus bonus);

	void drawTileCovered(GC g, V2i tile);

	void drawMaze(GC g, int mazeNumber, int x, int y, boolean flashing);

	void drawFoodTiles(GC g, Stream<V2i> tiles, Predicate<V2i> eaten);

	void drawEnergizerTiles(GC g, Stream<V2i> energizerTiles);

	void drawGameState(GC g, AbstractGameModel game, PacManGameState gameState);

	void drawScore(GC g, AbstractGameModel game, boolean hiscoreOnly);

	void drawLevelCounter(GC g, AbstractGameModel game, int rightX, int y);

	// Pac-Man game only:

	void drawBigPacMan(GC g, Pac bigPacMan);

	void drawNail(GC g, GameEntity nail);

	void drawBlinkyStretched(GC g, Ghost blinky, V2d nailPosition, int stretching);

	void drawBlinkyPatched(GC g, Ghost blinky);

	void drawBlinkyNaked(GC g, Ghost blinky);

	// Ms. Pac-Man game only:

	void drawFlap(GC g, Flap flap);

	void drawHeart(GC g, GameEntity heart);

	void drawStork(GC g, Stork stork);

	void drawJuniorBag(GC g, JuniorBag bag);

	void drawSpouse(GC g, Pac pac);
}