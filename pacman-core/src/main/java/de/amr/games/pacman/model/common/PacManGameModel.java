/*
MIT License

Copyright (c) 2021 Armin Reichert

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
 */
package de.amr.games.pacman.model.common;

import java.util.OptionalInt;
import java.util.stream.Stream;

import de.amr.games.pacman.model.pacman.entities.Bonus;

/**
 * Game model interface for Pac-Man and Ms. Pac-Man games.
 * 
 * @author Armin Reichert
 */
public interface PacManGameModel {

	/**
	 * Enters the level with the given number.
	 * 
	 * @param levelNumber 1-based game level number
	 */
	void enterLevel(int levelNumber);

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

	/**
	 * @return the current game level
	 */
	GameLevel level();

	/**
	 * @param phase hunting phase index (0..7)
	 * @return duration (ticks) of specified hunting phase (scattering/chasing alternating)
	 */
	long getHuntingPhaseDuration(int phase);

	/**
	 * @return 1-based number of intermission scene played after the specified level number or
	 *         {@code empty}, if no intermission is played after this level
	 */
	OptionalInt intermissionAfterLevel(int levelNumber);

	/**
	 * @param levelNumber 1-based level number
	 * @return name of level symbol
	 */
	String levelSymbol(int levelNumber);

	/**
	 * @return number of player lives left
	 */
	int lives();

	/**
	 * Changes the number of lives by the given delta.
	 */
	void changeLivesBy(int delta);

	/**
	 * @return value of an energizer pellet
	 */
	int energizerValue();

	/**
	 * @return value of a normal pellet
	 */
	int pelletValue();

	/**
	 * @return game score
	 */
	int score();

	/**
	 * Adds given number of points to game score.
	 * 
	 * @param points number of points
	 */
	void addScore(int points);

	/**
	 * @return high score points value
	 */
	int hiscorePoints();

	/**
	 * Sets the high score points value
	 * 
	 * @param score number of points
	 */
	void setHiscorePoints(int score);

	/**
	 * @return level at which current high score has been reached
	 */
	int hiscoreLevel();

	/**
	 * Sets the level at which current high score has been reached.
	 * 
	 * @param number level number
	 */
	void setHiscoreLevel(int levelNumber);

	/**
	 * Saves the current highscore to the highscore file.
	 */
	void saveHiscore();

	/**
	 * @return if the score for granting a bonus is reached
	 */
	boolean isBonusReached();

	/**
	 * Resets the game model to the initial state.
	 */
	void reset();

	/**
	 * Resets the player and the ghosts to their initial state.
	 */
	void resetGuys();

	/**
	 * @return the player entity
	 */
	Pac player();

	/**
	 * @return stream of all ghost entities
	 */
	Stream<Ghost> ghosts();

	/**
	 * @return stream of all ghost entities which are in the specified state
	 */
	Stream<Ghost> ghosts(GhostState state);

	/**
	 * @param id ghost identifier (index)
	 * @return ghost entity with specified identifier
	 */
	Ghost ghost(int id);

	/**
	 * Resets the ghost bounty value.
	 */
	void resetGhostBounty();

	/**
	 * @return next ghost bounty value (e.g. 200 -> 400 -> 800 -> 1600)
	 */
	int getNextGhostBounty();

	/**
	 * Advances the ghost bounty value.
	 */
	void increaseNextGhostBounty();

	/**
	 * @return the bonus entity
	 */
	Bonus bonus();

	/**
	 * @param symbolName bonus symbol name
	 * @return value of the specified bonus
	 */
	int bonusValue(String symbolName);

	/**
	 * @return value of the global dot counter, used by the ghosthouse logic
	 */
	int globalDotCounter();

	/**
	 * Sets the value of the global dot counter.
	 * 
	 * @param value dot counter value
	 */
	void setGlobalDotCounter(int value);

	/**
	 * @return if the global dot counter is enabled
	 */
	boolean isGlobalDotCounterEnabled();

	/**
	 * Sets the global dot counter's enabled state.
	 * 
	 * @param enable new state
	 */
	void enableGlobalDotCounter(boolean enable);
}