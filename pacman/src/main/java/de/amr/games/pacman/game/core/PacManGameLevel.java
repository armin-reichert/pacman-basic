package de.amr.games.pacman.game.core;

/**
 * Data comprising a game level.
 * 
 * @author Armin Reichert
 *
 */
public class PacManGameLevel {

	private static float percent(Integer value) {
		return value / 100.0f;
	}

	public byte bonusSymbol;
	public final float pacSpeed;
	public final float ghostSpeed;
	public final float ghostSpeedTunnel;
	public final byte elroy1DotsLeft;
	public final float elroy1Speed;
	public final byte elroy2DotsLeft;
	public final float elroy2Speed;
	public final float pacSpeedPowered;
	public final float ghostSpeedFrightened;
	public final byte ghostFrightenedSeconds;
	public final byte numFlashes;

	public PacManGameLevel(int... values) {
		int i = 0;
		bonusSymbol = (byte) values[i++];
		pacSpeed = percent(values[i++]);
		ghostSpeed = percent(values[i++]);
		ghostSpeedTunnel = percent(values[i++]);
		elroy1DotsLeft = (byte) values[i++];
		elroy1Speed = percent(values[i++]);
		elroy2DotsLeft = (byte) values[i++];
		elroy2Speed = percent(values[i++]);
		pacSpeedPowered = percent(values[i++]);
		ghostSpeedFrightened = percent(values[i++]);
		ghostFrightenedSeconds = (byte) values[i++];
		numFlashes = (byte) values[i++];
	}
}