package de.amr.games.pacman.ui.api;

import java.awt.Graphics2D;

import de.amr.games.pacman.model.AbstractPacManGame;
import de.amr.games.pacman.model.creatures.Creature;

public interface PacManGameRenderingAPI {

	void signalReadyState(Graphics2D g);

	void signalGameOverState(Graphics2D g);

	void drawScore(Graphics2D g, AbstractPacManGame game);

	void drawLivesCounter(Graphics2D g, AbstractPacManGame game, int x, int y);

	void drawLevelCounter(Graphics2D g, AbstractPacManGame game, int rightX, int y);

	void drawMaze(Graphics2D g, AbstractPacManGame game);

	void drawGuy(Graphics2D g, Creature guy, AbstractPacManGame game);

}
