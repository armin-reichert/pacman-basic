/*
MIT License

Copyright (c) 2021-2023 Armin Reichert

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
 */
package de.amr.games.pacman.model.actors;

import java.util.Optional;

import de.amr.games.pacman.lib.anim.Animated;
import de.amr.games.pacman.lib.math.Vector2f;

/**
 * The clapperboard used in the intermission scenes.
 * 
 * @author Armin Reichert
 */
public class Clapperboard {

	private Vector2f position = Vector2f.ZERO;
	private boolean visible;
	private int sceneNumber;
	private String sceneTitle;
	private Animated animation;

	public Clapperboard(int sceneNumber, String sceneTitle) {
		this.sceneNumber = sceneNumber;
		this.sceneTitle = sceneTitle;
	}

	public Vector2f position() {
		return position;
	}

	public void setPosition(float x, float y) {
		this.position = new Vector2f(x, y);
	}

	public void setVisible(boolean visible) {
		this.visible = visible;
	}

	public boolean isVisible() {
		return visible;
	}

	public int sceneNumber() {
		return sceneNumber;
	}

	public String sceneTitle() {
		return sceneTitle;
	}

	public void setAnimation(Animated animation) {
		this.animation = animation;
	}

	public Optional<Animated> animation() {
		return Optional.ofNullable(animation);
	}
}