package de.amr.games.pacman.model.common;

import java.util.stream.Stream;

import de.amr.games.pacman.model.pacman.Bonus;

/**
 * Game model interface for Pac-Man and Ms. Pac-Man
 * 
 * @author Armin Reichert
 */
public interface GameModel {

	/**
	 * @return the game variant identifier
	 */
	GameVariant variant();

	/**
	 * Creates the level with the given number.
	 * 
	 * @param levelNumber 1-based game level number
	 */
	void createLevel(int levelNumber);

	/**
	 * @param levelNumber 1-based game level number
	 * @return 1-based maze number of the maze used in that level
	 */
	int mazeNumber(int levelNumber);

	/**
	 * @param mazeNumber 1-based number of a maze
	 * @return 1-based number of the world map used by that maze
	 */
	int mapNumber(int mazeNumber);

	GameLevel currentLevel();

	long getHuntingPhaseDuration(int phase);

	/**
	 * @return number (1,2,3) of intermission scene played after the current level or {@code 0} if no
	 *         intermission occurs after current level
	 */
	int intermissionNumber();

	String levelSymbol(int levelNumber);

	void countLevel();

	int lives();

	void addLife();

	void removeLife();

	int score();

	void addScore(int points);

	int hiscorePoints();

	void setHiscorePoints(int score);

	int hiscoreLevel();

	void setHiscoreLevel(int number);

	void saveHiscore();

	void reset();

	void resetGuys();

	Pac player();

	Stream<Ghost> ghosts();

	Stream<Ghost> ghosts(GhostState state);

	Ghost ghost(int id);

	int ghostBounty();

	void setGhostBounty(int value);

	Bonus bonus();

	int bonusValue(String bonus);

	int globalDotCounter();

	void setGlobalDotCounter(int value);

	boolean isGlobalDotCounterEnabled();

	void enableGlobalDotCounter(boolean enable);
}