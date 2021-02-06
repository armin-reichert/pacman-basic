package de.amr.games.pacman.ui.swing;

import static de.amr.games.pacman.world.PacManGameWorld.TS;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;

import de.amr.games.pacman.model.AbstractPacManGame;
import de.amr.games.pacman.model.creatures.Bonus;
import de.amr.games.pacman.model.creatures.Creature;
import de.amr.games.pacman.model.creatures.Ghost;
import de.amr.games.pacman.model.creatures.Pac;

public abstract class SpriteBasedRendering implements PacManGameRendering {

	protected abstract Spritesheet spritesheet();

	protected abstract BufferedImage bonusSprite(Bonus guy, AbstractPacManGame game);

	protected abstract BufferedImage ghostSprite(Ghost guy, AbstractPacManGame game);

	protected abstract BufferedImage pacSprite(Pac guy, AbstractPacManGame game);

	@Override
	public void drawGuy(Graphics2D g, Creature guy, AbstractPacManGame game) {
		if (guy.visible) {
			BufferedImage sprite = sprite(guy, game);
			int dx = (sprite.getWidth() - TS) / 2, dy = (sprite.getHeight() - TS) / 2;
			Graphics2D g2 = smoothGC(g);
			g2.drawImage(sprite, (int) (guy.position.x) - dx, (int) (guy.position.y) - dy, null);
			g2.dispose();
		}
	}

	protected BufferedImage sprite(Creature guy, AbstractPacManGame game) {
		// we don't need polymorphism yet
		if (guy instanceof Pac) {
			return pacSprite((Pac) guy, game);
		} else if (guy instanceof Ghost) {
			return ghostSprite((Ghost) guy, game);
		} else if (guy instanceof Bonus) {
			return bonusSprite((Bonus) guy, game);
		}
		return null;
	}

	protected Graphics2D smoothGC(Graphics2D g) {
		Graphics2D g2 = (Graphics2D) g.create();
		g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
		return g2;
	}
}