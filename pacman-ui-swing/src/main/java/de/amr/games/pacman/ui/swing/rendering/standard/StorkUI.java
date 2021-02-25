package de.amr.games.pacman.ui.swing.rendering.standard;

import static de.amr.games.pacman.ui.swing.rendering.standard.MsPacMan_StandardRendering.assets;

import java.awt.image.BufferedImage;

import de.amr.games.pacman.lib.Animation;
import de.amr.games.pacman.model.common.GameEntity;

public class StorkUI extends GameEntity {

	public final Animation<BufferedImage> flying;

	public StorkUI() {
		flying = Animation.of(//
				assets.region(489, 176, 32, 16), //
				assets.region(521, 176, 32, 16)//
		).endless().frameDuration(10);
	}
}