package de.amr.games.pacman.ui.swing.rendering.standard;

import static de.amr.games.pacman.ui.swing.rendering.standard.MsPacMan_StandardRendering.assets;

import java.awt.image.BufferedImage;

import de.amr.games.pacman.lib.Animation;
import de.amr.games.pacman.model.common.Flap;

/**
 * The flap used in the intermission scenes.
 * 
 * @author Armin Reichert
 */
public class FlapUI extends Flap {

	public final Animation<BufferedImage> flapping;

	public FlapUI(int number, String title) {
		super(number, title);
		flapping = Animation.of( //
				assets.region(456, 208, 32, 32), //
				assets.region(488, 208, 32, 32), //
				assets.region(520, 208, 32, 32), //
				assets.region(488, 208, 32, 32), //
				assets.region(456, 208, 32, 32)//
		).repetitions(1).frameDuration(4);
	}
}