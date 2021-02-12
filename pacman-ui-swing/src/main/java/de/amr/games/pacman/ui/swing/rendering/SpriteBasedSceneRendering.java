package de.amr.games.pacman.ui.swing.rendering;

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

	BufferedImage lifeSprite();

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