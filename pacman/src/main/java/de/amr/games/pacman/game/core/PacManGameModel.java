package de.amr.games.pacman.game.core;

import java.util.List;

import de.amr.games.pacman.game.creatures.Bonus;
import de.amr.games.pacman.game.creatures.Ghost;
import de.amr.games.pacman.game.creatures.Pac;
import de.amr.games.pacman.game.worlds.PacManGameWorld;
import de.amr.games.pacman.lib.Hiscore;

/**
 * The game data.
 * 
 * @author Armin Reichert
 */
public class PacManGameModel {

	public static final byte CLASSIC = 0, MS_PACMAN = 1;

	public byte variant;
	public PacManGameWorld world;
	public short levelNumber;
	public PacManGameLevel level;
	public Pac pac;
	public Ghost[] ghosts;
	public Bonus bonus;
	public byte lives;
	public int score;
	public short ghostBounty;
	public short globalDotCounter;
	public boolean globalDotCounterEnabled;
	public List<Byte> levelSymbols;
	public Hiscore hiscore;
}