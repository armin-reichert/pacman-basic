package de.amr.games.pacman.ui.swing.rendering;

import static de.amr.games.pacman.world.PacManGameWorld.HTS;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;

import de.amr.games.pacman.model.Bonus;
import de.amr.games.pacman.model.Creature;
import de.amr.games.pacman.model.Ghost;
import de.amr.games.pacman.model.Pac;
import de.amr.games.pacman.model.PacManGameModel;
import de.amr.games.pacman.ui.swing.assets.Spritesheet;

public interface SpriteBasedSceneRendering extends SceneRendering {

	Spritesheet spritesheet();

	BufferedImage bonusSprite(Bonus guy, PacManGameModel game);

	BufferedImage ghostSprite(Ghost guy, PacManGameModel game);

	BufferedImage pacSprite(Pac guy, PacManGameModel game);

	default Graphics2D smoothGC(Graphics2D g) {
		Graphics2D g2 = (Graphics2D) g.create();
		g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
		return g2;
	}

	@Override
	default void drawGuy(Graphics2D g, Creature guy, PacManGameModel game) {
		if (guy.visible) {
			BufferedImage sprite = sprite(guy, game);
			if (sprite != null) {
				int dx = sprite.getWidth() / 2 - HTS, dy = sprite.getHeight() / 2 - HTS;
				Graphics2D g2 = smoothGC(g);
				g2.drawImage(sprite, (int) guy.position.x - dx, (int) guy.position.y - dy, null);
				g2.dispose();
			}
		}
	}

	default BufferedImage sprite(Creature guy, PacManGameModel game) {
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