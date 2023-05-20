package de.amr.games.pacman.model;

/**
 * @author Armin Reichert
 */
public class IllegalGameVariantException extends IllegalArgumentException {

	public IllegalGameVariantException(GameVariant variant) {
		super(String.format("Illegal game variant value '%s'", variant));
	}
}