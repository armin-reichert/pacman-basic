package de.amr.games.pacman.ui.fx.scene.pacman;

import de.amr.games.pacman.heaven.God;
import de.amr.games.pacman.model.PacManGameModel;
import de.amr.games.pacman.sound.PacManGameSound;
import de.amr.games.pacman.sound.SoundManager;
import de.amr.games.pacman.ui.fx.scene.common.AbstractPacManGameScene;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

public class PacManGameIntermissionScene2 extends AbstractPacManGameScene {

	public PacManGameIntermissionScene2(PacManGameModel game, SoundManager soundManager, double width, double height,
			double scaling) {
		super(game, soundManager, width, height, scaling, false);
	}

	@Override
	public void start() {
		soundManager.loop(PacManGameSound.INTERMISSION_2, 2);
	}

	@Override
	public void update() {
		if (game.state.ticksRun() == God.clock.sec(12)) {
			game.state.duration(0);
		}
	}

	@Override
	public void render() {
		fill(Color.RED);
		g.setFont(Font.getDefault());
		g.setFill(Color.WHITE);
		g.fillText("Intermission 2", 10, 10);
	}
}