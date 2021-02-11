package de.amr.games.pacman.ui.fx.scene.mspacman;

import de.amr.games.pacman.model.PacManGameModel;
import de.amr.games.pacman.ui.fx.scene.common.AbstractPacManGameScene;
import javafx.scene.paint.Color;

public class MsPacManGameIntroScene extends AbstractPacManGameScene {

	public MsPacManGameIntroScene(PacManGameModel game, double width, double height, double scaling) {
		super(game, null, width, height, scaling, true);
	}

	@Override
	public void render() {
		fill(Color.PINK);
	}
}