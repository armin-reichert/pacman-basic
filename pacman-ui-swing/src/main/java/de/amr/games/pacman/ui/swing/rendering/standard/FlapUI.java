package de.amr.games.pacman.ui.swing.rendering.standard;

import de.amr.games.pacman.lib.Animation;
import de.amr.games.pacman.model.common.Flap;
import de.amr.games.pacman.ui.swing.rendering.SwingRendering;

/**
 * The flap used in the intermission scenes.
 * 
 * @author Armin Reichert
 */
public class FlapUI extends Flap {

	public final Animation<?> flapping;

	public FlapUI(int number, String title, SwingRendering rendering) {
		super(number, title);
		flapping = rendering.flapFlapping();
	}
}