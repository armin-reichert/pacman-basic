package de.amr.games.pacman.game.core;

import java.io.File;
import java.util.List;

import de.amr.games.pacman.game.creatures.Bonus;
import de.amr.games.pacman.game.creatures.Ghost;
import de.amr.games.pacman.game.creatures.MovingBonus;
import de.amr.games.pacman.game.creatures.Pac;
import de.amr.games.pacman.game.worlds.MsPacManWorld;
import de.amr.games.pacman.game.worlds.PacManClassicWorld;
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

	public static PacManGameModel newPacManClassicGame() {
		PacManGameModel game = new PacManGameModel();
		game.variant = CLASSIC;
		game.hiscore = new Hiscore(new File(System.getProperty("user.home"), "hiscore-pacman.xml"));
		game.world = new PacManClassicWorld();
		game.bonus = new Bonus(game.world);
		game.pac = new Pac(game.world);
		game.ghosts = new Ghost[4];
		for (int ghostID = 0; ghostID < 4; ++ghostID) {
			game.ghosts[ghostID] = new Ghost(ghostID, game.world);
		}
		return game;
	}

	public static PacManGameModel newMsPacManGame() {
		PacManGameModel game = new PacManGameModel();
		game.variant = MS_PACMAN;
		game.hiscore = new Hiscore(new File(System.getProperty("user.home"), "hiscore-mspacman.xml"));
		game.world = new MsPacManWorld();
		game.bonus = new MovingBonus(game.world);
		game.pac = new Pac(game.world);
		game.ghosts = new Ghost[4];
		for (int ghostID = 0; ghostID < 4; ++ghostID) {
			game.ghosts[ghostID] = new Ghost(ghostID, game.world);
		}
		return game;
	}

	public void initLevel(int n) {
		levelNumber = (short) n;
		level = world.createLevel(n);
		ghostBounty = 200;
		bonus.edibleTicksLeft = 0;
		bonus.eatenTicksLeft = 0;
		for (Ghost ghost : ghosts) {
			ghost.dotCounter = 0;
			ghost.elroy = 0;
		}
	}
}