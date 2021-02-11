package de.amr.games.pacman.ui.swing.rendering.common;

import java.awt.Graphics2D;

import de.amr.games.pacman.model.PacManGameModel;
import de.amr.games.pacman.model.Creature;

public interface PacManGameRendering {

	void signalReadyState(Graphics2D g);

	void signalGameOverState(Graphics2D g);

	void drawScore(Graphics2D g, PacManGameModel game);

	void drawLivesCounter(Graphics2D g, PacManGameModel game, int x, int y);

	void drawLevelCounter(Graphics2D g, PacManGameModel game, int rightX, int y);

	void drawMaze(Graphics2D g, PacManGameModel game);

	void drawGuy(Graphics2D g, Creature guy, PacManGameModel game);

}
