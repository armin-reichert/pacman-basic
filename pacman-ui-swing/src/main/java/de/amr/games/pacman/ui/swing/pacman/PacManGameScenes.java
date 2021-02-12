package de.amr.games.pacman.ui.swing.pacman;

import java.util.ResourceBundle;

import de.amr.games.pacman.controller.PacManGameState;
import de.amr.games.pacman.lib.V2i;
import de.amr.games.pacman.model.PacManGame;
import de.amr.games.pacman.model.PacManGameModel;
import de.amr.games.pacman.sound.PacManGameSoundAssets;
import de.amr.games.pacman.sound.PacManGameSoundManager;
import de.amr.games.pacman.sound.SoundManager;
import de.amr.games.pacman.ui.swing.PacManGameScene;

public class PacManGameScenes {

	public final DefaultPacManGameRendering rendering;
	public final SoundManager soundManager;

	private PacManGameIntroScene introScene;
	private PacManGamePlayScene playScene;
	private PacManGameScene intermissionScene1;
	private PacManGameScene intermissionScene2;
	private PacManGameScene intermissionScene3;

	public PacManGameScenes() {
		rendering = new DefaultPacManGameRendering(ResourceBundle.getBundle("localization.translation"));
		soundManager = new PacManGameSoundManager(PacManGameSoundAssets::getPacManSoundURL);
	}

	public void createScenes(PacManGame game, V2i unscaledSize_px) {
		introScene = new PacManGameIntroScene(unscaledSize_px, rendering, game);
		playScene = new PacManGamePlayScene(unscaledSize_px, rendering, game);
		intermissionScene1 = new PacManGameIntermission1(unscaledSize_px, rendering, soundManager, game);
		intermissionScene2 = new PacManGameIntermission2(unscaledSize_px, rendering, soundManager, game);
		intermissionScene3 = new PacManGameIntermission3(unscaledSize_px, rendering, soundManager, game);
	}

	public PacManGameScene selectScene(PacManGameModel game) {
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