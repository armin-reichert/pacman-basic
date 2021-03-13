package de.amr.games.pacman.model.common;

import de.amr.games.pacman.ui.animation.TimedSequence;

/**
 * The flap used in the intermission scenes.
 * 
 * @author Armin Reichert
 */
public class Flap extends GameEntity {

	public final int sceneNumber;
	public final String sceneTitle;

	public TimedSequence<?> flapping;

	public Flap(int number, String title, TimedSequence<?> animation) {
		sceneNumber = number;
		sceneTitle = title;
		this.flapping = animation;
	}
}