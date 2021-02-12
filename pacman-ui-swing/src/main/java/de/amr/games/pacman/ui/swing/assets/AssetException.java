package de.amr.games.pacman.ui.swing.assets;

public class AssetException extends RuntimeException {

	public AssetException(String message, Object... args) {
		super("Asset exception: " + String.format(message, args));
	}
}