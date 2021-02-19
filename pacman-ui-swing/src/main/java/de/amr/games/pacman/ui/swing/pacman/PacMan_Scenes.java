package de.amr.games.pacman.ui.swing.pacman;

import java.awt.Dimension;

import de.amr.games.pacman.controller.PacManGameState;
import de.amr.games.pacman.model.PacManGame;
import de.amr.games.pacman.model.PacManGameModel;
import de.amr.games.pacman.sound.PacManGameSounds;
import de.amr.games.pacman.sound.PacManGameSoundManager;
import de.amr.games.pacman.sound.SoundManager;
import de.amr.games.pacman.ui.swing.GameScene;
import de.amr.games.pacman.ui.swing.common.PlayScene;

/**
 * The scenes in the Pac-Man game.
 * 
 * @author Armin Reichert
 */
public class PacMan_Scenes {

	public static final SoundManager soundManager = new PacManGameSoundManager(PacManGameSounds::getPacManSoundURL);
	public static final PacMan_GameRendering rendering = new PacMan_GameRendering();

	private PacMan_IntroScene introScene;
	private PlayScene playScene;
	private GameScene intermissionScene1;
	private GameScene intermissionScene2;
	private GameScene intermissionScene3;

	public void createScenes(PacManGame game, Dimension unscaledSize) {
		introScene = new PacMan_IntroScene(unscaledSize, rendering, game);
		playScene = new PlayScene(unscaledSize, rendering, game);
		intermissionScene1 = new PacMan_IntermissionScene1(unscaledSize, game);
		intermissionScene2 = new PacMan_IntermissionScene2(unscaledSize, game);
		intermissionScene3 = new PacMan_IntermissionScene3(unscaledSize, game);
	}

	public GameScene selectScene(PacManGameModel game) {
		if (game.state == PacManGameState.INTRO) {
			return introScene;
		}
		if (game.state == PacManGameState.INTERMISSION) {
			return game.intermissionNumber == 1 ? intermissionScene1
					: game.intermissionNumber == 2 ? intermissionScene2 : intermissionScene3;
		}
		return playScene;
	}
}