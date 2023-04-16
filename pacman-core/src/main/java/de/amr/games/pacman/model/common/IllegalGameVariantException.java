package de.amr.games.pacman.model.common;

/**
 * @author Armin Reichert
 */
public class IllegalGameVariantException extends IllegalArgumentException {

	public IllegalGameVariantException(GameVariant variant) {
		super("Illegal game variant value '%s'".formatted(variant));
	}
}