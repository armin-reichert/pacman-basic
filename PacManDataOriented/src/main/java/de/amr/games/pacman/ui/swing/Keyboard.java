package de.amr.games.pacman.ui.swing;

import java.awt.Component;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.BitSet;

/**
 * Keyboard handler.
 * 
 * @author Armin Reichert
 */
public class Keyboard {

	private final BitSet pressedKeys = new BitSet(256);

	public Keyboard(Component component) {
		component.addKeyListener(new KeyAdapter() {

			@Override
			public void keyPressed(KeyEvent e) {
				pressedKeys.set(e.getKeyCode());
			}

			@Override
			public void keyReleased(KeyEvent e) {
				pressedKeys.clear(e.getKeyCode());
			}
		});
	}

	public boolean keyPressed(String keySpec) {
		return pressedKeys.get(keyCode(keySpec));
	}

	public void clearKey(String keySpec) {
		pressedKeys.clear(keyCode(keySpec));
	}

	private int keyCode(String keySpec) {
		keySpec = keySpec.toLowerCase();
		if (keySpec.length() == 1) {
			int c = keySpec.charAt(0);
			int index = "abcdefghijklmnopqrstuvwxyz".indexOf(c);
			if (index != -1) {
				return KeyEvent.VK_A + index;
			}
			index = "0123456789".indexOf(c);
			if (index != -1) {
				return KeyEvent.VK_0 + index;
			}
		}
		switch (keySpec) {
		case "up":
			return KeyEvent.VK_UP;
		case "down":
			return KeyEvent.VK_DOWN;
		case "left":
			return KeyEvent.VK_LEFT;
		case "right":
			return KeyEvent.VK_RIGHT;
		case "escape":
			return KeyEvent.VK_ESCAPE;
		case "space":
			return KeyEvent.VK_SPACE;
		case "plus":
			return KeyEvent.VK_PLUS;
		case "minus":
			return KeyEvent.VK_MINUS;
		default:
			throw new IllegalArgumentException(String.format("Unknown key specification: %s", keySpec));
		}
	}
}