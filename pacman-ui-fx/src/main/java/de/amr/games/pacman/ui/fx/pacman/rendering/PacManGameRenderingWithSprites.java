package de.amr.games.pacman.ui.fx.pacman.rendering;

import de.amr.games.pacman.model.Bonus;
import de.amr.games.pacman.model.Ghost;
import de.amr.games.pacman.model.Pac;
import de.amr.games.pacman.model.PacManGameModel;
import javafx.geometry.Rectangle2D;

public interface PacManGameRenderingWithSprites {

	Rectangle2D bonusSprite(Bonus bonus, PacManGameModel game);

	Rectangle2D pacSprite(Pac pac, PacManGameModel game);

	Rectangle2D ghostSprite(Ghost ghost, PacManGameModel game);
}
