/*
Copyright (c) 2021-2023 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.model.actors;

/**
 * The clapperboard used in the intermission scenes.
 * 
 * @author Armin Reichert
 */
public class Clapperboard extends Entity {

	private String number;
	private String text;

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
}