package de.amr.games.pacman.ui.swing.scene;

import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import de.amr.games.pacman.model.GameModel;
import de.amr.games.pacman.sound.SoundManager;
import de.amr.games.pacman.ui.swing.rendering.GameRendering;

/**
 * Common game scene base class.
 * 
 * @author Armin Reichert
 */
public abstract class GameScene<R extends GameRendering> {

	protected final Dimension size;
	protected final R rendering;
	protected GameModel game;
	protected SoundManager sounds;

	public GameScene(Dimension size, R rendering, SoundManager sounds) {
		this.size = size;
		this.rendering = rendering;
		this.sounds = sounds;
	}

	public GameScene(Dimension size, R rendering) {
		this.size = size;
		this.rendering = rendering;
	}

	public abstract void update();

	public void start() {
	}

	public void end() {
	}

	public abstract void render(Graphics2D g);

	public Dimension size() {
		return size;
	}

	public void setGame(GameModel game) {
		this.game = game;
	}

	public void drawHCenteredText(Graphics2D g, String text, int y) {
		g.drawString(text, (size().width - g.getFontMetrics().stringWidth(text)) / 2, y);
	}

	public void drawHCenteredImage(Graphics2D g, BufferedImage image, int y) {
		g.drawImage(image, (size().width - image.getWidth()) / 2, y, null);
	}
}