/*
Copyright (c) 2021-2023 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.model.actors;

/**
 * @author Armin Reichert
 */
public interface PacAnimations {

	public static final String PAC_MUNCHING = "munching";
	public static final String PAC_DYING = "dying";

	// Pac-Man game specific
	public static final String BIG_PACMAN = "bigpacman";

	// Ms. Pac-Man game specific
	public static final String HUSBAND_MUNCHING = "husband_munching";

	void select(String name);

	void startSelected();

	void stopSelected();

	void resetSelected();

	Object currentSprite();
}