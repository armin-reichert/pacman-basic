package de.amr.games.pacman.ui.swing.rendering.standard;

import de.amr.games.pacman.lib.Animation;
import de.amr.games.pacman.model.common.GameEntity;
import de.amr.games.pacman.ui.swing.rendering.SwingRendering;

/**
 * Stork with flying animation.
 * 
 * @author Armin Reichert
 */
public class StorkUI extends GameEntity {

	public final Animation<?> flying;

	public StorkUI(SwingRendering rendering) {
		flying = rendering.storkFlying();
	}
}