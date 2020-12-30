package de.amr.games.pacman.ui.swing;

class AssetsException extends RuntimeException {

	public AssetsException(String message, Object... args) {
		super("Assets exception: " + String.format(message, args));
	}
}