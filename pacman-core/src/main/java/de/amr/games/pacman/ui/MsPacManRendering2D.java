package de.amr.games.pacman.ui;

import de.amr.games.pacman.model.common.GameEntity;
import de.amr.games.pacman.model.common.Pac;
import de.amr.games.pacman.model.mspacman.Flap;
import de.amr.games.pacman.model.mspacman.JuniorBag;
import de.amr.games.pacman.model.mspacman.Stork;

/**
 * Ms. Pac-Man 2D rendering interface.
 * 
 * @author Armin Reichert
 *
 * @param <GC>    specific graphics context type
 * @param <COLOR> specific color type
 */
public interface MsPacManRendering2D<GC, COLOR, FONT, SPRITE> extends CommonPacManRendering2D<GC, COLOR, FONT, SPRITE> {

	void drawFlap(GC g, Flap flap);

	void drawHeart(GC g, GameEntity heart);

	void drawStork(GC g, Stork stork);

	void drawJuniorBag(GC g, JuniorBag bag);

	void drawSpouse(GC g, Pac pac);
}