package de.amr.games.pacman.ui.swing.mspacman;

import java.util.ResourceBundle;

import de.amr.games.pacman.controller.PacManGameState;
import de.amr.games.pacman.lib.V2i;
import de.amr.games.pacman.model.MsPacManGame;
import de.amr.games.pacman.model.PacManGameModel;
import de.amr.games.pacman.sound.PacManGameSoundAssets;
import de.amr.games.pacman.sound.PacManGameSoundManager;
import de.amr.games.pacman.sound.SoundManager;
import de.amr.games.pacman.ui.swing.PacManGameScene;

public class MsPacManGameScenes {

	public final DefaultMsPacManGameRendering rendering;
	public final SoundManager soundManager;

	private MsPacManGameIntroScene introScene;
	private MsPacManGamePlayScene playScene;
	private PacManGameScene intermissionScene1;
	private PacManGameScene intermissionScene2;
	private PacManGameScene intermissionScene3;

	public MsPacManGameScenes() {
		rendering = new DefaultMsPacManGameRendering(ResourceBundle.getBundle("localization.translation"));
		soundManager = new PacManGameSoundManager(PacManGameSoundAssets::getMsPacManSoundURL);
	}

	public void createScenes(MsPacManGame game, V2i unscaledSize_px) {
		introScene = new MsPacManGameIntroScene(unscaledSize_px, rendering, game);
		playScene = new MsPacManGamePlayScene(unscaledSize_px, rendering, game);
		intermissionScene1 = new MsPacManIntermission1_TheyMeet(unscaledSize_px, rendering, soundManager, game);
		intermissionScene2 = new MsPacManIntermission2_TheChase(unscaledSize_px, rendering, soundManager, game);
		intermissionScene3 = new MsPacManIntermission3_Junior(unscaledSize_px, rendering, soundManager, game);
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