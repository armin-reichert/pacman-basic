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
				if (e.getKeyCode() != 0 && e.getKeyCode() < 256) {
					pressedKeys.set(e.getKeyCode());
				}
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

	public boolean anyKeyPressed() {
		return !pressedKeys.isEmpty();
	}
	
	public void clear() {
		pressedKeys.clear();
	}

	public void clearKey(String keySpec) {
		pressedKeys.clear(keyCode(keySpec));
	}

	private int keyCode(String keySpec) {
		if (keySpec.length() == 1) {
			int c = keySpec.charAt(0);
			int index = "ABCDEFGHIJKLMNOPQRSTUVWXYZ".indexOf(c);
			if (index != -1) {
				return KeyEvent.VK_A + index;
			}
			index = "0123456789".indexOf(c);
			if (index != -1) {
				return KeyEvent.VK_0 + index;
			}
		}
		switch (keySpec) {
		case "Up":
			return KeyEvent.VK_UP;
		case "Down":
			return KeyEvent.VK_DOWN;
		case "Left":
			return KeyEvent.VK_LEFT;
		case "Right":
			return KeyEvent.VK_RIGHT;
		case "Esc":
			return KeyEvent.VK_ESCAPE;
		case "Space":
			return KeyEvent.VK_SPACE;
		case "Plus":
			return KeyEvent.VK_PLUS;
		case "Minus":
			return KeyEvent.VK_MINUS;
		default:
			throw new IllegalArgumentException(String.format("Unknown key specification: %s", keySpec));
		}
	}
}