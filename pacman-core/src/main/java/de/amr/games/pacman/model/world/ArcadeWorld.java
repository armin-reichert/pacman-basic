package de.amr.games.pacman.model.world;

import static de.amr.games.pacman.lib.Globals.v2i;

/**
 * @author Armin Reichert
 */
public class ArcadeWorld extends World {

    public static final int TILES_X = 28;
    public static final int TILES_Y = 36;

    public static House createArcadeHouse() {
        var house = new House(
                v2i(10, 15), // top-left corner tile
                v2i(8, 5),   // size in tiles
                new Door(v2i(13, 15), v2i(14, 15))
        );
        house.setSeat("left",   halfTileRightOf(11, 17));
        house.setSeat("middle", halfTileRightOf(13, 17));
        house.setSeat("right",  halfTileRightOf(15, 17));
        return house;
    }

    public ArcadeWorld(byte[][] tileMapData) {
        super(tileMapData);
        if (numCols() != TILES_X) {
            throw new IllegalArgumentException("Arcade world map must have 28 columns");
        }
        if (numRows() != TILES_Y) {
            throw new IllegalArgumentException("Arcade world map must have 36 rows");
        }
        setHouse(createArcadeHouse());
    }
}