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

import static de.amr.games.pacman.lib.NavigationPoint.np;
import static de.amr.games.pacman.model.common.actors.Ghost.ID_CYAN_GHOST;
import static de.amr.games.pacman.model.common.actors.Ghost.ID_ORANGE_GHOST;
import static de.amr.games.pacman.model.common.actors.Ghost.ID_PINK_GHOST;
import static de.amr.games.pacman.model.common.actors.Ghost.ID_RED_GHOST;

import java.util.ArrayList;
import java.util.List;

import de.amr.games.pacman.event.GameEventType;
import de.amr.games.pacman.event.GameEvents;
import de.amr.games.pacman.lib.Direction;
import de.amr.games.pacman.lib.NavigationPoint;
import de.amr.games.pacman.lib.TickTimer;
import de.amr.games.pacman.model.common.GameLevel;
import de.amr.games.pacman.model.common.GameModel;
import de.amr.games.pacman.model.common.GameVariant;
import de.amr.games.pacman.model.common.actors.Bonus;
import de.amr.games.pacman.model.common.actors.Ghost;
import de.amr.games.pacman.model.common.actors.Pac;
import de.amr.games.pacman.model.common.world.ArcadeWorld;
import de.amr.games.pacman.model.common.world.HorizontalPortal;

/**
 * The Ms. Pac-Man game.
 * <p>
 * Clyde, the orange ghost, has become a transvestite and calls himself "Sue" now (pronouns: what/thefuck). Pac-Man too
 * wears women's dresses and calls himself "Ms. Pac-Man" (pronouns: mad/world). The bonus guy couldn't stand all this
 * madness anymore, became an alcoholic and tumbles through the maze, starting on a random portal, walking around the
 * ghost house and leaving the maze at a portal on the opposite side.
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
 * <p>
 * TODO: are the level data except for the bonus symbols the same as in Pac-Man?
 * <p>
 * See https://gamefaqs.gamespot.com/arcade/583976-ms-pac-man/faqs/1298
 * 
 * @author Armin Reichert
 */
public class MsPacManGame extends GameModel {

	/*@formatter:off*/
	private static final byte[][] MAP1 = {
		{1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1},
		{1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1},
		{1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1},
		{1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1},
		{1,3,3,3,3,3,3,1,1,3,3,3,3,3,3,3,3,3,3,1,1,3,3,3,3,3,3,1},
		{1,4,1,1,1,1,3,1,1,3,1,1,1,1,1,1,1,1,3,1,1,3,1,1,1,1,4,1},
		{1,3,1,1,1,1,3,1,1,3,1,1,1,1,1,1,1,1,3,1,1,3,1,1,1,1,3,1},
		{1,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,1},
		{1,1,1,3,1,1,3,1,1,1,1,1,3,1,1,3,1,1,1,1,1,3,1,1,3,1,1,1},
		{0,0,1,3,1,1,3,1,1,1,1,1,3,1,1,3,1,1,1,1,1,3,1,1,3,1,0,0},
		{1,1,1,3,1,1,3,1,1,1,1,1,3,1,1,3,1,1,1,1,1,3,1,1,3,1,1,1},
		{2,2,2,3,1,1,3,3,3,3,3,3,3,1,1,3,3,3,3,3,3,3,1,1,3,2,2,2},
		{1,1,1,3,1,1,1,1,1,0,1,1,1,1,1,1,1,1,0,1,1,1,1,1,3,1,1,1},
		{0,0,1,3,1,1,1,1,1,0,1,1,1,1,1,1,1,1,0,1,1,1,1,1,3,1,0,0},
		{0,0,1,3,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,3,1,0,0},
		{0,0,1,3,1,1,1,1,1,0,1,1,1,0,0,1,1,1,0,1,1,1,1,1,3,1,0,0},
		{0,0,1,3,1,1,1,1,1,0,1,0,0,0,0,0,0,1,0,1,1,1,1,1,3,1,0,0},
		{0,0,1,3,1,1,0,0,0,0,1,0,0,0,0,0,0,1,0,0,0,0,1,1,3,1,0,0},
		{0,0,1,3,1,1,0,1,1,0,1,0,0,0,0,0,0,1,0,1,1,0,1,1,3,1,0,0},
		{1,1,1,3,1,1,0,1,1,0,1,1,1,1,1,1,1,1,0,1,1,0,1,1,3,1,1,1},
		{2,2,2,3,0,0,0,1,1,0,0,0,0,0,0,0,0,0,0,1,1,0,0,0,3,2,2,2},
		{1,1,1,3,1,1,1,1,1,1,1,1,0,1,1,0,1,1,1,1,1,1,1,1,3,1,1,1},
		{0,0,1,3,1,1,1,1,1,1,1,1,0,1,1,0,1,1,1,1,1,1,1,1,3,1,0,0},
		{0,0,1,3,3,3,3,3,3,3,0,0,0,1,1,0,0,0,3,3,3,3,3,3,3,1,0,0},
		{0,0,1,3,1,1,1,1,1,3,1,1,1,1,1,1,1,1,3,1,1,1,1,1,3,1,0,0},
		{1,1,1,3,1,1,1,1,1,3,1,1,1,1,1,1,1,1,3,1,1,1,1,1,3,1,1,1},
		{1,3,3,3,3,3,3,3,3,3,3,3,3,0,0,3,3,3,3,3,3,3,3,3,3,3,3,1},
		{1,3,1,1,1,1,3,1,1,1,1,1,3,1,1,3,1,1,1,1,1,3,1,1,1,1,3,1},
		{1,3,1,1,1,1,3,1,1,1,1,1,3,1,1,3,1,1,1,1,1,3,1,1,1,1,3,1},
		{1,3,1,1,1,1,3,1,1,3,3,3,3,1,1,3,3,3,3,1,1,3,1,1,1,1,3,1},
		{1,4,1,1,1,1,3,1,1,3,1,1,1,1,1,1,1,1,3,1,1,3,1,1,1,1,4,1},
		{1,3,1,1,1,1,3,1,1,3,1,1,1,1,1,1,1,1,3,1,1,3,1,1,1,1,3,1},
		{1,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,1},
		{1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1},
		{1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1},
		{1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1},
	};

	private static final byte[][] MAP2 = {
		{1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1},
		{1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1},
		{1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1},
		{1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1},
		{2,2,2,2,2,2,0,1,1,3,3,3,3,3,3,3,3,3,3,1,1,0,2,2,2,2,2,2},
		{1,1,1,1,1,1,0,1,1,3,1,1,1,1,1,1,1,1,3,1,1,0,1,1,1,1,1,1},
		{1,1,1,1,1,1,0,1,1,3,1,1,1,1,1,1,1,1,3,1,1,0,1,1,1,1,1,1},
		{1,4,3,3,3,3,3,3,3,3,3,3,3,1,1,3,3,3,3,3,3,3,3,3,3,3,4,1},
		{1,3,1,1,1,1,1,1,1,3,1,1,3,1,1,3,1,1,3,1,1,1,1,1,1,1,3,1},
		{1,3,1,1,1,1,1,1,1,3,1,1,3,1,1,3,1,1,3,1,1,1,1,1,1,1,3,1},
		{1,3,1,1,3,3,3,3,3,3,1,1,3,1,1,3,1,1,3,3,3,3,3,3,1,1,3,1},
		{1,3,1,1,3,1,1,1,1,0,1,1,3,3,3,3,1,1,0,1,1,1,1,3,1,1,3,1},
		{1,3,1,1,3,1,1,1,1,0,1,1,1,1,1,1,1,1,0,1,1,1,1,3,1,1,3,1},
		{1,3,3,3,3,3,3,1,1,0,1,1,1,1,1,1,1,1,0,1,1,3,3,3,3,3,3,1},
		{1,1,1,1,1,1,3,1,1,0,0,0,0,0,0,0,0,0,0,1,1,3,1,1,1,1,1,1},
		{1,1,1,1,1,1,3,1,1,0,1,1,1,0,0,1,1,1,0,1,1,3,1,1,1,1,1,1},
		{1,3,3,3,3,3,3,1,1,0,1,0,0,0,0,0,0,1,0,1,1,3,3,3,3,3,3,1},
		{1,3,1,1,1,1,3,1,1,0,1,0,0,0,0,0,0,1,0,1,1,3,1,1,1,1,3,1},
		{1,3,1,1,1,1,3,0,0,0,1,0,0,0,0,0,0,1,0,0,0,3,1,1,1,1,3,1},
		{1,3,3,3,1,1,3,1,1,0,1,1,1,1,1,1,1,1,0,1,1,3,1,1,3,3,3,1},
		{1,1,1,3,1,1,3,1,1,0,0,0,0,0,0,0,0,0,0,1,1,3,1,1,3,1,1,1},
		{0,0,1,3,1,1,3,1,1,1,1,0,1,1,1,1,0,1,1,1,1,3,1,1,3,1,0,0},
		{0,0,1,3,1,1,3,1,1,1,1,0,1,1,1,1,0,1,1,1,1,3,1,1,3,1,0,0},
		{0,0,1,3,3,3,3,3,3,3,3,3,1,1,1,1,3,3,3,3,3,3,3,3,3,1,0,0},
		{0,0,1,3,1,1,1,1,1,1,1,3,1,1,1,1,3,1,1,1,1,1,1,1,3,1,0,0},
		{1,1,1,3,1,1,1,1,1,1,1,3,1,1,1,1,3,1,1,1,1,1,1,1,3,1,1,1},
		{2,2,2,3,3,3,3,1,1,3,3,3,0,0,0,0,3,3,3,1,1,3,3,3,3,2,2,2},
		{1,1,1,3,1,1,3,1,1,3,1,1,1,1,1,1,1,1,3,1,1,3,1,1,3,1,1,1},
		{1,1,1,3,1,1,3,1,1,3,1,1,1,1,1,1,1,1,3,1,1,3,1,1,3,1,1,1},
		{1,4,3,3,1,1,3,3,3,3,3,3,3,1,1,3,3,3,3,3,3,3,1,1,3,3,4,1},
		{1,3,1,1,1,1,3,1,1,1,1,1,3,1,1,3,1,1,1,1,1,3,1,1,1,1,3,1},
		{1,3,1,1,1,1,3,1,1,1,1,1,3,1,1,3,1,1,1,1,1,3,1,1,1,1,3,1},
		{1,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,1},
		{1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1},
		{1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1},
		{1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1},
	};

	private static final byte[][] MAP3 = {
		{1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1},
		{1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1},
		{1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1},
		{1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1},
		{1,3,3,3,3,3,3,3,3,3,1,1,3,3,3,3,1,1,3,3,3,3,3,3,3,3,3,1},
		{1,3,1,1,1,1,1,1,1,3,1,1,3,1,1,3,1,1,3,1,1,1,1,1,1,1,3,1},
		{1,4,1,1,1,1,1,1,1,3,1,1,3,1,1,3,1,1,3,1,1,1,1,1,1,1,4,1},
		{1,3,1,1,3,3,3,3,3,3,3,3,3,1,1,3,3,3,3,3,3,3,3,3,1,1,3,1},
		{1,3,1,1,3,1,1,3,1,1,1,1,3,1,1,3,1,1,1,1,3,1,1,3,1,1,3,1},
		{1,3,3,3,3,1,1,3,1,1,1,1,3,1,1,3,1,1,1,1,3,1,1,3,3,3,3,1},
		{1,1,1,1,3,1,1,3,1,1,1,1,3,1,1,3,1,1,1,1,3,1,1,3,1,1,1,1},
		{1,1,1,1,3,1,1,3,3,3,3,3,3,3,3,3,3,3,3,3,3,1,1,3,1,1,1,1},
		{2,3,3,3,3,1,1,1,1,0,1,1,1,1,1,1,1,1,0,1,1,1,1,3,3,3,3,2},
		{1,3,1,1,0,1,1,1,1,0,1,1,1,1,1,1,1,1,0,1,1,1,1,0,1,1,3,1},
		{1,3,1,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1,1,3,1},
		{1,3,1,1,1,1,0,1,1,0,1,1,1,0,0,1,1,1,0,1,1,0,1,1,1,1,3,1},
		{1,3,1,1,1,1,0,1,1,0,1,0,0,0,0,0,0,1,0,1,1,0,1,1,1,1,3,1},
		{1,3,0,0,0,0,0,1,1,0,1,0,0,0,0,0,0,1,0,1,1,0,0,0,0,0,3,1},
		{1,3,1,1,0,1,1,1,1,0,1,0,0,0,0,0,0,1,0,1,1,1,1,0,1,1,3,1},
		{1,3,1,1,0,1,1,1,1,0,1,1,1,1,1,1,1,1,0,1,1,1,1,0,1,1,3,1},
		{1,3,1,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1,1,3,1},
		{1,3,1,1,1,1,0,1,1,1,1,1,0,1,1,0,1,1,1,1,1,0,1,1,1,1,3,1},
		{1,3,1,1,1,1,0,1,1,1,1,1,0,1,1,0,1,1,1,1,1,0,1,1,1,1,3,1},
		{1,3,3,3,3,3,3,1,1,3,3,3,3,1,1,3,3,3,3,1,1,3,3,3,3,3,3,1},
		{1,1,1,3,1,1,3,1,1,3,1,1,1,1,1,1,1,1,3,1,1,3,1,1,3,1,1,1},
		{1,1,1,3,1,1,3,1,1,3,1,1,1,1,1,1,1,1,3,1,1,3,1,1,3,1,1,1},
		{1,4,3,3,1,1,3,3,3,3,3,3,3,0,0,3,3,3,3,3,3,3,1,1,3,3,4,1},
		{1,3,1,1,1,1,3,1,1,1,1,1,3,1,1,3,1,1,1,1,1,3,1,1,1,1,3,1},
		{1,3,1,1,1,1,3,1,1,1,1,1,3,1,1,3,1,1,1,1,1,3,1,1,1,1,3,1},
		{1,3,3,3,3,3,3,1,1,3,3,3,3,1,1,3,3,3,3,1,1,3,3,3,3,3,3,1},
		{1,3,1,1,1,1,3,1,1,3,1,1,1,1,1,1,1,1,3,1,1,3,1,1,1,1,3,1},
		{1,3,1,1,1,1,3,1,1,3,1,1,1,1,1,1,1,1,3,1,1,3,1,1,1,1,3,1},
		{1,3,3,3,3,3,3,1,1,3,3,3,3,3,3,3,3,3,3,1,1,3,3,3,3,3,3,1},
		{1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1},
		{1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1},
		{1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1},
	};

	private static final byte[][] MAP4 = {
		{1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1},
		{1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1},
		{1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1},
		{1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1},
		{1,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,1},
		{1,3,1,1,3,1,1,1,1,3,1,1,1,1,1,1,1,1,3,1,1,1,1,3,1,1,3,1},
		{1,4,1,1,3,1,1,1,1,3,1,1,1,1,1,1,1,1,3,1,1,1,1,3,1,1,4,1},
		{1,3,1,1,3,1,1,1,1,3,1,1,3,3,3,3,1,1,3,1,1,1,1,3,1,1,3,1},
		{1,3,1,1,3,3,3,3,3,3,1,1,3,1,1,3,1,1,3,3,3,3,3,3,1,1,3,1},
		{1,3,1,1,1,1,3,1,1,3,1,1,3,1,1,3,1,1,3,1,1,3,1,1,1,1,3,1},
		{1,3,1,1,1,1,3,1,1,3,1,1,3,1,1,3,1,1,3,1,1,3,1,1,1,1,3,1},
		{1,3,3,3,3,3,3,1,1,3,3,3,3,1,1,3,3,3,3,1,1,3,3,3,3,3,3,1},
		{1,1,1,3,1,1,1,1,1,1,1,1,0,1,1,0,1,1,1,1,1,1,1,1,3,1,1,1},
		{0,0,1,3,1,1,1,1,1,1,1,1,0,1,1,0,1,1,1,1,1,1,1,1,3,1,0,0},
		{0,0,1,3,3,3,3,1,1,0,0,0,0,0,0,0,0,0,0,1,1,3,3,3,3,1,0,0},
		{1,1,1,0,1,1,3,1,1,0,1,1,1,0,0,1,1,1,0,1,1,3,1,1,0,1,1,1},
		{2,2,2,0,1,1,3,1,1,0,1,0,0,0,0,0,0,1,0,1,1,3,1,1,0,2,2,2},
		{1,1,1,1,1,1,3,0,0,0,1,0,0,0,0,0,0,1,0,0,0,3,1,1,1,1,1,1},
		{1,1,1,1,1,1,3,1,1,0,1,0,0,0,0,0,0,1,0,1,1,3,1,1,1,1,1,1},
		{2,2,2,0,1,1,3,1,1,0,1,1,1,1,1,1,1,1,0,1,1,3,1,1,0,2,2,2},
		{1,1,1,0,1,1,3,1,1,0,0,0,0,0,0,0,0,0,0,1,1,3,1,1,0,1,1,1},
		{0,0,1,3,3,3,3,1,1,1,1,1,0,1,1,0,1,1,1,1,1,3,3,3,3,1,0,0},
		{0,0,1,3,1,1,3,1,1,1,1,1,0,1,1,0,1,1,1,1,1,3,1,1,3,1,0,0},
		{0,0,1,3,1,1,3,3,3,3,0,0,0,1,1,0,0,0,3,3,3,3,1,1,3,1,0,0},
		{0,0,1,3,1,1,1,1,1,3,1,1,0,1,1,0,1,1,3,1,1,1,1,1,3,1,0,0},
		{1,1,1,3,1,1,1,1,1,3,1,1,0,1,1,0,1,1,3,1,1,1,1,1,3,1,1,1},
		{1,3,3,3,3,3,3,3,3,3,1,1,0,0,0,0,1,1,3,3,3,3,3,3,3,3,3,1},
		{1,3,1,1,1,1,3,1,1,3,1,1,1,1,1,1,1,1,3,1,1,3,1,1,1,1,3,1},
		{1,3,1,1,1,1,3,1,1,3,1,1,1,1,1,1,1,1,3,1,1,3,1,1,1,1,3,1},
		{1,3,1,1,3,3,3,1,1,3,3,3,3,3,3,3,3,3,3,1,1,3,3,3,1,1,3,1},
		{1,4,1,1,3,1,1,1,1,1,1,1,3,1,1,3,1,1,1,1,1,1,1,3,1,1,4,1},
		{1,3,1,1,3,1,1,1,1,1,1,1,3,1,1,3,1,1,1,1,1,1,1,3,1,1,3,1},
		{1,3,3,3,3,3,3,3,3,3,3,3,3,1,1,3,3,3,3,3,3,3,3,3,3,3,3,1},
		{1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1},
		{1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1},
		{1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1},
	};
	
	private static Bonus createBonus(int levelNumber) {
		int n = (levelNumber > 7) ? 1 + RND.nextInt(7) : levelNumber;	
		return switch (n) {
		case 1 -> new MovingBonus(0,  100); // Cherries
		case 2 -> new MovingBonus(1,  200); // Strawberry
		case 3 -> new MovingBonus(2,  500); // Peach
		case 4 -> new MovingBonus(3,  700); // Pretzel (A Brezn, HerrGottSakra!)
		case 5 -> new MovingBonus(4, 1000); // Apple
		case 6 -> new MovingBonus(5, 2000); // Pear
		case 7 -> new MovingBonus(6, 5000); // Banana
		default -> throw new IllegalArgumentException();
		};
	}
	//@formatter:on

	public static ArcadeWorld createWorld(int number) {
		var map = switch (number) {
		case 1 -> MAP1;
		case 2 -> MAP2;
		case 3 -> MAP3;
		case 4 -> MAP4;
		default -> throw new IllegalArgumentException(
				"Illegal map number: %d. Allowed values: 1, 2, 3, 4.".formatted(number));
		};
		return new ArcadeWorld(map);
	}

	private static int mazeNumber(int levelNumber) {
		return switch (levelNumber) {
		case 1, 2 -> 1;
		case 3, 4, 5 -> 2;
		case 6, 7, 8, 9 -> 3;
		case 10, 11, 12, 13 -> 4;
		default -> (levelNumber - 14) % 8 < 4 ? 5 : 6;
		};
	}

	private static int mapNumber(int levelNumber) {
		return levelNumber < 14 ? mazeNumber(levelNumber) : mazeNumber(levelNumber) - 2;
	}

	@Override
	protected Pac createPac() {
		return new Pac("Ms. Pac-Man");
	}

	@Override
	protected Ghost[] createGhosts() {
		return new Ghost[] { //
				new Ghost(ID_RED_GHOST, "Blinky"), //
				new Ghost(ID_PINK_GHOST, "Pinky"), //
				new Ghost(ID_CYAN_GHOST, "Inky"), //
				new Ghost(ID_ORANGE_GHOST, "Sue") //
		};
	}

	@Override
	protected void createLevel(int levelNumber) {
		var data = levelNumber <= LEVELS.length ? LEVELS[levelNumber - 1] : LEVELS[LEVELS.length - 1];
		int mapNumber = mapNumber(levelNumber);
		int mazeNumber = mazeNumber(levelNumber);
		var world = createWorld(mapNumber);
		var bonus = createBonus(levelNumber);
		var houseRules = createHouseRules(levelNumber);
		level = GameLevel.fromData(levelNumber, mazeNumber, world, bonus, houseRules, data);
	}

	@Override
	public GameVariant variant() {
		return GameVariant.MS_PACMAN;
	}

	@Override
	protected void onBonusReached(Bonus bonus) {
		var houseEntry = level.world().ghostHouse().entryTile();
		int houseHeight = level.world().ghostHouse().size().y();
		int numPortals = level.world().portals().size();
		if (numPortals > 0) {
			var entryPortal = (HorizontalPortal) level.world().portals().get(RND.nextInt(numPortals));
			var exitPortal = (HorizontalPortal) level.world().portals().get(RND.nextInt(numPortals));
			var orientation = RND.nextBoolean() ? Direction.LEFT : Direction.RIGHT;
			var start = orientation == Direction.RIGHT ? np(entryPortal.leftTunnelEnd()) : np(entryPortal.rightTunnelEnd());
			List<NavigationPoint> route = new ArrayList<>();
			route.add(np(houseEntry));
			route.add(np(houseEntry.plus(0, houseHeight + 2)));
			route.add(np(houseEntry));
			route.add(orientation == Direction.RIGHT ? np(exitPortal.rightTunnelEnd()) : np(exitPortal.leftTunnelEnd()));
			LOGGER.trace("Bonus route: %s, orientation: %s", route, orientation);
			MovingBonus movingBonus = (MovingBonus) bonus;
			movingBonus.setRoute(route);
			movingBonus.placeAtTile(start.tile(), 0, 0);
			movingBonus.setMoveAndWishDir(orientation);
			movingBonus.setEdible(TickTimer.INDEFINITE);
			GameEvents.publish(GameEventType.BONUS_GETS_ACTIVE, movingBonus.tile());
		}
	}
}