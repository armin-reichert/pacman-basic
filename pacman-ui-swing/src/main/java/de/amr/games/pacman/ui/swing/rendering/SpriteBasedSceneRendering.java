package de.amr.games.pacman.ui.swing.rendering;

import java.awt.image.BufferedImage;

import de.amr.games.pacman.model.GameModel;
import de.amr.games.pacman.model.guys.Bonus;
import de.amr.games.pacman.model.guys.Creature;
import de.amr.games.pacman.model.guys.Ghost;
import de.amr.games.pacman.model.guys.Pac;
import de.amr.games.pacman.ui.swing.assets.Spritesheet;

public interface SpriteBasedSceneRendering extends GameRendering {

	Spritesheet spritesheet();

	BufferedImage bonusSprite(Bonus guy, GameModel game);

	BufferedImage ghostSprite(Ghost guy, GameModel game);

	BufferedImage pacSprite(Pac guy, GameModel game);

	BufferedImage lifeSprite();

	default BufferedImage sprite(Creature guy, GameModel game) {
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