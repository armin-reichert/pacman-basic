package de.amr.games.pacman.core;

/**
 * Data comprising a game level.
 * 
 * @author Armin Reichert
 *
 */
public class Level {

	public final byte bonusSymbol;
	public final short bonusPoints;
	public final float pacManSpeed;
	public final float ghostSpeed;
	public final float ghostSpeedTunnel;
	public final byte elroy1DotsLeft;
	public final float elroy1Speed;
	public final byte elroy2DotsLeft;
	public final float elroy2Speed;
	public final float pacManSpeedPowered;
	public final float ghostSpeedFrightened;
	public final byte ghostFrightenedSeconds;
	public final byte numFlashes;

	private static float percent(Integer value) {
		return value / 100.0f;
	}

	public Level(int... values) {
		bonusSymbol = (byte) values[0];
		bonusPoints = (short) values[1];
		pacManSpeed = percent(values[2]);
		ghostSpeed = percent(values[3]);
		ghostSpeedTunnel = percent(values[4]);
		elroy1DotsLeft = (byte) values[5];
		elroy1Speed = percent(values[6]);
		elroy2DotsLeft = (byte) values[7];
		elroy2Speed = percent(values[8]);
		pacManSpeedPowered = percent(values[9]);
		ghostSpeedFrightened = percent(values[10]);
		ghostFrightenedSeconds = (byte) values[11];
		numFlashes = (byte) values[12];
	}
}