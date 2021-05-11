package de.amr.games.pacman.model.mspacman.entities;

import de.amr.games.pacman.model.common.GameEntity;

/**
 * The flap used in the intermission scenes.
 * 
 * @author Armin Reichert
 */
public class Flap extends GameEntity {

	public final int sceneNumber;
	public final String sceneTitle;

	public Flap(int number, String title) {
		sceneNumber = number;
		sceneTitle = title;
	}
}