/*
Copyright (c) 2021-2023 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.lib.anim;

/**
 * @author Armin Reichert
 *
 */
public class Pulse extends SimpleAnimation<Boolean> {

	public Pulse(int ticks, boolean firstValue) {
		super(firstValue, !firstValue);
		setFrameDuration(ticks);
		repeatForever();
	}
}
