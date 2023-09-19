package de.amr.games.pacman.model.world;

import de.amr.games.pacman.lib.Vector2f;

import static de.amr.games.pacman.lib.Globals.checkNotNull;
import static de.amr.games.pacman.lib.Globals.v2i;

/**
 * @author Armin Reichert
 */
public class ArcadeWorld extends World {

    public static class ArcadeHouse extends House {

        private static final Vector2f SEAT_LEFT   = halfTileRightOf(11, 17);
        private static final Vector2f SEAT_MIDDLE = halfTileRightOf(13, 17);
        private static final Vector2f SEAT_RIGHT  = halfTileRightOf(15, 17);

        public ArcadeHouse() {
            setMinTile(v2i(10, 15));
            setSize(v2i(8, 5));
            setDoor(new Door(v2i(13, 15), v2i(14, 15)));
        }

        @Override
        public Vector2f seat(String id) {
            checkNotNull(id);
            switch (id) {
                case "left":   return SEAT_LEFT;
                case "middle": return SEAT_MIDDLE;
                case "right":  return SEAT_RIGHT;
                default: throw new IllegalArgumentException("Illegal seat ID: " + id);
            }
        }
    }

    public static final int TILES_X = 28;
    public static final int TILES_Y = 36;

    public static final House ARCADE_HOUSE = new ArcadeHouse();

    public ArcadeWorld(byte[][] tileMapData) {
        super(tileMapData);
        if (numCols() != TILES_X) {
            throw new IllegalArgumentException(String.format("Arcade world map must have %d columns", TILES_X));
        }
        if (numRows() != TILES_Y) {
            throw new IllegalArgumentException(String.format("Arcade world map must have %d rows", TILES_Y));
        }
        setHouse(ARCADE_HOUSE);
    }
}