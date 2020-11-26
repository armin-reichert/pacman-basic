package de.amr.games.pacman;

/**
 * The level-specific data.
 * <ol start="0">
 * <li>Bonus Symbol
 * <li>Bonus Points
 * <li>Pac-Man Speed
 * <li>Pac-Man Dots Speed,
 * <li>Ghost Speed
 * <li>Ghost Tunnel Speed
 * <li>Elroy1 Dots Left
 * <li>Elroy1 Speed
 * <li>Elroy2 Dots Left
 * <li>Elroy2 Speed,
 * <li>Frightening Pac-Man Speed
 * <li>Frightening Pac-Man Dots Speed
 * <li>Frightened Ghost Speed,
 * <li>Frightened Time (sec)
 * <li>Number of Flashes.
 * </ol>
 * <img src="../../../../../resources/levels.png">
 */
public class LevelData {

	public final String bonusSymbol;
	public final int bonusPoints;
	public final float pacManSpeed;
	public final float pacManDotsSpeed;
	public final float ghostSpeed;
	public final float ghostTunnelSpeed;
	public final int elroy1DotsLeft;
	public final float elroy1Speed;
	public final int elroy2DotsLeft;
	public final float elroy2Speed;
	public final float pacManPowerSpeed;
	public final float pacManPowerDotsSpeed;
	public final float frightenedGhostSpeed;
	public final int ghostFrightenedSeconds;
	public final int numFlashes;

	private static float percent(Object value) {
		return ((int) value) / 100f;
	}

	public LevelData(Object... values) {
		bonusSymbol = (String) values[0];
		bonusPoints = (int) values[1];
		pacManSpeed = percent(values[2]);
		pacManDotsSpeed = percent(values[3]);
		ghostSpeed = percent(values[4]);
		ghostTunnelSpeed = percent(values[5]);
		elroy1DotsLeft = (int) values[6];
		elroy1Speed = percent(values[7]);
		elroy2DotsLeft = (int) values[8];
		elroy2Speed = percent(values[9]);
		pacManPowerSpeed = percent(values[10]);
		pacManPowerDotsSpeed = percent(values[11]);
		frightenedGhostSpeed = percent(values[12]);
		ghostFrightenedSeconds = (int) values[13];
		numFlashes = (int) values[14];
	}
}