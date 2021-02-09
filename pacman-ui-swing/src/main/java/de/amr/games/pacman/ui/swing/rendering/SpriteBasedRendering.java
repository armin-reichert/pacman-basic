package de.amr.games.pacman.ui.swing.rendering;

import static de.amr.games.pacman.world.PacManGameWorld.HTS;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;

import de.amr.games.pacman.model.AbstractPacManGame;
import de.amr.games.pacman.model.creatures.Bonus;
import de.amr.games.pacman.model.creatures.Creature;
import de.amr.games.pacman.model.creatures.Ghost;
import de.amr.games.pacman.model.creatures.Pac;
import de.amr.games.pacman.ui.api.PacManGameRendering;
import de.amr.games.pacman.ui.swing.assets.Spritesheet;

public interface SpriteBasedRendering extends PacManGameRendering {

	Spritesheet spritesheet();

	BufferedImage bonusSprite(Bonus guy, AbstractPacManGame game);

	BufferedImage ghostSprite(Ghost guy, AbstractPacManGame game);

	BufferedImage pacSprite(Pac guy, AbstractPacManGame game);

	default Graphics2D smoothGC(Graphics2D g) {
		Graphics2D g2 = (Graphics2D) g.create();
		g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
		return g2;
	}

	@Override
	default void drawGuy(Graphics2D g, Creature guy, AbstractPacManGame game) {
		if (guy.visible) {
			BufferedImage sprite = sprite(guy, game);
			if (sprite != null) {
				int dx = sprite.getWidth() / 2 - HTS, dy = sprite.getHeight() / 2 - HTS;
				Graphics2D g2 = smoothGC(g);
				drawSprite(g2, sprite, guy.position.x - dx, guy.position.y - dy);
				g2.dispose();
			}
		}
	}

	default void drawSprite(Graphics2D g, BufferedImage sprite, float x, float y) {
		g.drawImage(sprite, (int) x, (int) y, null);
	}

	default BufferedImage sprite(Creature guy, AbstractPacManGame game) {
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

}