package de.amr.games.pacman.model.common;

import de.amr.games.pacman.ui.animation.Animation;

/**
 * The flap used in the intermission scenes.
 * 
 * @author Armin Reichert
 */
public class Flap extends GameEntity {

	public final int sceneNumber;
	public final String sceneTitle;

	public Animation<?> flapping;

	public Flap(int number, String title, Animation<?> animation) {
		sceneNumber = number;
		sceneTitle = title;
		this.flapping = animation;
	}
}