package de.amr.games.pacman.ui.swing.mspacman;

import java.awt.Dimension;

import de.amr.games.pacman.controller.PacManGameState;
import de.amr.games.pacman.model.MsPacManGame;
import de.amr.games.pacman.model.PacManGameModel;
import de.amr.games.pacman.sound.PacManGameSounds;
import de.amr.games.pacman.sound.PacManGameSoundManager;
import de.amr.games.pacman.sound.SoundManager;
import de.amr.games.pacman.ui.swing.GameScene;
import de.amr.games.pacman.ui.swing.common.PlayScene;

/**
 * The scenes in the Ms. Pac-Man game.
 * 
 * @author Armin Reichert
 */
public class MsPacMan_Scenes {

	public static final MsPacMan_GameRendering rendering = new MsPacMan_GameRendering();
	public static final SoundManager soundManager = new PacManGameSoundManager(
			PacManGameSounds::getMsPacManSoundURL);

	private GameScene introScene;
	private GameScene playScene;
	private GameScene intermissionScene1;
	private GameScene intermissionScene2;
	private GameScene intermissionScene3;

	public void createScenes(MsPacManGame game, Dimension unscaledSize_px) {
		introScene = new MsPacMan_IntroScene(unscaledSize_px, game);
		playScene = new PlayScene(unscaledSize_px, rendering, game);
		intermissionScene1 = new MsPacMan_IntermissionScene1(unscaledSize_px, game);
		intermissionScene2 = new MsPacMan_IntermissionScene2(unscaledSize_px, game);
		intermissionScene3 = new MsPacMan_IntermissionScene3(unscaledSize_px, game);
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