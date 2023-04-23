package de.amr.games.pacman.model;

/**
 * @author Armin Reichert
 */
public class IllegalGhostIDException extends IllegalArgumentException {

	public IllegalGhostIDException(int id) {
		super("Illegal ghost ID value '%d' (Allowed values: 0-3)".formatted(id));
	}
}