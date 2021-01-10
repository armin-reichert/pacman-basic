package de.amr.games.pacman.ui.swing;

public class AssetsException extends RuntimeException {

	public AssetsException(String message, Object... args) {
		super("Assets exception: " + String.format(message, args));
	}
}