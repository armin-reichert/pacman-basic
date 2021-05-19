package de.amr.games.pacman.model.world;

import java.io.PrintWriter;

public class WallMapGenerator {

	public static void main(String[] args) {
		new WallMapGenerator().run();
	}

	private void run() {
		WorldMap map = WorldMap.load("/pacman/maps/map1.txt");
		MapBasedPacManGameWorld world = new MapBasedPacManGameWorld();
		world.setMap(map);
		WallScanner scanner = new WallScanner(8);
		WallMap wallMap = scanner.scan(world);
		WallMap.printWallMap(new PrintWriter(System.out), wallMap, true);
	}

}
