/*
MIT License

Copyright (c) 2021-22 Armin Reichert

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
package de.amr.games.pacman.model.mspacman;

import static de.amr.games.pacman.lib.TickTimer.secToTicks;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.amr.games.pacman.event.GameEventType;
import de.amr.games.pacman.event.GameEvents;
import de.amr.games.pacman.lib.Direction;
import de.amr.games.pacman.lib.TickTimer;
import de.amr.games.pacman.lib.V2i;
import de.amr.games.pacman.model.common.GameLevel;
import de.amr.games.pacman.model.common.GameModel;
import de.amr.games.pacman.model.common.GameVariant;
import de.amr.games.pacman.model.common.world.ArcadeWorld;
import de.amr.games.pacman.model.common.world.HorizontalPortal;
import de.amr.games.pacman.model.common.world.Route;
import de.amr.games.pacman.model.common.world.World;

/**
 * Model of the Ms. Pac-Man game.
 * <p>
 * TODO: are the level data except for the bonus symbols the same as in Pac-Man?
 * <p>
 * See https://gamefaqs.gamespot.com/arcade/583976-ms-pac-man/faqs/1298
 * 
 * @author Armin Reichert
 */
public class MsPacManGame extends GameModel {

	private static final Logger LOGGER = LogManager.getFormatterLogger();

	public static final int CHERRIES = 0;
	public static final int STRAWBERRY = 1;
	public static final int PEACH = 2;
	public static final int PRETZEL = 3;
	public static final int APPLE = 4;
	public static final int PEAR = 5;
	public static final int BANANA = 6;

	private static final String[] BONUS_NAMES = { "CHERRIES", "STRAWBERRY", "PEACH", "PRETZEL", "APPLE", "PEAR",
			"BANANA" };

	public static String bonusName(int symbol) {
		return BONUS_NAMES[symbol];
	}

	protected static final int[] BONUS_VALUES = { 100, 200, 500, 700, 1000, 2000, 5000 };

	private static final Object[][] LEVEL_DATA = {
	/*@formatter:off*/
	/* 1*/ {CHERRIES,    80, 75, 40,  20,  80, 10,  85,  90, 50, 6, 5},
	/* 2*/ {STRAWBERRY,  90, 85, 45,  30,  90, 15,  95,  95, 55, 5, 5},
	/* Intermission scene 1: "They Meet" */
	/* 3*/ {PEACH,       90, 85, 45,  40,  90, 20,  95,  95, 55, 4, 5},
	/* 4*/ {PRETZEL,     90, 85, 45,  40,  90, 20,  95,  95, 55, 3, 5},
	/* 5*/ {APPLE,      100, 95, 50,  40, 100, 20, 105, 100, 60, 2, 5},
	/* Intermission scene 2: "The Chase" */
	/* 6*/ {PEAR,       100, 95, 50,  50, 100, 25, 105, 100, 60, 5, 5},
	/* 7*/ {BANANA,     100, 95, 50,  50, 100, 25, 105, 100, 60, 2, 5},
	/* 8*/ {BANANA,     100, 95, 50,  50, 100, 25, 105, 100, 60, 2, 5},
	/* 9*/ {BANANA,     100, 95, 50,  60, 100, 30, 105, 100, 60, 1, 3},
	/* Intermission scene 3: "Junior" */
	/*10*/ {BANANA,     100, 95, 50,  60, 100, 30, 105, 100, 60, 4, 5},
	/*11*/ {BANANA,     100, 95, 50,  60, 100, 30, 105, 100, 60, 2, 5},
	/*12*/ {BANANA,     100, 95, 50,  80, 100, 40, 105, 100, 60, 1, 3},
	/*13*/ {BANANA,     100, 95, 50,  80, 100, 40, 105, 100, 60, 1, 3},
	/* Intermission scene 3: "Junior" */
	/*14*/ {BANANA,     100, 95, 50,  80, 100, 40, 105, 100, 60, 3, 5},
	/*15*/ {BANANA,     100, 95, 50, 100, 100, 50, 105, 100, 60, 1, 3},
	/*16*/ {BANANA,     100, 95, 50, 100, 100, 50, 105, 100, 60, 1, 3},
	/*17*/ {BANANA,     100, 95, 50, 100, 100, 50, 105,   0,  0, 0, 0},
	/* Intermission scene 3: "Junior" */
	/*18*/ {BANANA,     100, 95, 50, 100, 100, 50, 105, 100, 60, 1, 3},
	/*19*/ {BANANA,     100, 95, 50, 120, 100, 60, 105,   0,  0, 0, 0},
	/*20*/ {BANANA,     100, 95, 50, 120, 100, 60, 105,   0,  0, 0, 0},
	/*21*/ {BANANA,      90, 95, 50, 120, 100, 60, 105,   0,  0, 0, 0},
	/*@formatter:on*/
	};

	private static final byte[][] MAP1 = {
			//@formatter:off
			{1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,},
			{1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,},
			{1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,},
			{1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,},
			{1,3,3,3,3,3,3,1,1,3,3,3,3,3,3,3,3,3,3,1,1,3,3,3,3,3,3,1,},
			{1,4,1,1,1,1,3,1,1,3,1,1,1,1,1,1,1,1,3,1,1,3,1,1,1,1,4,1,},
			{1,3,1,1,1,1,3,1,1,3,1,1,1,1,1,1,1,1,3,1,1,3,1,1,1,1,3,1,},
			{1,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,1,},
			{1,1,1,3,1,1,3,1,1,1,1,1,3,1,1,3,1,1,1,1,1,3,1,1,3,1,1,1,},
			{0,0,1,3,1,1,3,1,1,1,1,1,3,1,1,3,1,1,1,1,1,3,1,1,3,1,0,0,},
			{1,1,1,3,1,1,3,1,1,1,1,1,3,1,1,3,1,1,1,1,1,3,1,1,3,1,1,1,},
			{2,2,2,3,1,1,3,3,3,3,3,3,3,1,1,3,3,3,3,3,3,3,1,1,3,2,2,2,},
			{1,1,1,3,1,1,1,1,1,0,1,1,1,1,1,1,1,1,0,1,1,1,1,1,3,1,1,1,},
			{0,0,1,3,1,1,1,1,1,0,1,1,1,1,1,1,1,1,0,1,1,1,1,1,3,1,0,0,},
			{0,0,1,3,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,3,1,0,0,},
			{0,0,1,3,1,1,1,1,1,0,1,1,1,0,0,1,1,1,0,1,1,1,1,1,3,1,0,0,},
			{0,0,1,3,1,1,1,1,1,0,1,0,0,0,0,0,0,1,0,1,1,1,1,1,3,1,0,0,},
			{0,0,1,3,1,1,0,0,0,0,1,0,0,0,0,0,0,1,0,0,0,0,1,1,3,1,0,0,},
			{0,0,1,3,1,1,0,1,1,0,1,0,0,0,0,0,0,1,0,1,1,0,1,1,3,1,0,0,},
			{1,1,1,3,1,1,0,1,1,0,1,1,1,1,1,1,1,1,0,1,1,0,1,1,3,1,1,1,},
			{2,2,2,3,0,0,0,1,1,0,0,0,0,0,0,0,0,0,0,1,1,0,0,0,3,2,2,2,},
			{1,1,1,3,1,1,1,1,1,1,1,1,0,1,1,0,1,1,1,1,1,1,1,1,3,1,1,1,},
			{0,0,1,3,1,1,1,1,1,1,1,1,0,1,1,0,1,1,1,1,1,1,1,1,3,1,0,0,},
			{0,0,1,3,3,3,3,3,3,3,0,0,0,1,1,0,0,0,3,3,3,3,3,3,3,1,0,0,},
			{0,0,1,3,1,1,1,1,1,3,1,1,1,1,1,1,1,1,3,1,1,1,1,1,3,1,0,0,},
			{1,1,1,3,1,1,1,1,1,3,1,1,1,1,1,1,1,1,3,1,1,1,1,1,3,1,1,1,},
			{1,3,3,3,3,3,3,3,3,3,3,3,3,0,0,3,3,3,3,3,3,3,3,3,3,3,3,1,},
			{1,3,1,1,1,1,3,1,1,1,1,1,3,1,1,3,1,1,1,1,1,3,1,1,1,1,3,1,},
			{1,3,1,1,1,1,3,1,1,1,1,1,3,1,1,3,1,1,1,1,1,3,1,1,1,1,3,1,},
			{1,3,1,1,1,1,3,1,1,3,3,3,3,1,1,3,3,3,3,1,1,3,1,1,1,1,3,1,},
			{1,4,1,1,1,1,3,1,1,3,1,1,1,1,1,1,1,1,3,1,1,3,1,1,1,1,4,1,},
			{1,3,1,1,1,1,3,1,1,3,1,1,1,1,1,1,1,1,3,1,1,3,1,1,1,1,3,1,},
			{1,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,1,},
			{1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,},
			{1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,},
			{1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,},
			//@formatter:on
	};

	private static final byte[][] MAP2 = {
			//@formatter:off
			{1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,},
			{1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,},
			{1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,},
			{1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,},
			{2,2,2,2,2,2,0,1,1,3,3,3,3,3,3,3,3,3,3,1,1,0,2,2,2,2,2,2,},
			{1,1,1,1,1,1,0,1,1,3,1,1,1,1,1,1,1,1,3,1,1,0,1,1,1,1,1,1,},
			{1,1,1,1,1,1,0,1,1,3,1,1,1,1,1,1,1,1,3,1,1,0,1,1,1,1,1,1,},
			{1,4,3,3,3,3,3,3,3,3,3,3,3,1,1,3,3,3,3,3,3,3,3,3,3,3,4,1,},
			{1,3,1,1,1,1,1,1,1,3,1,1,3,1,1,3,1,1,3,1,1,1,1,1,1,1,3,1,},
			{1,3,1,1,1,1,1,1,1,3,1,1,3,1,1,3,1,1,3,1,1,1,1,1,1,1,3,1,},
			{1,3,1,1,3,3,3,3,3,3,1,1,3,1,1,3,1,1,3,3,3,3,3,3,1,1,3,1,},
			{1,3,1,1,3,1,1,1,1,0,1,1,3,3,3,3,1,1,0,1,1,1,1,3,1,1,3,1,},
			{1,3,1,1,3,1,1,1,1,0,1,1,1,1,1,1,1,1,0,1,1,1,1,3,1,1,3,1,},
			{1,3,3,3,3,3,3,1,1,0,1,1,1,1,1,1,1,1,0,1,1,3,3,3,3,3,3,1,},
			{1,1,1,1,1,1,3,1,1,0,0,0,0,0,0,0,0,0,0,1,1,3,1,1,1,1,1,1,},
			{1,1,1,1,1,1,3,1,1,0,1,1,1,0,0,1,1,1,0,1,1,3,1,1,1,1,1,1,},
			{1,3,3,3,3,3,3,1,1,0,1,0,0,0,0,0,0,1,0,1,1,3,3,3,3,3,3,1,},
			{1,3,1,1,1,1,3,1,1,0,1,0,0,0,0,0,0,1,0,1,1,3,1,1,1,1,3,1,},
			{1,3,1,1,1,1,3,0,0,0,1,0,0,0,0,0,0,1,0,0,0,3,1,1,1,1,3,1,},
			{1,3,3,3,1,1,3,1,1,0,1,1,1,1,1,1,1,1,0,1,1,3,1,1,3,3,3,1,},
			{1,1,1,3,1,1,3,1,1,0,0,0,0,0,0,0,0,0,0,1,1,3,1,1,3,1,1,1,},
			{0,0,1,3,1,1,3,1,1,1,1,0,1,1,1,1,0,1,1,1,1,3,1,1,3,1,0,0,},
			{0,0,1,3,1,1,3,1,1,1,1,0,1,1,1,1,0,1,1,1,1,3,1,1,3,1,0,0,},
			{0,0,1,3,3,3,3,3,3,3,3,3,1,1,1,1,3,3,3,3,3,3,3,3,3,1,0,0,},
			{0,0,1,3,1,1,1,1,1,1,1,3,1,1,1,1,3,1,1,1,1,1,1,1,3,1,0,0,},
			{1,1,1,3,1,1,1,1,1,1,1,3,1,1,1,1,3,1,1,1,1,1,1,1,3,1,1,1,},
			{2,2,2,3,3,3,3,1,1,3,3,3,0,0,0,0,3,3,3,1,1,3,3,3,3,2,2,2,},
			{1,1,1,3,1,1,3,1,1,3,1,1,1,1,1,1,1,1,3,1,1,3,1,1,3,1,1,1,},
			{1,1,1,3,1,1,3,1,1,3,1,1,1,1,1,1,1,1,3,1,1,3,1,1,3,1,1,1,},
			{1,4,3,3,1,1,3,3,3,3,3,3,3,1,1,3,3,3,3,3,3,3,1,1,3,3,4,1,},
			{1,3,1,1,1,1,3,1,1,1,1,1,3,1,1,3,1,1,1,1,1,3,1,1,1,1,3,1,},
			{1,3,1,1,1,1,3,1,1,1,1,1,3,1,1,3,1,1,1,1,1,3,1,1,1,1,3,1,},
			{1,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,1,},
			{1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,},
			{1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,},
			{1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,},
			//@formatter:on
	};

	private static final byte[][] MAP3 = {
			//@formatter:off
			{1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,},
			{1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,},
			{1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,},
			{1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,},
			{1,3,3,3,3,3,3,3,3,3,1,1,3,3,3,3,1,1,3,3,3,3,3,3,3,3,3,1,},
			{1,3,1,1,1,1,1,1,1,3,1,1,3,1,1,3,1,1,3,1,1,1,1,1,1,1,3,1,},
			{1,4,1,1,1,1,1,1,1,3,1,1,3,1,1,3,1,1,3,1,1,1,1,1,1,1,4,1,},
			{1,3,1,1,3,3,3,3,3,3,3,3,3,1,1,3,3,3,3,3,3,3,3,3,1,1,3,1,},
			{1,3,1,1,3,1,1,3,1,1,1,1,3,1,1,3,1,1,1,1,3,1,1,3,1,1,3,1,},
			{1,3,3,3,3,1,1,3,1,1,1,1,3,1,1,3,1,1,1,1,3,1,1,3,3,3,3,1,},
			{1,1,1,1,3,1,1,3,1,1,1,1,3,1,1,3,1,1,1,1,3,1,1,3,1,1,1,1,},
			{1,1,1,1,3,1,1,3,3,3,3,3,3,3,3,3,3,3,3,3,3,1,1,3,1,1,1,1,},
			{2,3,3,3,3,1,1,1,1,0,1,1,1,1,1,1,1,1,0,1,1,1,1,3,3,3,3,2,},
			{1,3,1,1,0,1,1,1,1,0,1,1,1,1,1,1,1,1,0,1,1,1,1,0,1,1,3,1,},
			{1,3,1,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1,1,3,1,},
			{1,3,1,1,1,1,0,1,1,0,1,1,1,0,0,1,1,1,0,1,1,0,1,1,1,1,3,1,},
			{1,3,1,1,1,1,0,1,1,0,1,0,0,0,0,0,0,1,0,1,1,0,1,1,1,1,3,1,},
			{1,3,0,0,0,0,0,1,1,0,1,0,0,0,0,0,0,1,0,1,1,0,0,0,0,0,3,1,},
			{1,3,1,1,0,1,1,1,1,0,1,0,0,0,0,0,0,1,0,1,1,1,1,0,1,1,3,1,},
			{1,3,1,1,0,1,1,1,1,0,1,1,1,1,1,1,1,1,0,1,1,1,1,0,1,1,3,1,},
			{1,3,1,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1,1,3,1,},
			{1,3,1,1,1,1,0,1,1,1,1,1,0,1,1,0,1,1,1,1,1,0,1,1,1,1,3,1,},
			{1,3,1,1,1,1,0,1,1,1,1,1,0,1,1,0,1,1,1,1,1,0,1,1,1,1,3,1,},
			{1,3,3,3,3,3,3,1,1,3,3,3,3,1,1,3,3,3,3,1,1,3,3,3,3,3,3,1,},
			{1,1,1,3,1,1,3,1,1,3,1,1,1,1,1,1,1,1,3,1,1,3,1,1,3,1,1,1,},
			{1,1,1,3,1,1,3,1,1,3,1,1,1,1,1,1,1,1,3,1,1,3,1,1,3,1,1,1,},
			{1,4,3,3,1,1,3,3,3,3,3,3,3,0,0,3,3,3,3,3,3,3,1,1,3,3,4,1,},
			{1,3,1,1,1,1,3,1,1,1,1,1,3,1,1,3,1,1,1,1,1,3,1,1,1,1,3,1,},
			{1,3,1,1,1,1,3,1,1,1,1,1,3,1,1,3,1,1,1,1,1,3,1,1,1,1,3,1,},
			{1,3,3,3,3,3,3,1,1,3,3,3,3,1,1,3,3,3,3,1,1,3,3,3,3,3,3,1,},
			{1,3,1,1,1,1,3,1,1,3,1,1,1,1,1,1,1,1,3,1,1,3,1,1,1,1,3,1,},
			{1,3,1,1,1,1,3,1,1,3,1,1,1,1,1,1,1,1,3,1,1,3,1,1,1,1,3,1,},
			{1,3,3,3,3,3,3,1,1,3,3,3,3,3,3,3,3,3,3,1,1,3,3,3,3,3,3,1,},
			{1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,},
			{1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,},
			{1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,},
			//@formatter:on
	};

	private static final byte[][] MAP4 = {
			//@formatter:off
			{1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,},
			{1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,},
			{1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,},
			{1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,},
			{1,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,1,},
			{1,3,1,1,3,1,1,1,1,3,1,1,1,1,1,1,1,1,3,1,1,1,1,3,1,1,3,1,},
			{1,4,1,1,3,1,1,1,1,3,1,1,1,1,1,1,1,1,3,1,1,1,1,3,1,1,4,1,},
			{1,3,1,1,3,1,1,1,1,3,1,1,3,3,3,3,1,1,3,1,1,1,1,3,1,1,3,1,},
			{1,3,1,1,3,3,3,3,3,3,1,1,3,1,1,3,1,1,3,3,3,3,3,3,1,1,3,1,},
			{1,3,1,1,1,1,3,1,1,3,1,1,3,1,1,3,1,1,3,1,1,3,1,1,1,1,3,1,},
			{1,3,1,1,1,1,3,1,1,3,1,1,3,1,1,3,1,1,3,1,1,3,1,1,1,1,3,1,},
			{1,3,3,3,3,3,3,1,1,3,3,3,3,1,1,3,3,3,3,1,1,3,3,3,3,3,3,1,},
			{1,1,1,3,1,1,1,1,1,1,1,1,0,1,1,0,1,1,1,1,1,1,1,1,3,1,1,1,},
			{0,0,1,3,1,1,1,1,1,1,1,1,0,1,1,0,1,1,1,1,1,1,1,1,3,1,0,0,},
			{0,0,1,3,3,3,3,1,1,0,0,0,0,0,0,0,0,0,0,1,1,3,3,3,3,1,0,0,},
			{1,1,1,0,1,1,3,1,1,0,1,1,1,0,0,1,1,1,0,1,1,3,1,1,0,1,1,1,},
			{2,2,2,0,1,1,3,1,1,0,1,0,0,0,0,0,0,1,0,1,1,3,1,1,0,2,2,2,},
			{1,1,1,1,1,1,3,0,0,0,1,0,0,0,0,0,0,1,0,0,0,3,1,1,1,1,1,1,},
			{1,1,1,1,1,1,3,1,1,0,1,0,0,0,0,0,0,1,0,1,1,3,1,1,1,1,1,1,},
			{2,2,2,0,1,1,3,1,1,0,1,1,1,1,1,1,1,1,0,1,1,3,1,1,0,2,2,2,},
			{1,1,1,0,1,1,3,1,1,0,0,0,0,0,0,0,0,0,0,1,1,3,1,1,0,1,1,1,},
			{0,0,1,3,3,3,3,1,1,1,1,1,0,1,1,0,1,1,1,1,1,3,3,3,3,1,0,0,},
			{0,0,1,3,1,1,3,1,1,1,1,1,0,1,1,0,1,1,1,1,1,3,1,1,3,1,0,0,},
			{0,0,1,3,1,1,3,3,3,3,0,0,0,1,1,0,0,0,3,3,3,3,1,1,3,1,0,0,},
			{0,0,1,3,1,1,1,1,1,3,1,1,0,1,1,0,1,1,3,1,1,1,1,1,3,1,0,0,},
			{1,1,1,3,1,1,1,1,1,3,1,1,0,1,1,0,1,1,3,1,1,1,1,1,3,1,1,1,},
			{1,3,3,3,3,3,3,3,3,3,1,1,0,0,0,0,1,1,3,3,3,3,3,3,3,3,3,1,},
			{1,3,1,1,1,1,3,1,1,3,1,1,1,1,1,1,1,1,3,1,1,3,1,1,1,1,3,1,},
			{1,3,1,1,1,1,3,1,1,3,1,1,1,1,1,1,1,1,3,1,1,3,1,1,1,1,3,1,},
			{1,3,1,1,3,3,3,1,1,3,3,3,3,3,3,3,3,3,3,1,1,3,3,3,1,1,3,1,},
			{1,4,1,1,3,1,1,1,1,1,1,1,3,1,1,3,1,1,1,1,1,1,1,3,1,1,4,1,},
			{1,3,1,1,3,1,1,1,1,1,1,1,3,1,1,3,1,1,1,1,1,1,1,3,1,1,3,1,},
			{1,3,3,3,3,3,3,3,3,3,3,3,3,1,1,3,3,3,3,3,3,3,3,3,3,3,3,1,},
			{1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,},
			{1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,},
			{1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,},
			//@formatter:on
	};

	public static ArcadeWorld createWorld(int number) {
		return switch (number) {
		case 1 -> new ArcadeWorld(MAP1);
		case 2 -> new ArcadeWorld(MAP2);
		case 3 -> new ArcadeWorld(MAP3);
		case 4 -> new ArcadeWorld(MAP4);
		default -> throw new IllegalArgumentException();
		};
	}

	public static int mazeNumber(int levelNumber) {
		return switch (levelNumber) {
		case 1, 2 -> 1;
		case 3, 4, 5 -> 2;
		case 6, 7, 8, 9 -> 3;
		case 10, 11, 12, 13 -> 4;
		default -> (levelNumber - 14) % 8 < 4 ? 5 : 6;
		};
	}

	private static GameLevel createLevel(int number) {
		int numLevels = LEVEL_DATA.length;
		var level = new GameLevel(number, number <= numLevels ? LEVEL_DATA[number - 1] : LEVEL_DATA[numLevels - 1]);
		int mapNumber = switch (level.number) {
		case 1, 2 -> 1;
		case 3, 4, 5 -> 2;
		case 6, 7, 8, 9 -> 3;
		case 10, 11, 12, 13 -> 4;
		default -> (level.number - 14) % 8 < 4 ? 3 : 4;
		};
		level.world = createWorld(mapNumber);
		if (level.number >= 8) {
			level.bonusSymbol = rnd.nextInt(7);
		}
		level.mazeNumber = mazeNumber(number);
		level.pacStarvingTimeLimit = (int) secToTicks(level.number < 5 ? 4 : 3);
		level.globalDotLimits = new int[] { Integer.MAX_VALUE, 7, 17, Integer.MAX_VALUE };
		level.privateDotLimits = switch (level.number) {
		case 1 -> new int[] { 0, 0, 30, 60 };
		case 2 -> new int[] { 0, 0, 0, 50 };
		default -> new int[] { 0, 0, 0, 0 };
		};
		return level;
	}

	private static final Random rnd = new Random();

	private final MovingBonus movingBonus;

	/**
	 * The Ms. Pac-Man game.
	 * <p>
	 * Clyde, the orange ghost has become a transvestite and calls himself "Sue" now. Pac-Man wears woman's clothes and
	 * calls himself "Ms. Pac-Man" (pronoun she/her). The bonus probably couldn't stand all this madness anymore, became
	 * an alcoholic and tumbles through the maze, starting on a random portal, walking around the ghost house and leaving
	 * the maze at a portal on the opposite side.
	 * <p>
	 * What a freak show!
	 * <p>
	 * There are 4 different maps used by the 6 different mazes. Up to level 13, the used mazes are:
	 * <ul>
	 * <li>Maze #1: pink maze, white dots (level 1-2)
	 * <li>Maze #2: light blue maze, yellow dots (level 3-5)
	 * <li>Maze #3: orange maze, red dots (level 6-9)
	 * <li>Maze #4: dark blue maze, white dots (level 10-13)
	 * </ul>
	 * From level 14 on, the maze alternates every 4th level between maze #5 and maze #6.
	 * <ul>
	 * <li>Maze #5: pink maze, cyan dots (same map as maze #3)
	 * <li>Maze #6: orange maze, white dots (same map as maze #4)
	 * </ul>
	 */
	public MsPacManGame() {
		super(GameVariant.MS_PACMAN, "Ms. Pac-Man", "Blinky", "Pinky", "Inky", "Sue");
		movingBonus = new MovingBonus();
		scores.setHiscoreFile(new File(System.getProperty("user.home"), "highscore-ms_pacman.xml"));
		setLevel(1);
	}

	@Override
	public ArcadeWorld world() {
		return (ArcadeWorld) level.world;
	}

	@Override
	public MovingBonus bonus() {
		return movingBonus;
	}

	@Override
	public void setLevel(int levelNumber) {
		if (levelNumber < 1) {
			throw new IllegalArgumentException("Level number must be at least 1, but is: " + levelNumber);
		}
		level = createLevel(levelNumber);
		initGhosts();
		ghostsKilledByEnergizer = 0;
		scores.gameScore.levelNumber = levelNumber;
	}

	@Override
	protected void onBonusReached() {
		var route = computeBonusRoute(level.world);
		LOGGER.info("Bonus route: %s", route);
		if (!route.tiles().isEmpty()) {
			movingBonus.setRoute(route);
			movingBonus.setEdible(level.bonusSymbol, BONUS_VALUES[level.bonusSymbol], TickTimer.INDEFINITE);
			GameEvents.publish(GameEventType.BONUS_GETS_ACTIVE, movingBonus.tile());
		}
	}

	private Route computeBonusRoute(World world) {
		List<V2i> tiles = new ArrayList<>();
		Direction routeDir = rnd.nextBoolean() ? Direction.LEFT : Direction.RIGHT;
		int numPortals = world.portals().size();
		if (numPortals > 0) {
			var entryPortal = (HorizontalPortal) world.portals().get(rnd.nextInt(numPortals));
			var exitPortal = (HorizontalPortal) world.portals().get(rnd.nextInt(numPortals));
			var houseEntry = world.ghostHouse().entryTile();
			int houseHeight = world.ghostHouse().size().y();
			tiles.add(routeDir == Direction.RIGHT ? entryPortal.leftTunnelEnd() : entryPortal.rightTunnelEnd());
			tiles.add(houseEntry);
			tiles.add(houseEntry.plus(0, houseHeight + 2));
			tiles.add(houseEntry);
			tiles.add(routeDir == Direction.RIGHT ? exitPortal.rightTunnelEnd() : exitPortal.leftTunnelEnd());
		}
		return new Route(tiles, routeDir);
	}
}