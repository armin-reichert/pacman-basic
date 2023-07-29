package de.amr.games.pacman.model.world;

import static de.amr.games.pacman.lib.Globals.v2i;

/**
 * @author Armin Reichert
 */
public class ArcadeWorld extends World {

    public static final House createArcadeHouse() {
        var arcadeHouse = new House(
                v2i(10, 15), // top-left corner tile
                v2i(8, 5),   // size in tiles
                new Door(v2i(13, 15), v2i(14, 15))
        );
        arcadeHouse.setSeat("left",   halfTileRightOf(11, 17));
        arcadeHouse.setSeat("middle", halfTileRightOf(13, 17));
        arcadeHouse.setSeat("right",  halfTileRightOf(15, 17));
        return arcadeHouse;
    }

    public ArcadeWorld(byte[][] tileMapData) {
        super(tileMapData, createArcadeHouse());
    }

}
