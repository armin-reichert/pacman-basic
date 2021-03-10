package de.amr.games.pacman.model.world;

import java.util.stream.IntStream;
import java.util.stream.Stream;

import de.amr.games.pacman.lib.V2d;
import de.amr.games.pacman.lib.V2i;

/**
 * Interface of world as seen by game.
 * 
 * @author Armin Reichert
 */
public interface PacManGameWorld {

	public static final byte TS = 8, HTS = 4;

	public static int t(double nTiles) {
		return (int) nTiles * TS;
	}

	public static V2i tile(V2d position) {
		return new V2i((int) position.x / TS, (int) position.y / TS);
	}

	public static V2d offset(V2d position) {
		return new V2d(position.x - (int) (position.x / TS) * TS, position.y - (int) (position.y / TS) * TS);
	}

	int xTiles();

	int yTiles();

	default Stream<V2i> tiles() {
		return IntStream.range(0, xTiles() * yTiles()).mapToObj(index -> new V2i(index % xTiles(), index / xTiles()));
	}

	default int index(V2i tile) {
		return xTiles() * tile.y + tile.x;
	}

	boolean insideMap(V2i tile);

	V2i pacHome();

	V2i ghostHome(int ghost);

	V2i ghostScatterTile(int ghost);

	V2i houseEntry();

	V2i houseCenter();

	V2i houseLeft();

	V2i houseRight();

	int numPortals();

	V2i portalLeft(int i);

	V2i portalRight(int i);

	default void setUpwardsBlocked(V2i... tiles) {
	}

	boolean isUpwardsBlocked(V2i tile);

	boolean isPortal(V2i tile);

	boolean isIntersection(V2i tile);

	boolean isWall(V2i tile);

	boolean isTunnel(V2i tile);

	boolean isGhostHouseDoor(V2i tile);

	boolean isFoodTile(V2i tile);

	boolean isEnergizerTile(V2i tile);

	Stream<V2i> energizerTiles();
}