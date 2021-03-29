package de.amr.games.pacman.ui.pacman;

import de.amr.games.pacman.lib.V2d;
import de.amr.games.pacman.model.common.GameEntity;
import de.amr.games.pacman.model.common.Ghost;
import de.amr.games.pacman.model.common.Pac;
import de.amr.games.pacman.ui.CommonPacManGameRendering2D;

/**
 * Pac-Man game 2D rendering interface.
 * 
 * @author Armin Reichert
 *
 * @param <GC>    specific graphics context type
 * @param <COLOR> specific color type
 */
public interface PacManGameRendering2D<GC, COLOR, FONT, SPRITE> extends CommonPacManGameRendering2D<GC, COLOR, FONT, SPRITE> {

	void drawBigPacMan(GC g, Pac bigPacMan);

	void drawNail(GC g, GameEntity nail);

	void drawBlinkyStretched(GC g, Ghost blinky, V2d nailPosition, int stretching);

	void drawBlinkyPatched(GC g, Ghost blinky);

	void drawBlinkyNaked(GC g, Ghost blinky);
}