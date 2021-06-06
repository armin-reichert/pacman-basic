package de.amr.games.pacman.model.common;

/**
 * Game variants that can be played.
 * 
 * @author Armin Reichert
 */
public enum GameVariant {

	MS_PACMAN, PACMAN;

	public static int numValues() {
		return values().length;
	}

	public GameVariant pred() {
		return values()[ordinal() == 0 ? numValues() - 1 : ordinal() - 1];
	}

	public GameVariant succ() {
		return values()[ordinal() == numValues() - 1 ? 0 : ordinal() + 1];
	}
}