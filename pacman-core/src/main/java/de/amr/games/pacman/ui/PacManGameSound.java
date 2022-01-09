/*
MIT License

Copyright (c) 2021 Armin Reichert

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
package de.amr.games.pacman.ui;

import java.util.Arrays;
import java.util.List;

/**
 * Constants for the sounds used in the Pac-Man and Ms. Pac-Man games.
 * 
 * @author Armin Reichert
 */
public enum PacManGameSound {

	//@formatter:off
	BONUS_EATEN, 
	CREDIT, 
	EXTRA_LIFE, 
	GAME_READY, 
	GHOST_EATEN, 
	GHOST_RETURNING,
	GHOST_SIREN_1, 
	GHOST_SIREN_2, 
	GHOST_SIREN_3, 
	GHOST_SIREN_4, 
	INTERMISSION_1, 
	INTERMISSION_2,
	INTERMISSION_3,
	PACMAN_DEATH, 
	PACMAN_MUNCH, 
	PACMAN_POWER;
	//@formatter:on

	public static final List<PacManGameSound> SIRENS = Arrays.asList(GHOST_SIREN_1, GHOST_SIREN_2, GHOST_SIREN_3,
			GHOST_SIREN_4);
}