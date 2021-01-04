package de.amr.games.pacman.core;

import static de.amr.games.pacman.core.World.TS;

import de.amr.games.pacman.lib.Direction;
import de.amr.games.pacman.lib.V2f;
import de.amr.games.pacman.lib.V2i;

/**
 * Base class for Pac-Man and ghosts.
 * 
 * @author Armin Reichert
 */
public abstract class Creature {

	/** Left upper corner of collision box */
	public V2f position;
	public V2i homeTile;
	public V2i targetTile;
	public Direction dir;
	public Direction wishDir;
	/** Relative speed (between 0 and 1) */
	public float speed;
	public boolean visible;
	public boolean changedTile;
	public boolean couldMove;
	public boolean forcedOnTrack;
	public boolean dead;

	@Override
	public String toString() {
		return String.format("[%-8s tile=%s offset=%s dir=%s wishDir=%s speed=%.2f changedTile=%s couldMove=%s]", name(),
				tile(), offset(), dir, wishDir, speed, changedTile, couldMove);
	}

	public abstract void updateSpeed(World world, Level level);

	public abstract String name();

	public void placeAt(int x, int y, float offsetX, float offsetY) {
		position = new V2f(x * TS + offsetX, y * TS + offsetY);
	}

	public boolean at(V2i tile) {
		return tile().equals(tile);
	}

	public static V2i tile(V2f position) {
		return new V2i((int) position.x / TS, (int) position.y / TS);
	}

	public V2i tile() {
		return tile(position);
	}

	public static V2f offset(V2f position) {
		V2i tile = tile(position);
		return new V2f(position.x - tile.x * TS, position.y - tile.y * TS);
	}

	public V2f offset() {
		return offset(position);
	}

	public void setOffset(float offsetX, float offsetY) {
		V2i tile = tile();
		placeAt(tile.x, tile.y, offsetX, offsetY);
	}
}