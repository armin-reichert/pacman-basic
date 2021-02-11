package de.amr.games.pacman.ui.fx.scene.pacman;

import de.amr.games.pacman.model.PacManGameModel;
import de.amr.games.pacman.ui.fx.scene.common.AbstractPacManGameScene;
import javafx.scene.paint.Color;

public class PacManGameIntroScene extends AbstractPacManGameScene {

	public PacManGameIntroScene(PacManGameModel game, double width, double height, double scaling) {
		super(game, null, width, height, scaling, false);
	}

	@Override
	public void render() {
		fill(Color.BLUE);
	}
}