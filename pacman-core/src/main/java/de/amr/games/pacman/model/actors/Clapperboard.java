/*
Copyright (c) 2021-2023 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.model.actors;

import java.util.Optional;

import de.amr.games.pacman.lib.anim.Animated;

/**
 * The clapperboard used in the intermission scenes.
 * 
 * @author Armin Reichert
 */
public class Clapperboard extends Entity {

	private String number;
	private String text;
	private Animated animation;

	public Clapperboard(String number, String text) {
		this.number = number;
		this.text = text;
	}

	public String number() {
		return number;
	}

	public String text() {
		return text;
	}

	public void setAnimation(Animated animation) {
		this.animation = animation;
	}

	public Optional<Animated> animation() {
		return Optional.ofNullable(animation);
	}
}