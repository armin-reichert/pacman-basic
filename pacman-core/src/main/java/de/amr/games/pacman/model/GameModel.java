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
package de.amr.games.pacman.model;

import static de.amr.games.pacman.lib.Globals.checkGameVariant;
import static de.amr.games.pacman.lib.Globals.checkLevelNumber;
import static de.amr.games.pacman.lib.Globals.checkNotNull;
import static de.amr.games.pacman.lib.Globals.v2i;
import static de.amr.games.pacman.lib.steering.NavigationPoint.np;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.Properties;

import org.tinylog.Logger;

import de.amr.games.pacman.event.GameEvents;
import de.amr.games.pacman.lib.math.Vector2i;
import de.amr.games.pacman.lib.steering.Direction;
import de.amr.games.pacman.lib.steering.NavigationPoint;
import de.amr.games.pacman.lib.steering.RouteBasedSteering;
import de.amr.games.pacman.lib.steering.RuleBasedSteering;
import de.amr.games.pacman.model.world.World;

/**
 * Pac-Man / Ms. Pac-Man game model.
 * 
 * @author Armin Reichert
 */
public class GameModel {

	//@formatter:off
	public static final byte[][] PACMAN_MAP = {
		{1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1},
		{1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1},
		{1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1},
		{1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1},
		{1,3,3,3,3,3,3,3,3,3,3,3,3,1,1,3,3,3,3,3,3,3,3,3,3,3,3,1},
		{1,3,1,1,1,1,3,1,1,1,1,1,3,1,1,3,1,1,1,1,1,3,1,1,1,1,3,1},
		{1,4,1,1,1,1,3,1,1,1,1,1,3,1,1,3,1,1,1,1,1,3,1,1,1,1,4,1},
		{1,3,1,1,1,1,3,1,1,1,1,1,3,1,1,3,1,1,1,1,1,3,1,1,1,1,3,1},
		{1,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,1},
		{1,3,1,1,1,1,3,1,1,3,1,1,1,1,1,1,1,1,3,1,1,3,1,1,1,1,3,1},
		{1,3,1,1,1,1,3,1,1,3,1,1,1,1,1,1,1,1,3,1,1,3,1,1,1,1,3,1},
		{1,3,3,3,3,3,3,1,1,3,3,3,3,1,1,3,3,3,3,1,1,3,3,3,3,3,3,1},
		{1,1,1,1,1,1,3,1,1,1,1,1,0,1,1,0,1,1,1,1,1,3,1,1,1,1,1,1},
		{0,0,0,0,0,1,3,1,1,1,1,1,0,1,1,0,1,1,1,1,1,3,1,0,0,0,0,0},
		{0,0,0,0,0,1,3,1,1,0,0,0,0,0,0,0,0,0,0,1,1,3,1,0,0,0,0,0},
		{0,0,0,0,0,1,3,1,1,0,1,1,1,0,0,1,1,1,0,1,1,3,1,0,0,0,0,0},
		{1,1,1,1,1,1,3,1,1,0,1,0,0,0,0,0,0,1,0,1,1,3,1,1,1,1,1,1},
		{2,2,2,2,2,2,3,0,0,0,1,0,0,0,0,0,0,1,0,0,0,3,2,2,2,2,2,2},
		{1,1,1,1,1,1,3,1,1,0,1,0,0,0,0,0,0,1,0,1,1,3,1,1,1,1,1,1},
		{0,0,0,0,0,1,3,1,1,0,1,1,1,1,1,1,1,1,0,1,1,3,1,0,0,0,0,0},
		{0,0,0,0,0,1,3,1,1,0,0,0,0,0,0,0,0,0,0,1,1,3,1,0,0,0,0,0},
		{0,0,0,0,0,1,3,1,1,0,1,1,1,1,1,1,1,1,0,1,1,3,1,0,0,0,0,0},
		{1,1,1,1,1,1,3,1,1,0,1,1,1,1,1,1,1,1,0,1,1,3,1,1,1,1,1,1},
		{1,3,3,3,3,3,3,3,3,3,3,3,3,1,1,3,3,3,3,3,3,3,3,3,3,3,3,1},
		{1,3,1,1,1,1,3,1,1,1,1,1,3,1,1,3,1,1,1,1,1,3,1,1,1,1,3,1},
		{1,3,1,1,1,1,3,1,1,1,1,1,3,1,1,3,1,1,1,1,1,3,1,1,1,1,3,1},
		{1,4,3,3,1,1,3,3,3,3,3,3,3,0,0,3,3,3,3,3,3,3,1,1,3,3,4,1},
		{1,1,1,3,1,1,3,1,1,3,1,1,1,1,1,1,1,1,3,1,1,3,1,1,3,1,1,1},
		{1,1,1,3,1,1,3,1,1,3,1,1,1,1,1,1,1,1,3,1,1,3,1,1,3,1,1,1},
		{1,3,3,3,3,3,3,1,1,3,3,3,3,1,1,3,3,3,3,1,1,3,3,3,3,3,3,1},
		{1,3,1,1,1,1,1,1,1,1,1,1,3,1,1,3,1,1,1,1,1,1,1,1,1,1,3,1},
		{1,3,1,1,1,1,1,1,1,1,1,1,3,1,1,3,1,1,1,1,1,1,1,1,1,1,3,1},
		{1,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,1},
		{1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1},
		{1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1},
		{1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1},
	};
	
	public static final List<Vector2i> PACMAN_RED_ZONE = List.of(v2i(12, 14), v2i(15, 14), v2i(12, 26), v2i(15, 26));

	public static final byte[][][] MS_PACMAN_MAPS =  {
		{
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
		},
	
		{
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
		},
	
		{
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
		},
	
		{
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
		}
	};
	
	/**
	 * In Ms. Pac-Man, there are 4 maps used by the 6 mazes. Up to level 13, the mazes are:
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
	 */
	public static int mazeNumberMsPacMan(int levelNumber) {
		return switch (levelNumber) {
		case 1, 2 -> 1;
		case 3, 4, 5 -> 2;
		case 6, 7, 8, 9 -> 3;
		case 10, 11, 12, 13 -> 4;
		default -> (levelNumber - 14) % 8 < 4 ? 5 : 6;
		};
	}
	
	private static int mapNumberMsPacMan(int levelNumber) {
		return switch (levelNumber) {
		case 1, 2 -> 1;
		case 3, 4, 5 -> 2;
		case 6, 7, 8, 9 -> 3;
		case 10, 11, 12, 13 -> 4;
		default -> (levelNumber - 14) % 8 < 4 ? 3 : 4;
		};
	}
	
	private static final List<NavigationPoint> PACMAN_DEMOLEVEL_ROUTE = List.of( //
			np(12, 26), np(9, 26), np(12, 32), np(15, 32), np(24, 29), np(21, 23), np(18, 23), np(18, 20), np(18, 17),
			np(15, 14), np(12, 14), np(9, 17), np(6, 17), np(6, 11), np(6, 8), np(6, 4), np(1, 8), np(6, 8), np(9, 8),
			np(12, 8), np(6, 4), np(6, 8), np(6, 11), np(1, 8), np(6, 8), np(9, 8), np(12, 14), np(9, 17), np(6, 17),
			np(0, 17), np(21, 17), np(21, 23), np(21, 26), np(24, 29), /* avoid moving up: */ np(26, 29), np(15, 32),
			np(12, 32), np(3, 29), np(6, 23), np(9, 23), np(12, 26), np(15, 26), np(18, 23), np(21, 23), np(24, 29),
			/* avoid moving up: */ np(26, 29), np(15, 32), np(12, 32), np(3, 29), np(6, 23) //
	);
	
	@SuppressWarnings("unused")
	private static final List<NavigationPoint> GHOST_0_ROUTE = List.of( //
			np(21, 4, Direction.DOWN), np(21, 8, Direction.DOWN), np(21, 11, Direction.RIGHT), np(26, 8, Direction.LEFT),
			np(21, 8, Direction.DOWN), np(26, 8, Direction.UP), np(26, 8, Direction.DOWN), np(21, 11, Direction.DOWN),
			np(21, 17, Direction.RIGHT), // enters

			np(99, 99, Direction.DOWN) //
	);

	//@formatter:on

	public static final byte RED_GHOST = 0;
	public static final byte PINK_GHOST = 1;
	public static final byte CYAN_GHOST = 2;
	public static final byte ORANGE_GHOST = 3;

	/** Game loop frequency. */
	public static final short FPS = 60;
	/** Pixels/tick at 100% relative speed. */
	public static final float SPEED_PX_100_PERCENT = 1.25f;
	public static final float SPEED_PX_INSIDE_HOUSE = 0.5f; // correct?
	public static final float SPEED_PX_RETURNING_TO_HOUSE = 2.0f; // correct?
	public static final float SPEED_PX_ENTERING_HOUSE = 1.25f; // correct?

	public static final short MAX_CREDIT = 99;
	public static final short LEVEL_COUNTER_MAX_SYMBOLS = 7;
	public static final short INITIAL_LIVES = 3;
	public static final short RESTING_TICKS_NORMAL_PELLET = 1;
	public static final short RESTING_TICKS_ENERGIZER = 3;
	public static final short POINTS_NORMAL_PELLET = 10;
	public static final short POINTS_ENERGIZER = 50;
	public static final short POINTS_ALL_GHOSTS_KILLED_IN_LEVEL = 12_000;
	public static final short[] POINTS_GHOSTS_SEQUENCE = { 200, 400, 800, 1600 };
	public static final short SCORE_EXTRA_LIFE = 10_000;
	public static final short BONUS_POINTS_SHOWN_TICKS = 2 * FPS; // unsure
	public static final short PAC_POWER_FADES_TICKS = 2 * FPS - 1; // unsure

	// Animation keys (positive byte value, -1 = no selection)
	public static final byte AK_GHOST_BLUE = 0;
	public static final byte AK_GHOST_COLOR = 1;
	public static final byte AK_GHOST_EYES = 2;
	public static final byte AK_GHOST_FLASHING = 3;
	public static final byte AK_GHOST_VALUE = 4;
	public static final byte AK_MAZE_ENERGIZER_BLINKING = 5;
	public static final byte AK_MAZE_FLASHING = 6;
	public static final byte AK_PAC_DYING = 7;
	public static final byte AK_PAC_MUNCHING = 8;
	public static final byte AK_PAC_BIG = 9;
	public static final byte AK_BLINKY_DAMAGED = 10;
	public static final byte AK_BLINKY_PATCHED = 11;
	public static final byte AK_BLINKY_NAKED = 12;

	// Sound events
	public static final String SE_BONUS_EATEN = "bonus_eaten";
	public static final String SE_CREDIT_ADDED = "credit_added";
	public static final String SE_EXTRA_LIFE = "extra_life";
	public static final String SE_GHOST_EATEN = "ghost_eaten";
	public static final String SE_HUNTING_PHASE_STARTED_0 = "hunting_phase_started_0";
	public static final String SE_HUNTING_PHASE_STARTED_2 = "hunting_phase_started_2";
	public static final String SE_HUNTING_PHASE_STARTED_4 = "hunting_phase_started_4";
	public static final String SE_HUNTING_PHASE_STARTED_6 = "hunting_phase_started_5";
	public static final String SE_PACMAN_DEATH = "pacman_death";
	public static final String SE_PACMAN_FOUND_FOOD = "pacman_found_food";
	public static final String SE_PACMAN_POWER_ENDS = "pacman_power_ends";
	public static final String SE_PACMAN_POWER_STARTS = "pacman_power_starts";
	public static final String SE_READY_TO_PLAY = "ready_to_play";
	public static final String SE_START_INTERMISSION_1 = "start_intermission_1";
	public static final String SE_START_INTERMISSION_2 = "start_intermission_2";
	public static final String SE_START_INTERMISSION_3 = "start_intermission_3";
	public static final String SE_STOP_ALL_SOUNDS = "stop_all_sounds";

	//@formatter:off
	private static final byte[][] LEVEL_DATA = {
	/* 1*/ { 80, 75, 40,  20,  80, 10,  85,  90, 50, 6, 5, 0},
	/* 2*/ { 90, 85, 45,  30,  90, 15,  95,  95, 55, 5, 5, 1},
	/* 3*/ { 90, 85, 45,  40,  90, 20,  95,  95, 55, 4, 5, 0},
	/* 4*/ { 90, 85, 45,  40,  90, 20,  95,  95, 55, 3, 5, 0},
	/* 5*/ {100, 95, 50,  40, 100, 20, 105, 100, 60, 2, 5, 2},
	/* 6*/ {100, 95, 50,  50, 100, 25, 105, 100, 60, 5, 5, 0},
	/* 7*/ {100, 95, 50,  50, 100, 25, 105, 100, 60, 2, 5, 0},
	/* 8*/ {100, 95, 50,  50, 100, 25, 105, 100, 60, 2, 5, 0},
	/* 9*/ {100, 95, 50,  60, 100, 30, 105, 100, 60, 1, 3, 3},
	/*10*/ {100, 95, 50,  60, 100, 30, 105, 100, 60, 5, 5, 0},
	/*11*/ {100, 95, 50,  60, 100, 30, 105, 100, 60, 2, 5, 0},
	/*12*/ {100, 95, 50,  80, 100, 40, 105, 100, 60, 1, 3, 0},
	/*13*/ {100, 95, 50,  80, 100, 40, 105, 100, 60, 1, 3, 3},
	/*14*/ {100, 95, 50,  80, 100, 40, 105, 100, 60, 3, 5, 0},
	/*15*/ {100, 95, 50, 100, 100, 50, 105, 100, 60, 1, 3, 0},
	/*16*/ {100, 95, 50, 100, 100, 50, 105, 100, 60, 1, 3, 0},
	/*17*/ {100, 95, 50, 100, 100, 50, 105,   0,  0, 0, 0, 3},
	/*18*/ {100, 95, 50, 100, 100, 50, 105, 100, 60, 1, 3, 0},
	/*19*/ {100, 95, 50, 120, 100, 60, 105,   0,  0, 0, 0, 0},
	/*20*/ {100, 95, 50, 120, 100, 60, 105,   0,  0, 0, 0, 0},
	/*21*/ { 90, 95, 50, 120, 100, 60, 105,   0,  0, 0, 0, 0},
	};
	
	/**
	 * @param levelNumber level number (starting at 1)
	 * @return parameter values (speed, pellet counts etc.) used in specified level. From level 21 on, level parameters
	 *         remain the same
	 */
	private static byte[] levelData(int levelNumber) {
		return levelNumber <= LEVEL_DATA.length //
				? LEVEL_DATA[levelNumber - 1]
				: LEVEL_DATA[LEVEL_DATA.length - 1];
	}

	
	// Hunting duration (in ticks) of chase and scatter phases. See Pac-Man dossier.
	private static final int[][] HUNTING_DURATIONS_PACMAN = {
		{ 7 * FPS, 20 * FPS, 7 * FPS, 20 * FPS, 5 * FPS,   20 * FPS, 5 * FPS, -1 }, // level 1
		{ 7 * FPS, 20 * FPS, 7 * FPS, 20 * FPS, 5 * FPS, 1033 * FPS,       1, -1 }, // levels 2-4
		{ 5 * FPS, 20 * FPS, 5 * FPS, 20 * FPS, 5 * FPS, 1037 * FPS,       1, -1 }, // levels 5+
	};
	
	// Got this from a conversation on Reddit
	// https://www.reddit.com/r/Pacman/comments/12q4ny3/is_anyone_able_to_explain_the_ai_behind_the/
	// https://github.com/armin-reichert/pacman-basic/blob/main/doc/mspacman-details-reddit-user-damselindis.md
	// TODO: is this information correct?
	private static final int[][] HUNTING_DURATIONS_MS_PACMAN = {
			{ 7 * FPS, 20 * FPS, 1, 1037 * FPS, 1, 1037 * FPS, 1, -1 }, // levels 1-4
			{ 5 * FPS, 20 * FPS, 1, 1037 * FPS, 1, 1037 * FPS, 1, -1 }, // levels 5+
		};
	
	//@formatter:on

	public int[] huntingDurations(int levelNumber) {
		checkLevelNumber(levelNumber);
		return switch (variant) {
		case MS_PACMAN -> HUNTING_DURATIONS_MS_PACMAN[levelNumber <= 4 ? 0 : 1];
		case PACMAN -> HUNTING_DURATIONS_PACMAN[levelNumber == 1 ? 0 : levelNumber <= 4 ? 1 : 2];
		default -> throw new IllegalGameVariantException(variant);
		};
	}

	// Note: To avoid religious wars I named the peach/orange thingy peach_orange

	//@formatter:off
	public static final BonusInfo MS_PACMAN_CHERRIES     = new BonusInfo(0,  100);
	public static final BonusInfo MS_PACMAN_STRAWBERRY   = new BonusInfo(1,  200);
	public static final BonusInfo MS_PACMAN_PEACH_ORANGE = new BonusInfo(2,  500);
	public static final BonusInfo MS_PACMAN_PRETZEL      = new BonusInfo(3,  700);
	public static final BonusInfo MS_PACMAN_APPLE        = new BonusInfo(4, 1000);
	public static final BonusInfo MS_PACMAN_PEAR         = new BonusInfo(5, 2000);
	public static final BonusInfo MS_PACMAN_BANANA       = new BonusInfo(6, 5000);

	public static final BonusInfo PACMAN_CHERRIES        = new BonusInfo(0,  100);
	public static final BonusInfo PACMAN_STRAWBERRY      = new BonusInfo(1,  300);
	public static final BonusInfo PACMAN_PEACH_ORANGE    = new BonusInfo(2,  500);
	public static final BonusInfo PACMAN_APPLE           = new BonusInfo(3,  700);
	public static final BonusInfo PACMAN_GRAPES          = new BonusInfo(4, 1000);
	public static final BonusInfo PACMAN_GALAXIAN        = new BonusInfo(5, 2000);
	public static final BonusInfo PACMAN_BELL            = new BonusInfo(6, 3000);
	public static final BonusInfo PACMAN_KEY             = new BonusInfo(7, 5000);
	//@formatter:on

	private final GameVariant variant;
	private GameLevel level;
	private final List<Byte> levelCounter = new LinkedList<>();
	private Score score;
	private Score highScore;
	private int credit;
	private int lives;
	private boolean playing;
	private boolean scoringEnabled;
	private boolean immune; // extra feature
	private boolean oneLessLifeDisplayed; // TODO get rid of this
	public int intermissionTestNumber; // intermission test mode

	public GameModel(GameVariant variant) {
		this.variant = variant;
		init();
	}

	/**
	 * Initializes the game. Credit, immunity and scores remain unchanged.
	 */
	public void init() {
		level = null;
		lives = INITIAL_LIVES;
		playing = false;
		scoringEnabled = true;
		oneLessLifeDisplayed = false; // @remove
		Logger.trace("Game model ({}) initialized", variant());
	}

	/**
	 * @return the game variant realized by this model
	 */
	public GameVariant variant() {
		return variant;
	}

	/**
	 * @return number of maze (not map) used in this level, 1-based.
	 */
	public int mazeNumber(int levelNumber) {
		return variant == GameVariant.MS_PACMAN ? mazeNumberMsPacMan(levelNumber) : 1;
	}

	/** @return (optional) current game level. */
	public Optional<GameLevel> level() {
		return Optional.ofNullable(level);
	}

	/**
	 * Creates and "enters" the level with the given number.
	 * 
	 * @param levelNumber level number (starting at 1)
	 */
	public void enterLevel(int levelNumber) {
		checkLevelNumber(levelNumber);

		var map = switch (variant) {
		case MS_PACMAN -> MS_PACMAN_MAPS[mapNumberMsPacMan(levelNumber) - 1];
		case PACMAN -> PACMAN_MAP;
		default -> throw new IllegalGameVariantException(variant);
		};
		level = new GameLevel(this, new World(map), levelNumber, levelData(levelNumber), false);

		if (level.number() == 1) {
			levelCounter.clear();
		}
		// In Ms. Pac-Man, the level counter stays unchanged from level 8 on and bonus symbols are created randomly whenever
		// a bonus is reached (also in the same level). At least that's what I was told.
		if (variant == GameVariant.PACMAN || level.number() <= 7) {
			levelCounter.add(level.symbol());
			if (levelCounter.size() > LEVEL_COUNTER_MAX_SYMBOLS) {
				levelCounter.remove(0);
			}
		}
		if (score != null) {
			score.setLevelNumber(levelNumber);
		}

		level.letsGetReadyToRumbleAndShowGuys(false);
	}

	/**
	 * Enters the demo game level ("attract mode").
	 */
	public void enterDemoLevel() {
		switch (variant) {
		case MS_PACMAN -> {
			level = new GameLevel(this, new World(MS_PACMAN_MAPS[0]), 1, levelData(1), true);
			level.setPacSteering(new RuleBasedSteering());
		}
		case PACMAN -> {
			level = new GameLevel(this, new World(PACMAN_MAP), 1, levelData(1), true);
			level.setPacSteering(new RouteBasedSteering(PACMAN_DEMOLEVEL_ROUTE));

		}
		default -> throw new IllegalGameVariantException(variant);
		}
		level.letsGetReadyToRumbleAndShowGuys(true);
		scoringEnabled = false;
		GameEvents.setSoundEventsEnabled(false);
		Logger.info("Ms. Pac-Man demo level entered");
	}

	/**
	 * Enters the next game level.
	 */
	public void nextLevel() {
		if (level == null) {
			throw new IllegalStateException("Cannot enter next level, no current level exists");
		}
		enterLevel(level.number() + 1);
	}

	public void removeLevel() {
		level = null;
	}

	/** @return tells if the game play is running. */
	public boolean isPlaying() {
		return playing;
	}

	public void setPlaying(boolean playing) {
		this.playing = playing;
	}

	/**
	 * @return tells if Pac-Man can get killed by ghosts
	 */
	public boolean isImmune() {
		return immune;
	}

	public void setImmune(boolean immune) {
		this.immune = immune;
	}

	public int lives() {
		return lives;
	}

	public void setLives(int lives) {
		if (lives < 0) {
			throw new IllegalArgumentException("Lives must not be negative but is: " + lives);
		}
		this.lives = lives;
	}

	/** @return collected level symbols. */
	public List<Byte> levelCounter() {
		return Collections.unmodifiableList(levelCounter);
	}

	public void clearLevelCounter() {
		levelCounter.clear();
	}

	public Optional<Score> score() {
		return Optional.ofNullable(score);
	}

	public void newScore() {
		score = new Score();
	}

	public Optional<Score> highScore() {
		return Optional.ofNullable(highScore);
	}

	public void scorePoints(int points) {
		if (points < 0) {
			throw new IllegalArgumentException("Scored points value must not be negative but is: " + points);
		}
		if (level == null) {
			throw new IllegalStateException("Cannot score points: No game level selected");
		}
		if (!scoringEnabled) {
			return;
		}
		final int oldScore = score.points();
		final int newScore = oldScore + points;
		score.setPoints(newScore);
		if (newScore > highScore.points()) {
			highScore.setPoints(newScore);
			highScore.setLevelNumber(level.number());
			highScore.setDate(LocalDate.now());
		}
		if (oldScore < SCORE_EXTRA_LIFE && newScore >= SCORE_EXTRA_LIFE) {
			lives += 1;
			GameEvents.publishSoundEvent(SE_EXTRA_LIFE);
		}
	}

	private static File highscoreFile(GameVariant variant) {
		checkGameVariant(variant);
		var dir = System.getProperty("user.home");
		return switch (variant) {
		case PACMAN -> new File(dir, "highscore-pacman.xml");
		case MS_PACMAN -> new File(dir, "highscore-ms_pacman.xml");
		default -> throw new IllegalGameVariantException(variant);
		};
	}

	private static Score loadHighscore(File file) {
		checkNotNull(file);
		try (var in = new FileInputStream(file)) {
			var props = new Properties();
			props.loadFromXML(in);
			var points = Integer.parseInt(props.getProperty("points"));
			var levelNumber = Integer.parseInt(props.getProperty("level"));
			var date = LocalDate.parse(props.getProperty("date"), DateTimeFormatter.ISO_LOCAL_DATE);
			Score scoreFromFile = new Score();
			scoreFromFile.setPoints(points);
			scoreFromFile.setLevelNumber(levelNumber);
			scoreFromFile.setDate(date);
			Logger.info("Highscore loaded. File: '{}' Points: {} Level: {}", file.getAbsolutePath(), scoreFromFile.points(),
					scoreFromFile.levelNumber());
			return scoreFromFile;
		} catch (Exception x) {
			Logger.info("Highscore could not be loaded. File '{}' Reason: {}", file, x.getMessage());
			return new Score();
		}
	}

	public void loadHighscore() {
		highScore = loadHighscore(highscoreFile(variant()));
	}

	public void saveNewHighscore() {
		var file = highscoreFile(variant());
		var oldHiscore = loadHighscore(file);
		if (highScore.points() <= oldHiscore.points()) {
			return;
		}
		var p = new Properties();
		p.setProperty("points", String.valueOf(highScore.points()));
		p.setProperty("level", String.valueOf(highScore.levelNumber()));
		p.setProperty("date", highScore.date().format(DateTimeFormatter.ISO_LOCAL_DATE));
		try (var out = new FileOutputStream(file)) {
			p.storeToXML(out, "%s Hiscore".formatted(variant()));
			Logger.info("Highscore saved. File: '{}' Points: {} Level: {}", file.getAbsolutePath(), highScore.points(),
					highScore.levelNumber());
		} catch (Exception x) {
			Logger.error("Highscore could not be saved. File '{}' Reason: {}", file, x.getMessage());
		}
	}

	/** @return number of coins inserted. */
	public int credit() {
		return credit;
	}

	public boolean setCredit(int credit) {
		if (0 <= credit && credit <= MAX_CREDIT) {
			this.credit = credit;
			return true;
		}
		return false;
	}

	public boolean changeCredit(int delta) {
		return setCredit(credit + delta);
	}

	public boolean hasCredit() {
		return credit > 0;
	}

	// TODO: get rid of this:

	/** If one less life is displayed in the lives counter. */
	public boolean isOneLessLifeDisplayed() {
		return oneLessLifeDisplayed;
	}

	public void setOneLessLifeDisplayed(boolean value) {
		this.oneLessLifeDisplayed = value;
	}
}