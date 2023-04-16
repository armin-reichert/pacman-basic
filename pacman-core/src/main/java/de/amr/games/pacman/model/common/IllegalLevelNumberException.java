package de.amr.games.pacman.model.common;

/**
 * @author Armin Reichert
 */
public class IllegalLevelNumberException extends IllegalArgumentException {

	public IllegalLevelNumberException(int number) {
		super("Illegal level number '%d' (Allowed values: 1-)".formatted(number));
	}
}