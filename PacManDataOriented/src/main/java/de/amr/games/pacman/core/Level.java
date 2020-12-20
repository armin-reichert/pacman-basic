package de.amr.games.pacman.core;

/**
 * Data comprising a game level.
 * 
 * @author Armin Reichert
 *
 */
public class Level {

	public final int bonusSymbol;
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

	public Level(int... values) {
		bonusSymbol = values[0];
		bonusPoints = values[1];
		pacManSpeed = percent(values[2]);
		ghostSpeed = percent(values[3]);
		ghostSpeedTunnel = percent(values[4]);
		elroy1DotsLeft = values[5];
		elroy1Speed = percent(values[6]);
		elroy2DotsLeft = values[7];
		elroy2Speed = percent(values[8]);
		pacManSpeedPowered = percent(values[9]);
		ghostSpeedFrightened = percent(values[10]);
		ghostFrightenedSeconds = values[11];
		numFlashes = values[12];
	}
}