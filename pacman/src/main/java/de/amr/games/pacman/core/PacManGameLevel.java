package de.amr.games.pacman.core;

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

	public final byte bonusSymbol;
	public final int bonusPoints;
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

	public PacManGameLevel(PacManGameLevel level, int bonusSymbol) {
		this.bonusSymbol = (byte) bonusSymbol;
		this.bonusPoints = level.bonusPoints;
		this.pacSpeed = level.pacSpeed;
		this.ghostSpeed = level.ghostSpeed;
		this.ghostSpeedTunnel = level.ghostSpeedTunnel;
		this.elroy1DotsLeft = level.elroy1DotsLeft;
		this.elroy1Speed = level.elroy1Speed;
		this.elroy2DotsLeft = level.elroy2DotsLeft;
		this.elroy2Speed = level.elroy2Speed;
		this.pacSpeedPowered = level.pacSpeedPowered;
		this.ghostSpeedFrightened = level.ghostSpeedFrightened;
		this.ghostFrightenedSeconds = level.ghostFrightenedSeconds;
		this.numFlashes = level.numFlashes;
	}

	public PacManGameLevel(int... values) {
		bonusSymbol = (byte) values[0];
		bonusPoints = (short) values[1];
		pacSpeed = percent(values[2]);
		ghostSpeed = percent(values[3]);
		ghostSpeedTunnel = percent(values[4]);
		elroy1DotsLeft = (byte) values[5];
		elroy1Speed = percent(values[6]);
		elroy2DotsLeft = (byte) values[7];
		elroy2Speed = percent(values[8]);
		pacSpeedPowered = percent(values[9]);
		ghostSpeedFrightened = percent(values[10]);
		ghostFrightenedSeconds = (byte) values[11];
		numFlashes = (byte) values[12];
	}
}