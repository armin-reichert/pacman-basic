package de.amr.games.pacman;

/**
 * The level-specific data.
 * <ol start="0">
 * <li>Bonus Symbol
 * <li>Bonus Points
 * <li>Pac-Man Speed
 * <li>Ghost Speed
 * <li>Ghost Tunnel Speed
 * <li>Elroy1 Dots Left
 * <li>Elroy1 Speed
 * <li>Elroy2 Dots Left
 * <li>Elroy2 Speed,
 * <li>Frightening Pac-Man Speed
 * <li>Frightened Ghost Speed,
 * <li>Frightened Time (sec)
 * <li>Number of Flashes.
 * </ol>
 * <img src="../../../../../resources/levels.png">
 */
public class PacManGameLevel {

	public final String bonusSymbol;
	public final int bonusPoints;
	public final float pacManSpeed;
	public final float ghostSpeed;
	public final float ghostSpeedTunnel;
	public final int elroy1DotsLeft;
	public final float elroy1Speed;
	public final int elroy2DotsLeft;
	public final float elroy2Speed;
	public final float pacManSpeedPowered;
	public final float ghostSpeedFrightened;
	public final int ghostFrightenedSeconds;
	public final int numFlashes;

	private static float percent(Object value) {
		return ((int) value) / 100f;
	}

	public PacManGameLevel(String symbolName, int... values) {
		bonusSymbol = symbolName;
		bonusPoints = values[0];
		pacManSpeed = percent(values[1]);
		ghostSpeed = percent(values[2]);
		ghostSpeedTunnel = percent(values[3]);
		elroy1DotsLeft = values[4];
		elroy1Speed = percent(values[5]);
		elroy2DotsLeft = values[6];
		elroy2Speed = percent(values[7]);
		pacManSpeedPowered = percent(values[8]);
		ghostSpeedFrightened = percent(values[9]);
		ghostFrightenedSeconds = values[10];
		numFlashes = values[11];
	}
}