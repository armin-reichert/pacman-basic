package de.amr.games.pacman.model.common;

import java.util.stream.Stream;

import de.amr.games.pacman.model.pacman.Bonus;

public interface GameModel {

	void reset();

	void resetGuys();

	void initLevel(int levelNumber);

	GameLevel currentLevel();

	long getHuntingPhaseDuration(int phase);

	int intermissionNumber();

	String levelSymbol(int levelNumber);

	void addLevelSymbol(String symbol);

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