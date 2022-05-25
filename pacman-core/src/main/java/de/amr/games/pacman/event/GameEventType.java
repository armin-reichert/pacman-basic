/*
MIT License

Copyright (c) 2022 Armin Reichert

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

package de.amr.games.pacman.event;

/**
 * Game event types.
 * 
 * @author Armin Reichert
 */
public enum GameEventType {
	//@formatter:off
	BONUS_ACTIVATED, 
	BONUS_EATEN, 
	BONUS_EXPIRED, 
	GAME_SCATTERING_STARTED, 
	GAME_STATE_CHANGED,
	GHOST_ENTERED_HOUSE, 
	GHOST_REVIVED, 
	GHOST_STARTED_LEAVING_HOUSE, 
	GHOST_FINISHED_LEAVING_HOUSE, 
	GHOST_STARTED_RETURNING_HOME,
	PLAYER_FOUND_FOOD, 
	PLAYER_GOT_EXTRA_LIFE, 
	PLAYER_GOT_POWER, 
	PLAYER_STARTED_LOSING_POWER,
	PLAYER_LOST_POWER;
	//@formatter:on
}