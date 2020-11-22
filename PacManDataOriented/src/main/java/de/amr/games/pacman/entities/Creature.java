package de.amr.games.pacman.entities;

import static de.amr.games.pacman.World.TS;

import de.amr.games.pacman.common.Direction;
import de.amr.games.pacman.common.V2f;
import de.amr.games.pacman.common.V2i;

public class Creature {

	public final String name;
	public final V2i homeTile;

	public boolean visible;
	public float speed;
	public Direction dir;
	public Direction wishDir;
	public V2f position; // left upper corner
	public boolean changedTile;
	public boolean couldMove;
	public boolean forcedOnTrack;
	public boolean forcedTurningBack;
	public boolean dead;

	public Creature(String name, V2i homeTile) {
		this.name = name;
		this.homeTile = homeTile;
	}

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

	@Override
	public String toString() {
		return String.format("[%-8s tile=%s offset=%s dir=%s wishDir=%s speed=%.2f changedTile=%s]", name, tile(), offset(),
				dir, wishDir, speed, changedTile);
	}
}