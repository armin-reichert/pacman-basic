package de.amr.games.pacman.core;

/**
 * Data comprising a game level.
 * 
 * @author Armin Reichert
 *
 */
public class Level {

	public final short bonusSymbol;
	public final short bonusPoints;
	public final float pacManSpeed;
	public final float ghostSpeed;
	public final float ghostSpeedTunnel;
	public final short elroy1DotsLeft;
	public final float elroy1Speed;
	public final short elroy2DotsLeft;
	public final float elroy2Speed;
	public final float pacManSpeedPowered;
	public final float ghostSpeedFrightened;
	public final short ghostFrightenedSeconds;
	public final byte numFlashes;

	private static float percent(Integer value) {
		return value / 100.0f;
	}

	public Level(int... values) {
		bonusSymbol = (short) values[0];
		bonusPoints = (short) values[1];
		pacManSpeed = percent(values[2]);
		ghostSpeed = percent(values[3]);
		ghostSpeedTunnel = percent(values[4]);
		elroy1DotsLeft = (short) values[5];
		elroy1Speed = percent(values[6]);
		elroy2DotsLeft = (short) values[7];
		elroy2Speed = percent(values[8]);
		pacManSpeedPowered = percent(values[9]);
		ghostSpeedFrightened = percent(values[10]);
		ghostFrightenedSeconds = (short) values[11];
		numFlashes = (byte) values[12];
	}
}