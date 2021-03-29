package de.amr.games.pacman.ui.mspacman;

import de.amr.games.pacman.model.common.GameEntity;
import de.amr.games.pacman.model.common.Pac;
import de.amr.games.pacman.model.mspacman.Flap;
import de.amr.games.pacman.model.mspacman.JuniorBag;
import de.amr.games.pacman.model.mspacman.Stork;
import de.amr.games.pacman.ui.CommonPacManGameRendering2D;

/**
 * Ms. Pac-Man 2D rendering interface.
 * 
 * @author Armin Reichert
 *
 * @param <GC>    specific graphics context type
 * @param <COLOR> specific color type
 */
public interface MsPacManGameRendering2D<GC, COLOR, FONT, SPRITE> extends CommonPacManGameRendering2D<GC, COLOR, FONT, SPRITE> {

	void drawFlap(GC g, Flap flap);

	void drawHeart(GC g, GameEntity heart);

	void drawStork(GC g, Stork stork);

	void drawJuniorBag(GC g, JuniorBag bag);

	void drawSpouse(GC g, Pac pac);
}